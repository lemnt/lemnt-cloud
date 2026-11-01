const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createPickUpClient } = require('../services/pickup');

test('自提门店列表只走交易会员路径并严格解码', async () => {
  const calls = [];
  const client = createPickUpClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async options => { calls.push(options); return { statusCode: 200, data: { code: 200, data: [{
      id: 1, name: '中心店', introduction: null, phone: '13800000000', areaId: 1,
      detailAddress: '中心路1号', logo: null, openingTime: '09:00:00', closingTime: '21:00:00',
      latitude: null, longitude: null,
    }] } }; },
  });
  const stores = await client.stores();
  assert.equal(stores[0].name, '中心店');
  assert.equal(calls[0].url, 'https://example.test/trade/app/delivery/pick-up-stores');
  assert.equal(calls[0].header.Authorization, 'Bearer member-token');
  await client.stores({ latitude: 31.23, longitude: 121.47 });
  assert.equal(calls[1].url, 'https://example.test/trade/app/delivery/pick-up-stores?latitude=31.23&longitude=121.47');
  await assert.rejects(client.stores({ latitude: 91, longitude: 0 }), /坐标/);
  const bad = createPickUpClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async () => ({ statusCode: 200, data: { code: 200, data: [{ id: 0 }] } }),
  });
  await assert.rejects(bad.stores(), /编号/);
  const anonymous = createPickUpClient({ baseUrl: 'https://example.test', ready: true, request: async () => { throw new Error('不应请求'); } });
  await assert.rejects(anonymous.stores(), /会员登录/);
});

let definition;
test('自提门店页加载门店', async () => {
  global.Page = value => { definition = value; };
  global.wx = { request() {} };
  delete require.cache[require.resolve('../pages/pickup-stores/index')];
  require('../pages/pickup-stores/index');
  const page = { ...definition };
  page.data = JSON.parse(JSON.stringify(page.data));
  page.setData = function (next) { Object.assign(this.data, next); };
  page.client = { stores: async (location) => [{ id: '1', name: '中心店', distanceKm: location ? '0.00' : null }] };
  await page.load();
  assert.equal(page.data.stores[0].name, '中心店');
  global.wx.getLocation = options => options.success({ latitude: 31.23, longitude: 121.47 });
  await page.sortByDistance();
  assert.equal(page.data.stores[0].distanceKm, '0.00');
});
