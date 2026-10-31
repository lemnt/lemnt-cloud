const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createSeckillClient } = require('../services/seckill');

function setup(response) {
  const calls = [];
  const client = createSeckillClient({
    baseUrl: 'https://example.test',
    ready: true,
    request: async (options) => { calls.push(options); return response; },
  });
  return { client, calls };
}

test('秒杀时段、当前活动与分页只走商城路径', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, data: [
    { id: 1, name: '早场', startTime: '09:00:00', endTime: '10:00:00', sliderPicUrls: [] },
  ] } });
  assert.deepEqual(await client.configs(), [
    { id: '1', name: '早场', startTime: '09:00:00', endTime: '10:00:00', sliderPicUrls: [] },
  ]);
  assert.equal(calls[0].url, 'https://example.test/mall/app/seckill/configs');
  assert.equal(calls[0].method, 'GET');

  const second = setup({ statusCode: 200, data: { code: 200, rows: [{
    id: '2', name: '限时', spuId: '3', spuName: '商品', picUrl: null,
    marketPrice: '10.00', status: 0, stock: 2, totalStock: 5, seckillPrice: '8.00',
  }], total: 1 } });
  const page = await second.client.activities({ configId: '1', pageNum: 2 });
  assert.deepEqual(page.records[0].seckillPrice, '8.00');
  assert.equal(page.total, 1);
  assert.deepEqual(second.calls[0].data, { configId: '1', pageNum: 2, pageSize: 20 });
  await assert.rejects(() => second.client.activities({ pageNum: 0 }));
});

test('当前活动与详情严格解码价格和编号', async () => {
  const { client } = setup({ statusCode: 200, data: { code: 200, data: {
    config: { id: '1', name: '早场', startTime: '09:00:00', endTime: '10:00:00', sliderPicUrls: [] },
    activities: [],
  } } });
  assert.deepEqual(await client.now(), {
    config: { id: '1', name: '早场', startTime: '09:00:00', endTime: '10:00:00', sliderPicUrls: [] },
    activities: [],
  });

  const detail = setup({ statusCode: 200, data: { code: 200, data: {
    id: '7', name: '限时', status: 0, startTime: '2026-11-01 09:00:00',
    endTime: '2026-11-01 10:00:00', spuId: '3', totalLimitCount: 10, singleLimitCount: 5,
    stock: 2, totalStock: 5, products: [{ skuId: '9', seckillPrice: '8.00', stock: 2 }],
  } } });
  const value = await detail.client.activity('7');
  assert.equal(value.products[0].seckillPrice, '8.00');
  await assert.rejects(() => detail.client.activity('8'), /编号不一致/);

  const bad = setup({ statusCode: 200, data: { code: 200, data: {
    id: '7', name: '限时', status: 0, startTime: '2026-11-01 09:00:00',
    endTime: '2026-11-01 10:00:00', spuId: '3', totalLimitCount: 10, singleLimitCount: 5,
    stock: 2, totalStock: 5, products: [{ skuId: '9', seckillPrice: '8', stock: 2 }],
  } } });
  await assert.rejects(() => bad.client.activity('7'), /金额/);
});

test('未启用服务不发起请求', async () => {
  const calls = [];
  const client = createSeckillClient({
    baseUrl: 'https://example.test', ready: false,
    request: async (options) => { calls.push(options); return {}; },
  });
  await assert.rejects(() => client.configs(), /暂未启用/);
  assert.equal(calls.length, 0);
});
