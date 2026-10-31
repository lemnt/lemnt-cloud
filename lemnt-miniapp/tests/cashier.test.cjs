const { test } = require('node:test');
const assert = require('node:assert/strict');

let definition;
function load() {
  delete require.cache[require.resolve('../pages/cashier/index')];
  global.Page = value => { definition = value; };
  require('../pages/cashier/index');
  return definition;
}
function session() { return { memberId: '1', accessToken: 'token' }; }
function setupGlobals() {
  global.getApp = () => ({ globalData: { memberSession: session() } });
  global.wx = { navigateTo() {}, showToast() {} };
}
function makePage(client) {
  setupGlobals();
  const page = { ...load() };
  page.data = { ...JSON.parse(JSON.stringify(page.data)), loading: false, orderNo: 'TR1', appKey: 'lemnt-mall' };
  page.setData = function (value) { Object.assign(this.data, value); };
  page.client = client;
  return page;
}
const prepay = {
  paymentNo: 'PAY1D1667178F2041E1BFD70474', orderNo: 'TR1', channel: 'SANDBOX',
  amount: '8.00', payUrl: null, params: { paymentNo: 'PAY1D1667178F2041E1BFD70474' },
};
const status = localPaymentStatus => ({
  orderNo: 'TR1', orderStatus: localPaymentStatus === 'SUCCESS' ? 'PAID' : 'PENDING_PAYMENT',
  payableAmount: '8.00', localPaymentStatus, channelVerified: false,
});

test('收银台加载渠道、创建支付单并模拟沙箱支付', async () => {
  const submitted = [];
  const page = makePage({
    paymentChannels: async () => [{ code: 'SANDBOX', name: '沙箱' }],
    prepay: async input => { submitted.push(input); return prepay; },
    sandboxPay: async paymentNo => { submitted.push(paymentNo); return paymentNo; },
    paymentStatus: async () => status(page.data.prepay ? 'SUCCESS' : 'WAITING'),
  });
  await page.loadChannels();
  assert.equal(page.data.channel, 'SANDBOX');
  await page.pay();
  assert.deepEqual(submitted[0], { orderNo: 'TR1', appKey: 'lemnt-mall', channel: 'SANDBOX' });
  assert.equal(page.data.prepay.paymentNo, prepay.paymentNo);
  assert.match(page.data.message, /模拟支付/);
  await page.sandboxPay();
  assert.equal(submitted[1], prepay.paymentNo);
  assert.equal(page.data.paid, true);
  assert.equal(page.data.message, '支付成功');
});

test('收银台拒绝非法渠道参数与非法响应', async () => {
  const page = makePage({
    paymentChannels: async () => [{ code: 'CASH', name: '现金' }],
    prepay: async input => {
      if (input.channel === 'CASH') throw new Error('支付渠道不合法');
      return { ...prepay, orderNo: 'TR2' };
    },
    paymentStatus: async () => status('WAITING'),
  });
  await page.loadChannels().catch(() => {});
  page.data.channel = 'SANDBOX';
  await page.pay();
  assert.match(page.data.error, /支付单响应不合法/);
  page.data.channel = 'CASH';
  await page.pay();
  assert.match(page.data.error, /支付渠道不合法/);
});
