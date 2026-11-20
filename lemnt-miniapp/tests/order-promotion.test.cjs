const { test } = require('node:test');
const assert = require('node:assert/strict');
const { orderPromotion, afterSalePromotion } = require('../services/orderPromotion');
test('订单优惠历史未知不补零，保留调价和逐行归因', () => {
  const value = orderPromotion(
    {
      orderNo: 'T1',
      snapshotStatus: 'LEGACY_PARTIAL',
      summary: { discountAmount: null, deliveryDiscountPrice: null, adjustmentAmount: '-1.00' },
      promotions: [
        {
          promotionType: 'COUPON',
          promotionName: '优惠券',
          discountAmount: '2.00',
          deliveryDiscountPrice: '0.00',
          usePoint: 0,
          givePoint: 0,
          items: [
            {
              orderItemId: 11,
              discountAmount: '2.00',
              deliveryDiscountPrice: '0.00',
              usePoint: 0,
              givePoint: 0,
            },
          ],
        },
      ],
    },
    'T1',
  );
  assert.equal(value.summary.discountAmount, null);
  assert.equal(value.summary.adjustmentAmount, '-1.00');
  assert.equal(value.promotions[0].items[0].discountAmount, '2.00');
});
test('退单计划积分不伪装已到账，金额与优惠分别展示', () => {
  const value = afterSalePromotion(
    {
      afterSaleNo: 'RF1',
      snapshotStatus: 'COMPLETE',
      amount: '40.00',
      discountAmount: '10.00',
      deliveryDiscountPrice: '0.00',
      returnUsePoint: '9223372036854775807',
      deductGivePoint: 0,
      benefitStatus: 'PENDING',
      promotions: [],
    },
    'RF1',
  );
  assert.equal(value.amount, '40.00');
  assert.equal(value.discountAmount, '10.00');
  assert.equal(value.benefitStatusLabel, '处理中');
  assert.equal(value.returnUsePoint, '9223372036854775807');
});
test('拒绝错单、未知状态与浮点金额', () => {
  assert.throws(() =>
    orderPromotion(
      { orderNo: 'OTHER', snapshotStatus: 'COMPLETE', summary: {}, promotions: [] },
      'T1',
    ),
  );
  assert.throws(() =>
    afterSalePromotion(
      { afterSaleNo: 'RF1', snapshotStatus: 'COMPLETE', benefitStatus: 'DONE', promotions: [] },
      'RF1',
    ),
  );
  assert.throws(() =>
    orderPromotion(
      {
        orderNo: 'T1',
        snapshotStatus: 'COMPLETE',
        summary: { discountAmount: 1.5 },
        promotions: [],
      },
      'T1',
    ),
  );
});
