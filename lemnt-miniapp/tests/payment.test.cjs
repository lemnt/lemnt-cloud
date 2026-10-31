const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createTradeClient } = require('../services/trade');
const { decodeChannels, decodePrepay } = require('../services/payment');

function setup(response) {
  const calls = [];
  const client = createTradeClient({
    baseUrl: 'https://example.test', ready: true, getMemberToken: () => 'member-token',
    request: async options => { calls.push(options); return response; },
  });
  return { client, calls };
}

const ok = data => ({ statusCode: 200, data: { code: 200, data } });

test('收银台渠道、预支付与沙箱支付走交易路径', async () => {
  const { client, calls } = setup(ok([{ code: 'SANDBOX', name: '沙箱' }]));
  assert.deepEqual(await client.paymentChannels('lemnt-mall'), [{ code: 'SANDBOX', name: '沙箱' }]);
  assert.equal(calls[0].url, 'https://example.test/trade/app/payment-channels');
  assert.deepEqual(calls[0].data, { appKey: 'lemnt-mall' });
  const prepayResponse = {
    paymentNo: 'PAY1D1667178F2041E1BFD70474', orderNo: 'TR1', channel: 'SANDBOX',
    amount: '8.00', payUrl: null, params: { paymentNo: 'PAY1D1667178F2041E1BFD70474', appKey: 'lemnt-mall' },
  };
  const { client: payClient, calls: payCalls } = setup(ok(prepayResponse));
  const raw = await payClient.prepay({ orderNo: 'TR1', appKey: 'lemnt-mall', channel: 'SANDBOX' });
  assert.equal(payCalls[0].url, 'https://example.test/trade/app/orders/TR1/payment');
  assert.deepEqual(payCalls[0].data, { appKey: 'lemnt-mall', channel: 'SANDBOX' });
  const prepay = decodePrepay(raw, 'TR1');
  assert.equal(prepay.channelLabel, '沙箱');
  assert.equal(prepay.payUrl, '');
  const { client: sandboxClient, calls: sandboxCalls } = setup(ok('PAY1D1667178F2041E1BFD70474'));
  await sandboxClient.sandboxPay(prepay.paymentNo);
  assert.equal(sandboxCalls[0].url,
    'https://example.test/trade/app/payments/PAY1D1667178F2041E1BFD70474/sandbox-pay');
});

test('支付响应与渠道参数不合法时拒绝', async () => {
  const { client } = setup(ok([]));
  assert.throws(() => client.paymentChannels(''));
  assert.throws(() => client.prepay({ orderNo: 'TR1', appKey: 'lemnt-mall', channel: 'CASH' }));
  assert.throws(() => client.sandboxPay('bad'));
  assert.throws(() => decodeChannels([{ code: 'SANDBOX' }, { code: 'SANDBOX' }]));
  assert.throws(() => decodeChannels([{ code: 'CASH' }]));
  assert.throws(() => decodePrepay({
    paymentNo: 'PAY1', orderNo: 'TR2', channel: 'SANDBOX', amount: '8.00', params: {},
  }, 'TR1'));
  assert.throws(() => decodePrepay({
    paymentNo: 'PAY1', orderNo: 'TR1', channel: 'SANDBOX', amount: '8.0', params: {},
  }, 'TR1'));
  assert.throws(() => decodePrepay({
    paymentNo: 'PAY1', orderNo: 'TR1', channel: 'SANDBOX', amount: '8.00', params: { secret: 1 },
  }, 'TR1'));
});
