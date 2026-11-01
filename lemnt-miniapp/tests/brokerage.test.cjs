const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createBrokerageClient } = require('../services/brokerage');

function setup(data) {
  const calls = [];
  const client = createBrokerageClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async options => { calls.push(options); return { statusCode: 200, data: { code: 200, ...data } }; },
  });
  return { client, calls };
}

test('分销信息、绑定、记录与提现走会员接口', async () => {
  let x = setup({ data: { id: 1, bindUserId: 2, bindNickname: '推广员', brokerageEnabled: true, brokeragePrice: '10.00', frozenPrice: '5.00', firstLevelCount: 1, secondLevelCount: 2 } });
  assert.equal((await x.client.user()).brokeragePrice, '10.00');
  assert.equal(x.calls[0].url, 'https://example.test/trade/app/brokerage/user');
  x = setup({ data: true });
  await x.client.bind('2');
  assert.deepEqual(x.calls[0].data, { bindUserId: '2' });
  x = setup({ rows: [{ id: 3, bizId: 'TR1', bizType: 1, title: '佣金', price: '10.00', totalPrice: '10.00', status: 0, createTime: 't' }], total: 1 });
  assert.equal((await x.client.records()).records[0].price, '10.00');
  x = setup({ data: 4 });
  assert.equal(await x.client.createWithdraw({ price: '4', type: 1 }), 4);
  assert.deepEqual(x.calls[0].data, { price: '4.00', type: 1, userName: null, userAccount: null, qrCodeUrl: null, bankName: null, bankAddress: null });
  assert.throws(() => x.client.createWithdraw({ price: '0', type: 1 }), /金额/);
  assert.throws(() => x.client.createWithdraw({ price: '1', type: 9 }), /方式/);
});

let definition;
function page() {
  delete require.cache[require.resolve('../pages/brokerage/index')];
  global.Page = value => { definition = value; };
  global.getApp = () => ({ globalData: { memberSession: { memberId: '1', accessToken: 'token' } } });
  global.wx = { navigateTo() {} };
  require('../pages/brokerage/index');
  const value = { ...definition };
  value.data = JSON.parse(JSON.stringify(value.data));
  value.setData = function (next) { Object.assign(this.data, next); };
  return value;
}

test('分销页加载佣金并提交绑定与提现', async () => {
  const p = page();
  const calls = [];
  p.brokerage = {
    user: async () => ({ brokeragePrice: '10.00', frozenPrice: '0.00', firstLevelCount: 0, secondLevelCount: 0 }),
    records: async () => ({ records: [], total: 0 }),
    withdraws: async () => ({ records: [], total: 0 }),
    bind: async id => calls.push(['bind', id]),
    createWithdraw: async input => calls.push(['withdraw', input]),
  };
  await p.reload();
  assert.equal(p.data.user.brokeragePrice, '10.00');
  p.data.bindId = '2';
  await p.bind();
  p.data.price = '4';
  p.data.type = 1;
  await p.withdraw();
  assert.deepEqual(calls.map(item => item[0]), ['bind', 'withdraw']);
});
