const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createKefuClient } = require('../services/kefu');
const { createDiyClient, template, components } = require('../services/diy');

function client(factory, data) {
  const calls = [];
  const value = factory({
    baseUrl: 'https://example.test',
    ready: true,
    getMemberToken: () => 'member-token',
    request: async (options) => {
      calls.push(options);
      return { statusCode: 200, data: { code: 200, data } };
    },
  });
  return { value, calls };
}

test('客服会话、消息、发送与已读走会员接口', async () => {
  let x = client(createKefuClient, {
    id: 1,
    lastMessageTime: '2026-11-01 10:00:00',
    lastMessageContent: '你好',
    lastMessageContentType: 1,
    unreadCount: 2,
  });
  assert.deepEqual(await x.value.conversation(), {
    id: '1',
    lastMessageTime: '2026-11-01 10:00:00',
    lastMessageContent: '你好',
    lastMessageContentType: 1,
    unreadCount: 2,
  });
  assert.equal(x.calls[0].url, 'https://example.test/mall/app/kefu/conversation');
  x = client(createKefuClient, [
    {
      id: 2,
      senderType: 2,
      contentType: 1,
      content: '回复',
      readStatus: false,
      createTime: '2026-11-01 10:00:00',
    },
  ]);
  const messages = await x.value.messages(10);
  assert.equal(messages[0].senderType, 2);
  assert.equal(x.calls[0].url, 'https://example.test/mall/app/kefu/messages');
  assert.deepEqual(x.calls[0].data, { limit: 10 });
  x = client(createKefuClient, '3');
  assert.equal(await x.value.send(' 你好 ', 1), '3');
  assert.equal(x.calls[0].url, 'https://example.test/mall/app/kefu/messages');
  assert.deepEqual(x.calls[0].data, { contentType: 1, content: '你好' });
  await x.value.read();
  assert.equal(x.calls[1].url, 'https://example.test/mall/app/kefu/read');
  assert.throws(() => x.value.send(' ', 1), /消息/);
  assert.throws(() => x.value.send('x', 9), /类型/);
});

test('装修模板解析 JSON 属性并只支持声明组件', async () => {
  const home = JSON.stringify({
    components: [
      {
        id: 'Carousel',
        property: {
          items: [{ type: 'img', imgUrl: 'https://example.test/a.png', url: '/pages/store/index' }],
        },
      },
      { id: 'TitleBar', property: { title: '标题' } },
      { id: 'Unknown', property: {} },
    ],
  });
  const x = client(createDiyClient, {
    id: 1,
    name: '模板',
    property: '{"tabbar":{}}',
    home,
    user: '{}',
  });
  const value = await x.value.used();
  assert.equal(value.id, '1');
  assert.equal(components(value.home)[0].id, 'Carousel');
  assert.equal(x.calls[0].url, 'https://example.test/mall/app/diy/template/used');
  assert.throws(
    () => template({ id: 1, name: 'x', property: 'bad', home: '{}', user: '{}' }),
    /JSON/,
  );
  assert.throws(() => components({ components: [{ id: '' }] }), /组件/);
});

let definition;
function page(modulePath) {
  delete require.cache[require.resolve(modulePath)];
  global.Page = (value) => {
    definition = value;
  };
  require(modulePath);
  const value = { ...definition };
  value.data = JSON.parse(JSON.stringify(value.data || {}));
  value.setData = function (next) {
    Object.assign(this.data, next);
  };
  return value;
}
function globals() {
  global.getApp = () => ({
    globalData: { memberSession: { memberId: '1', accessToken: 'token' } },
  });
  global.wx = { navigateTo() {}, setClipboardData() {}, showToast() {} };
}

test('客服页加载会话并发送后刷新', async () => {
  globals();
  const p = page('../pages/kefu/index');
  const calls = [];
  p.kefu = {
    conversation: async () => ({ id: '1', unreadCount: 1 }),
    messages: async () => [
      {
        id: '2',
        senderType: 2,
        contentType: 1,
        content: '回复',
        readStatus: false,
        createTime: 't',
      },
    ],
    read: async () => calls.push('read'),
    send: async (content, type) => {
      calls.push([content, type]);
      return '3';
    },
  };
  await p.load();
  assert.equal(p.data.messages[0].content, '回复');
  p.data.content = ' 你好 ';
  await p.send();
  assert.deepEqual(calls, [['你好', 1]]);
  assert.equal(p.data.content, '');
});

test('装修页把首页组件转换为可渲染列表', async () => {
  globals();
  const p = page('../pages/diy/index');
  p.client = {
    used: async () => ({
      id: '1',
      name: '模板',
      property: {},
      user: {},
      home: {
        components: [
          {
            id: 'Carousel',
            property: {
              items: [
                { type: 'img', imgUrl: 'https://example.test/a.png', url: '/pages/store/index' },
              ],
            },
          },
          { id: 'NoticeBar', property: { contents: [{ text: '公告', url: '' }] } },
          { id: 'Unknown', property: {} },
        ],
      },
    }),
  };
  await p.load();
  assert.deepEqual(
    p.data.items.map((item) => item.type),
    ['carousel', 'notice', 'unsupported'],
  );
  assert.equal(p.data.items[0].items[0].imgUrl, 'https://example.test/a.png');
  assert.equal(p.data.items[1].text, '公告');
});
