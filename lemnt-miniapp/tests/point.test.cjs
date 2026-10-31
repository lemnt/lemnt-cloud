const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createPointClient } = require('../services/point');

function setup(response) {
  const calls = [];
  const client = createPointClient({
    baseUrl: 'https://example.test', ready: true,
    request: async (options) => { calls.push(options); return response; },
  });
  return { client, calls };
}

test('积分商城分页与详情严格解码', async () => {
  const { client, calls } = setup({ statusCode: 200, data: { code: 200, rows: [{
    id: 1, spuId: 2, spuName: '商品', picUrl: null, status: 0, stock: 3, totalStock: 3,
    point: 100, price: '0.00', marketPrice: '10.00', products: [
      { skuId: 3, exchangeCount: 2, point: 100, price: '0.00', stock: 3 },
    ],
  }], total: 1 } });
  const page = await client.activities({ pageNum: 1 });
  assert.equal(page.records[0].price, '0.00');
  assert.equal(page.records[0].products[0].point, 100);
  assert.deepEqual(calls[0].data, { pageNum: 1, pageSize: 20 });
});

test('积分详情编号不一致拒绝', async () => {
  const { client } = setup({ statusCode: 200, data: { code: 200, data: {
    id: '9', spuId: '2', spuName: '商品', picUrl: null, status: 0, stock: 3, totalStock: 3,
    point: 100, price: '0.00', marketPrice: '10.00', products: [],
  } } });
  await assert.rejects(() => client.activity('8'), /编号不一致/);
});
