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

test('转账与本人转账分页只走交易路径并归一金额', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, data: { transferNo: 'TF1' } } });
  await client.walletTransfer({ toMemberId: 2, amount: '10.5', remark: ' 备注 ', requestNo: 'REQ1', memberId: 1 });
  assert.equal(calls[0].url, 'https://example.test/trade/app/wallet/transfers');
  assert.equal(calls[0].method, 'POST');
  assert.deepEqual(calls[0].data, { toMemberId: 2, amount: '10.50', requestNo: 'REQ1', remark: '备注' });
  const { client: second, calls: secondCalls } = setup({ statusCode: 200, data: { code: 200, rows: [], total: 0 } });
  const page = await second.myTransfers({ pageNum: 2, pageSize: 10 });
  assert.deepEqual(page, { records: [], total: 0 });
  assert.deepEqual(secondCalls[0].data, { pageNum: 2, pageSize: 10 });
  assert.throws(() => client.walletTransfer({ toMemberId: 0, amount: '1.00', requestNo: 'R' }));
  assert.throws(() => client.walletTransfer({ toMemberId: 2, amount: '0', requestNo: 'R' }));
  assert.throws(() => client.walletTransfer({ toMemberId: 2, amount: '1.00', requestNo: '' }));
});

let definition;
function makePage(client) {
  global.getApp = () => ({ globalData: { memberSession: { memberId: '1', accessToken: 'token' } } });
  global.wx = { navigateTo() {}, showToast() {} };
  delete require.cache[require.resolve('../pages/wallet-transfer/index')];
  global.Page = value => { definition = value; };
  require('../pages/wallet-transfer/index');
  const page = { ...definition };
  page.data = { ...JSON.parse(JSON.stringify(page.data || {})), loading: false };
  page.setData = function (value) { Object.assign(this.data, value); };
  page.client = client;
  return page;
}

test('转账页校验输入并复用请求编号', async () => {
  const calls = [];
  let attempt = 0;
  const page = makePage({
    myTransfers: async () => ({ records: [], total: 0 }),
    walletTransfer: async input => {
      calls.push(input);
      if (attempt++ === 0) throw new Error('网络未确认');
      return { transferNo: 'TF1', status: 'SUCCESS', amount: '10.00' };
    },
  });
  await page.reload();
  page.inputTo({ detail: { value: '2' } });
  page.inputAmount({ detail: { value: '10.00' } });
  await page.submit();
  await page.submit();
  assert.equal(calls.length, 2);
  assert.equal(calls[0].requestNo, calls[1].requestNo);
  assert.throws(() => page.toTransfer({ transferNo: 'TF1', status: 'FAILED', amount: '1.00' }));
});
