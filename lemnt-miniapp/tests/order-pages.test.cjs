const { test } = require('node:test');
const assert = require('node:assert/strict');

let definition;
function load(modulePath) { delete require.cache[require.resolve(modulePath)]; global.Page = value => { definition = value; }; require(modulePath); return definition; }
function session() { return { memberId: '1', accessToken: 'token' }; }
function setupGlobals(modalConfirm = true) {
  global.getApp = () => ({ globalData: { memberSession: session() } });
  global.wx = {
    navigateTo() {}, redirectTo() {}, showToast() {},
    showModal: options => options.success({ confirm: modalConfirm }),
  };
}
function makePage(modulePath, client, data = {}) {
  setupGlobals();
  const page = { ...load(modulePath) };
  page.data = { ...JSON.parse(JSON.stringify(page.data || {})), ...data, loading: false };
  page.setData = function (value) { Object.assign(this.data, value); };
  page.client = client;
  page.onSessionLost = page.onSessionLost || function () { this.setData({ error: '会员已切换' }); };
  return page;
}

test('订单记录按状态计算可操作项并拒绝非法状态', () => {
  const page = makePage('../pages/order-list/index', {});
  const pending = page.toRecord({ orderNo: 'TR1', status: 'PENDING_PAYMENT', totalAmount: '10.00', items: [] });
  assert.equal(pending.canCancel, true);
  assert.equal(pending.statusLabel, '待付款');
  const shipped = page.toRecord({ orderNo: 'TR2', status: 'SHIPPED', totalAmount: '10.00', items: [] });
  assert.equal(shipped.canReceive, true);
  const comment = page.toRecord({ orderNo: 'TR3', status: 'COMPLETED', commentStatus: false, totalAmount: '10.00', items: [] });
  assert.equal(comment.canComment, true);
  const done = page.toRecord({ orderNo: 'TR4', status: 'COMPLETED', commentStatus: true, totalAmount: '10.00', items: [] });
  assert.equal(done.canComment, false);
  assert.throws(() => page.toRecord({ orderNo: 'TR5', status: 'UNKNOWN', totalAmount: '10.00', items: [] }));
});

test('订单状态数量按分组汇总并在缺省时补零', async () => {
  const page = makePage('../pages/order-list/index', {
    orderStatusCounts: async () => ({ pendingPayment: 1, awaitingShipment: 2, awaitingReceipt: 3, awaitingComment: 4, completed: 5, closed: 6, refunded: 7 }),
  });
  await page.loadCounts();
  assert.deepEqual(page.data.counts, [28, 1, 2, 3, 4, 5, 6, 7]);
});

test('取消订单必须先确认，确认后刷新计数与列表', async () => {
  const calls = [];
  const page = makePage('../pages/order-list/index', {
    cancelOrder: async orderNo => { calls.push(orderNo); return true; },
    orderStatusCounts: async () => ({ pendingPayment: 0, awaitingShipment: 0, awaitingReceipt: 0, awaitingComment: 0, completed: 0, closed: 0, refunded: 0 }),
    orderPage: async () => ({ records: [], total: 0 }),
  });
  setupGlobals(false);
  await page.cancel({ currentTarget: { dataset: { order: 'TR1' } } });
  assert.equal(calls.length, 0);
  setupGlobals(true);
  await page.cancel({ currentTarget: { dataset: { order: 'TR1' } } });
  assert.deepEqual(calls, ['TR1']);
});

test('订单详情校验归属并计算操作项', () => {
  const page = makePage('../pages/order-detail/index', {});
  page.orderNo = 'TR1';
  const order = page.toOrder({ orderNo: 'TR1', status: 'SHIPPED', totalAmount: '10.00', items: [] });
  assert.equal(order.statusLabel, '待收货');
  assert.equal(order.canReceive, true);
  assert.throws(() => page.toOrder({ orderNo: 'TR2', status: 'SHIPPED', totalAmount: '10.00', items: [] }));
});

test('售后申请选择首个可退商品并在重试时复用请求编号', async () => {
  setupGlobals();
  const page = makePage('../pages/after-sale-create/index', {
    afterSaleItems: async () => [
      { orderItemId: 10, refundableQuantity: 0, refundableAmount: '0.00', productName: '已退完' },
      { orderItemId: 11, refundableQuantity: 2, refundableAmount: '20.00', productName: '可退' },
    ],
  });
  page.orderNo = 'TR1';
  await page.reload();
  assert.equal(page.data.selectedIndex, 1);
  assert.equal(page.data.quantity, 1);
  const payloads = [];
  let attempt = 0;
  page.client.createAfterSale = async (orderNo, input) => { payloads.push({ orderNo, input }); if (attempt++ === 0) throw new Error('网络未确认'); return { refundNo: 'RF1' }; };
  page.data.reason = '质量问题';
  await page.submit();
  await page.submit();
  assert.equal(payloads.length, 2);
  assert.equal(payloads[0].input.requestNo, payloads[1].input.requestNo);
  assert.equal(payloads[1].input.type, 'REFUND_ONLY');
  assert.equal(payloads[1].orderNo, 'TR1');
  assert.equal(payloads[1].input.memberId, undefined);
  assert.equal(payloads[1].input.amount, undefined);
});

test('售后详情按状态与方式给出可取消、可登记退货', () => {
  const page = makePage('../pages/after-sale-detail/index', {});
  page.no = 'RF1';
  const awaiting = page.toSale({ refundNo: 'RF1', status: 'AWAITING_RETURN', type: 'RETURN_REFUND', amount: '10.00' });
  assert.equal(awaiting.canReturn, true);
  assert.equal(awaiting.canCancel, true);
  const refunding = page.toSale({ refundNo: 'RF1', status: 'REFUNDING', type: 'REFUND_ONLY', amount: '10.00' });
  assert.equal(refunding.canCancel, false);
  assert.throws(() => page.toSale({ refundNo: 'RF2', status: 'AWAITING_RETURN', type: 'RETURN_REFUND', amount: '10.00' }));
});
