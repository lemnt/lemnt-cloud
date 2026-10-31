import { expect, it } from 'vitest';
import { decodeCart, decodeQuote, decodeReceipt, positiveId } from './checkoutDecoders';
it('保留金额字符串与大编号精度，并显式显示不可售商品', () => {
  expect(positiveId('9223372036854775807')).toBe('9223372036854775807');
  expect(() => positiveId(9007199254740992)).toThrow();
  expect(() => positiveId('9223372036854775808')).toThrow();
  const row = decodeCart({
    id: 1,
    productId: 5,
    quantity: 2,
    selected: false,
    version: 0,
    sellable: false,
  });
  expect(row.unitPrice).toBeNull();
  expect(row.sellable).toBe(false);
  expect(row.version).toBe('0');
});
it('报价必须含令牌；订单状态与金额不得被未知字段或数值金额替代', () => {
  const quote = {
    items: [],
    originalAmount: '20.00',
    discountAmount: '3.00',
    payableAmount: '17.00',
    quoteToken: 'a'.repeat(64),
  };
  expect(decodeQuote(quote).payableAmount).toBe('17.00');
  expect(() => decodeQuote({ ...quote, payableAmount: 17 })).toThrow();
  expect(() => decodeQuote({ ...quote, quoteToken: undefined })).toThrow();
  expect(decodeReceipt({ ...quote, orderNo: 'TR1', memberId: 1, status: 'RESERVING' }).status).toBe(
    'RESERVING',
  );
  expect(() =>
    decodeReceipt({ ...quote, orderNo: 'TR1', memberId: 1, status: 'UNKNOWN' }),
  ).toThrow();
});

it('结算回执能读取履约后的发货和完成状态', () => {
  const receipt = {
    items: [],
    originalAmount: '20.00',
    discountAmount: '0.00',
    payableAmount: '20.00',
    orderNo: 'TR1',
    memberId: 1,
  };
  expect(decodeReceipt({ ...receipt, status: 'SHIPPED' }).status).toBe('SHIPPED');
  expect(decodeReceipt({ ...receipt, status: 'COMPLETED' }).status).toBe('COMPLETED');
});
it('会员回执保留订单项主键与已评标记，报价不生成这些事实', () => {
  const input = {
    orderNo: 'TR1',
    memberId: 1,
    status: 'COMPLETED',
    originalAmount: '10.00',
    discountAmount: '0.00',
    payableAmount: '10.00',
    couponId: null,
    items: [
      {
        productId: 7,
        productCode: 'SKU',
        productName: '商品',
        unitPrice: '10.00',
        quantity: 1,
        lineAmount: '10.00',
        orderItemId: '9223372036854775807',
        commentStatus: true,
      },
    ],
    commentStatus: true,
    finishTime: '2026-10-29 10:00:00',
  };
  const value = decodeReceipt(input);
  expect(value.commentStatus).toBe(true);
  expect(value.items[0].orderItemId).toBe('9223372036854775807');
  expect(value.items[0].commentStatus).toBe(true);
  const old = decodeReceipt({
    ...input,
    commentStatus: undefined,
    finishTime: undefined,
    items: [{ ...input.items[0], orderItemId: undefined, commentStatus: undefined }],
  });
  expect(old.commentStatus).toBeUndefined();
  expect(old.items[0].orderItemId).toBeUndefined();
  expect(() => decodeReceipt({ ...input, commentStatus: 'true' })).toThrow();
});
