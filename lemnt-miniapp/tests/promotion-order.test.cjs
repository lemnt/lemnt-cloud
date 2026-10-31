const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createSeckillClient } = require('../services/seckill');
const { createCombinationClient } = require('../services/combination');
const { createBargainClient } = require('../services/bargain');
const { createPointClient } = require('../services/point');

function setup(factory, response) {
  const calls = [];
  const client = factory({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async (options) => { calls.push(options); return response; },
  });
  return { client, calls };
}

const ok = { statusCode: 200, data: { code: 200, data: {
  orderNo: 'TR1', totalAmount: '10.00', status: 'PENDING_PAYMENT', usePoint: 0, givePoint: 0,
} } };

test('四类活动下单走各自路径并携带会员令牌', async () => {
  const cases = [
    [createSeckillClient, { activityId: '1', skuId: '2', quantity: 1, addressId: '3', requestNo: 'REQ1' },
      'https://example.test/mall/app/seckill/orders'],
    [createCombinationClient, { activityId: '1', headId: '9', skuId: '2', quantity: 1, addressId: '3', requestNo: 'REQ1' },
      'https://example.test/mall/app/combination/orders'],
    [createBargainClient, { recordId: '8', skuId: '2', quantity: 1, addressId: '3', requestNo: 'REQ1' },
      'https://example.test/mall/app/bargain/orders'],
    [createPointClient, { activityId: '1', skuId: '2', quantity: 1, addressId: '3', requestNo: 'REQ1' },
      'https://example.test/mall/app/point-mall/orders'],
    [createPointClient, { activityId: '1', lines: [{ skuId: '2', quantity: 1 }, { skuId: '4', quantity: 2 }], addressId: '3', requestNo: 'REQ2' },
      'https://example.test/mall/app/point-mall/orders/batch'],
  ];
  for (const [factory, payload, url] of cases) {
    const { client, calls } = setup(factory, ok);
    const order = payload.lines ? await client.batchOrder(payload) : await client.order(payload);
    assert.equal(order.orderNo, 'TR1');
    assert.equal(order.totalAmount, '10.00');
    assert.equal(calls[0].url, url);
    assert.equal(calls[0].method, 'POST');
    assert.equal(calls[0].header.Authorization, 'Bearer member-token');
  }
});

test('下单请求编号和数量不合法时不请求', async () => {
  const { client, calls } = setup(createSeckillClient, ok);
  await assert.rejects(() => client.order({ activityId: '1', skuId: '2', quantity: 1, requestNo: 'bad!' }), /请求编号/);
  await assert.rejects(() => client.order({ activityId: '1', skuId: '2', quantity: 0, requestNo: 'REQ1' }), /数量/);
  assert.equal(calls.length, 0);
});
