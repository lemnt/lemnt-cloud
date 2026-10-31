const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createBargainClient } = require('../services/bargain');

function setup(response) {
  const calls = [];
  const client = createBargainClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async (options) => { calls.push(options); return response; },
  });
  return { client, calls };
}

test('砍价活动列表与详情严格解码金额', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [{
    id: 1, name: '砍价', spuId: 2, spuName: '商品', picUrl: null, skuPrice: '12.00', skuId: 3,
    bargainFirstPrice: '10.00', bargainMinPrice: '8.00', stock: 5, totalStock: 5, helpMaxCount: 3,
    bargainCount: 1, totalLimitCount: 1, startTime: '2026-11-01 00:00:00',
    endTime: '2026-11-02 00:00:00', successCount: 0,
  }], total: 1 } });
  const page = await client.activities({ pageNum: 1 });
  assert.equal(page.records[0].bargainMinPrice, '8.00');
  assert.deepEqual(calls[0].data, { pageNum: 1, pageSize: 20 });
  assert.equal(calls[0].header.Authorization, undefined);
});

test('发起砍价和助力需要会员令牌并返回结果', async () => {
  const created = setup({ statusCode: 200, data: { code: 200, data: 7 } });
  const record = await created.client.createRecord('2');
  assert.equal(record.id, '7');
  assert.equal(created.calls[0].url, 'https://example.test/mall/app/bargain/records');
  assert.equal(created.calls[0].method, 'POST');
  assert.equal(created.calls[0].header.Authorization, 'Bearer member-token');

  const helped = setup({ statusCode: 200, data: { code: 200, data: { id: 8, userId: 2, reducePrice: '1.00' } } });
  const help = await helped.client.help('7');
  assert.equal(help.reducePrice, '1.00');
  assert.equal(helped.calls[0].url, 'https://example.test/mall/app/bargain/helps');
});

test('砍价详情包含助力与本人助力状态', async () => {
  const { client } = setup({ statusCode: 200, data: { code: 200, data: {
    activity: {
      id: '2', name: '砍价', spuId: '3', spuName: '商品', picUrl: null, skuPrice: '12.00', skuId: '4',
      bargainFirstPrice: '10.00', bargainMinPrice: '8.00', stock: 5, totalStock: 5, helpMaxCount: 3,
      bargainCount: 1, totalLimitCount: 1, startTime: '2026-11-01 00:00:00',
      endTime: '2026-11-02 00:00:00', successCount: 1,
    },
    record: {
      id: '7', userId: '1', activityId: '2', spuId: '3', spuName: '商品', picUrl: null, skuId: '4',
      bargainFirstPrice: '10.00', bargainPrice: '9.00', status: 1, endTime: null, orderId: null,
    },
    helps: [{ id: '8', userId: '2', reducePrice: '1.00' }],
    helped: true,
  } } });
  const detail = await client.record('7');
  assert.equal(detail.record.bargainPrice, '9.00');
  assert.equal(detail.helps.length, 1);
  assert.equal(detail.helped, true);
});
