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

test('钱包与流水分页只走本人交易路径', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [{ transactionNo: 'WT1' }], total: 1 } });
  await client.wallet();
  assert.equal(calls[0].url, 'https://example.test/trade/app/wallet');
  assert.equal(calls[0].header.Authorization, 'Bearer member-token');
  const page = await client.walletTransactions({ pageNum: 2, pageSize: 10, bizType: 'RECHARGE' });
  assert.equal(calls[1].url, 'https://example.test/trade/app/wallet/transactions');
  assert.deepEqual(calls[1].data, { pageNum: 2, pageSize: 10, bizType: 'RECHARGE' });
  assert.deepEqual(page, { records: [{ transactionNo: 'WT1' }], total: 1 });
  assert.throws(() => client.walletTransactions({ bizType: 'UNKNOWN' }));
});

let definition;
function makePage(client) {
  global.getApp = () => ({ globalData: { memberSession: { memberId: '1', accessToken: 'token' } } });
  global.wx = { navigateTo() {} };
  delete require.cache[require.resolve('../pages/member-wallet/index')];
  global.Page = value => { definition = value; };
  require('../pages/member-wallet/index');
  const page = { ...definition };
  page.data = { ...JSON.parse(JSON.stringify(page.data || {})), loading: false };
  page.setData = function (value) { Object.assign(this.data, value); };
  page.client = client;
  return page;
}

test('钱包页面校验金额并加载账户与流水', async () => {
  const page = makePage({
    wallet: async () => ({ balance: '12.00', freezePrice: '0.00', totalExpense: '0.00', totalRecharge: '12.00' }),
    walletTransactions: async () => ({
      records: [{ transactionNo: 'WT1', title: '充值', bizType: 'RECHARGE', bizNo: 'R1', amount: '12.00', balanceAfter: '12.00' }],
      total: 1,
    }),
  });
  await page.reload();
  assert.equal(page.data.wallet.balance, '12.00');
  assert.equal(page.data.records.length, 1);
  assert.equal(page.data.records[0].increase, true);
  assert.throws(() => page.toWallet({ balance: 12, freezePrice: '0.00', totalExpense: '0.00', totalRecharge: '0.00' }));
  assert.throws(() => page.toTransaction({ transactionNo: 'WT1', amount: '-1.00', balanceAfter: '-1.00' }));
});
