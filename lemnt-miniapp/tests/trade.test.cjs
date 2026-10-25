const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createTradeClient } = require('../services/trade');
function setup(extra = {}) {
  const calls = [];
  const client = createTradeClient({ baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token', request: async options => { calls.push(options); return { statusCode: 200, data: { code: 200, data: [] } }; }, ...extra });
  return { client, calls };
}
test('默认关闭；没有会员登录态时不请求网络', async () => {
  for (const options of [{ ready: false }, { getMemberToken: () => null }]) {
    const { client, calls } = setup(options); await assert.rejects(client.cart()); assert.equal(calls.length, 0);
  }
});
test('只调用用户端路径，忽略伪造会员和金额字段', async () => {
  const { client, calls } = setup();
  await client.saveCart('5', { quantity: 2, selected: false, memberId: '999', price: '0.01' });
  assert.equal(calls[0].url, 'https://example.test/trade/app/cart/5');
  assert.deepEqual(calls[0].data, { quantity: 2, selected: false });
  assert.equal(calls[0].header.Authorization, 'Bearer member-token');
});
test('结算重试保留原编号和令牌，不发送客户端金额', async () => {
  const { client, calls } = setup();
  const input = { productIds: ['5'], requestNo: 'R1', settlementId: '12345678-1234-1234-1234-123456789abc', memberId: '999', payableAmount: '0.01' };
  await client.checkout(input); await client.checkout(input);
  assert.deepEqual(calls[0].data, calls[1].data);
  assert.deepEqual(Object.keys(calls[0].data).sort(), ['requestNo', 'settlementId']);
});
test('预支付只发送订单编号；支付结果从服务端查询', async () => {
  const { client, calls } = setup(); await client.prepay('TR1'); await client.paymentStatus('TR1');
  assert.equal(calls[0].url, 'https://example.test/trade/app/orders/TR1/prepay');
  assert.equal(calls[0].data, undefined); assert.equal(calls[1].method, 'GET');
});
test('参数、HTTP错误和业务错误不能当作成功', async () => {
  const { client } = setup(); assert.throws(() => client.saveCart('5', { quantity: 0, selected: true }));
  assert.throws(() => client.checkout({}));
  for (const response of [{ statusCode: 401 }, { statusCode: 200, data: { code: 500 } }]) {
    await assert.rejects(setup({ request: async () => response }).client.cart());
  }
});
