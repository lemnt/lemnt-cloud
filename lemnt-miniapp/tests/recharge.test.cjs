const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createTradeClient } = require('../services/trade');

function setup(response = { statusCode: 200, data: { code: 200, data: [] } }) {
  const calls = [];
  const client = createTradeClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async options => { calls.push(options); return response; },
  });
  return { client, calls };
}

test('套餐、创建充值单与本人充值单只走交易路径', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, data: [] } });
  await client.rechargePackages();
  assert.equal(calls[0].url, 'https://example.test/trade/app/recharge-packages');
  await client.createRecharge({ packageId: 3, requestNo: 'REQ1', amount: '1.00' });
  assert.equal(calls[1].url, 'https://example.test/trade/app/wallet/recharges');
  assert.deepEqual(calls[1].data, { packageId: 3, requestNo: 'REQ1' });
  const page = await client.myRecharges({ pageNum: 2, pageSize: 10 });
  assert.equal(calls[2].url, 'https://example.test/trade/app/wallet/recharges');
  assert.deepEqual(calls[2].data, { pageNum: 2, pageSize: 10 });
  assert.deepEqual(page, { records: [], total: 0 });
  assert.throws(() => client.createRecharge({ packageId: 0, requestNo: 'REQ1' }));
  assert.throws(() => client.createRecharge({ packageId: 3, requestNo: '' }));
});

let definition;
function makePage(client) {
  global.getApp = () => ({ globalData: { memberSession: { memberId: '1', accessToken: 'token' } } });
  global.wx = { navigateTo() {}, showToast() {} };
  delete require.cache[require.resolve('../pages/wallet-recharge/index')];
  global.Page = value => { definition = value; };
  require('../pages/wallet-recharge/index');
  const page = { ...definition };
  page.data = { ...JSON.parse(JSON.stringify(page.data || {})), loading: false };
  page.setData = function (value) { Object.assign(this.data, value); };
  page.client = client;
  return page;
}

test('充值页校验套餐金额并复用请求编号', async () => {
  const created = [];
  let attempt = 0;
  const page = makePage({
    rechargePackages: async () => [{ id: 3, name: '标准', payPrice: '100.00', bonusPrice: '20.00' }],
    myRecharges: async () => ({ records: [], total: 0 }),
    createRecharge: async input => {
      created.push(input);
      if (attempt++ === 0) throw new Error('网络未确认');
      return { rechargeNo: 'RC1', status: 'PENDING', totalPrice: '120.00' };
    },
  });
  await page.reload();
  assert.equal(page.data.packages[0].total, '120.00');
  page.choosePackage({ currentTarget: { dataset: { index: 0 } } });
  await page.recharge();
  await page.recharge();
  assert.equal(created.length, 2);
  assert.equal(created[0].requestNo, created[1].requestNo);
  assert.throws(() => page.toPackage({ id: 3, payPrice: 100, bonusPrice: '0.00' }));
  assert.throws(() => page.toRecharge({ rechargeNo: 'RC1', status: 'UNKNOWN', totalPrice: '1.00' }));
});
