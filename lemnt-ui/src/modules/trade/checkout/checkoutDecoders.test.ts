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
