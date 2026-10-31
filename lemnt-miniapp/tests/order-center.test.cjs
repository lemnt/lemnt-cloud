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

test('订单分页只走 trade/app 并携带筛选，不发送会员身份', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [{ orderNo: 'TR1' }], total: 1 } });
  const page = await client.orderPage({ status: 'COMPLETED', commentStatus: false, pageNum: 2, pageSize: 10, memberId: 999 });
  assert.equal(calls[0].url, 'https://example.test/trade/app/orders');
  assert.equal(calls[0].method, 'GET');
  assert.deepEqual(calls[0].data, { pageNum: 2, pageSize: 10, status: 'COMPLETED', commentStatus: false });
  assert.deepEqual(page, { records: [{ orderNo: 'TR1' }], total: 1 });
});

test('订单详情、轨迹、状态数量与可售后额度使用本人交易路径', async () => {
  const { client, calls } = setup();
  await client.orderStatusCounts();
  await client.orderDetail('TR1');
  await client.orderTracks('TR1');
  await client.afterSaleItems('TR1');
  assert.equal(calls[0].url, 'https://example.test/trade/app/orders/status-counts');
  assert.equal(calls[1].url, 'https://example.test/trade/app/orders/TR1');
  assert.equal(calls[2].url, 'https://example.test/trade/app/orders/TR1/tracks');
  assert.equal(calls[3].url, 'https://example.test/trade/app/orders/TR1/after-sale-items');
  assert.ok(calls.every(call => call.header.Authorization === 'Bearer member-token'));
});

test('售后申请由服务端计算金额，只发送必要字段', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, data: { refundNo: 'RF1' } } });
  const result = await client.createAfterSale('TR1', {
    requestNo: 'REQ1', orderItemId: 10, quantity: 2, type: 'RETURN_REFUND', reason: '质量问题',
    memberId: 999, amount: '0.01',
  });
  assert.equal(calls[0].url, 'https://example.test/trade/app/orders/TR1/after-sales');
  assert.deepEqual(calls[0].data, { requestNo: 'REQ1', orderItemId: 10, quantity: 2, type: 'RETURN_REFUND', reason: '质量问题' });
  assert.deepEqual(result, { refundNo: 'RF1' });
});

test('售后分页、详情与登记退货只走本人交易路径', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [], total: 0 } });
  await client.afterSalePage({ pageNum: 1, pageSize: 10, status: 'REQUESTED', memberId: 999 });
  assert.equal(calls[0].url, 'https://example.test/trade/app/after-sales');
  assert.deepEqual(calls[0].data, { pageNum: 1, pageSize: 10, status: 'REQUESTED' });
  const { client: second, calls: secondCalls } = setup();
  await second.afterSaleDetail('RF1');
  await second.returnAfterSale('RF1', { carrierCode: 'SF', trackingNo: '123', memberId: 999 });
  assert.equal(secondCalls[0].url, 'https://example.test/trade/app/after-sales/RF1');
  assert.deepEqual(secondCalls[1].data, { carrierCode: 'SF', trackingNo: '123' });
});

test('参数校验阻止非法分页和售后字段', () => {
  const { client } = setup();
  assert.throws(() => client.orderPage({ pageNum: 0, pageSize: 10 }));
  assert.throws(() => client.orderPage({ pageNum: 1, pageSize: 0 }));
  assert.throws(() => client.orderPage({ status: 'UNKNOWN' }));
  assert.throws(() => client.createAfterSale('TR1', { requestNo: '', orderItemId: 1, quantity: 1, type: 'REFUND_ONLY', reason: 'r' }));
  assert.throws(() => client.createAfterSale('TR1', { requestNo: 'R', orderItemId: 0, quantity: 1, type: 'REFUND_ONLY', reason: 'r' }));
  assert.throws(() => client.createAfterSale('TR1', { requestNo: 'R', orderItemId: 1, quantity: 0, type: 'REFUND_ONLY', reason: 'r' }));
  assert.throws(() => client.createAfterSale('TR1', { requestNo: 'R', orderItemId: 1, quantity: 1, type: 'X', reason: 'r' }));
  assert.throws(() => client.createAfterSale('TR1', { requestNo: 'R', orderItemId: 1, quantity: 1, type: 'REFUND_ONLY', reason: '  ' }));
  assert.throws(() => client.returnAfterSale('RF1', { carrierCode: '', trackingNo: '1' }));
  assert.throws(() => client.returnAfterSale('RF1', { carrierCode: 'SF', trackingNo: '' }));
});
