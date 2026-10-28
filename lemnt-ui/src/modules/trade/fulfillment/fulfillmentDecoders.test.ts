import { expect, it } from 'vitest';
import { decodePage, decodeOrder, decodeDetail, decodeRefund } from './fulfillmentDecoders';
import { canShip, canRequestRefund } from './constants';
const order = {
  orderNo: 'TR1',
  memberId: '9223372036854775807',
  totalAmount: '17.00',
  status: 'PAID',
  inventoryConfirmed: false,
  activeRefund: false,
  createTime: '2026-10-25 10:00:00',
};
it('支付与库存确认是独立门禁，待处理退款阻止发货', () => {
  const row = decodeOrder(order);
  expect(canShip(row)).toBe(false);
  expect(canRequestRefund(row)).toBe(true);
  expect(canShip({ ...row, inventoryConfirmed: true })).toBe(true);
  expect(canShip({ ...row, inventoryConfirmed: true, activeRefund: true })).toBe(false);
  expect(canRequestRefund({ ...row, status: 'SHIPPED' })).toBe(false);
});
it('精确金额和长ID保持字符串，已发货与完成状态可读取', () => {
  expect(decodeOrder({ ...order, status: 'SHIPPED' }).memberId).toBe(order.memberId);
  expect(decodeDetail({ ...order, status: 'COMPLETED', items: [], shipment: null }).status).toBe(
    'COMPLETED',
  );
  expect(() => decodeOrder({ ...order, totalAmount: 17 })).toThrow();
  expect(() => decodeOrder({ ...order, memberId: Number('9223372036854775807') })).toThrow();
  const refunded = decodeOrder({ ...order, status: 'REFUNDED' });
  expect(refunded.status).toBe('REFUNDED');
  expect(canShip(refunded)).toBe(false);
  expect(canRequestRefund(refunded)).toBe(false);
  expect(() => decodeOrder({ ...order, status: 'UNKNOWN' })).toThrow();
});
it('分页及退款状态不能将未知值误作已退款', () => {
  expect(() => decodePage({ rows: [], total: -1 }, decodeOrder)).toThrow();
  expect(() => decodePage({ rows: null, total: 0 }, decodeOrder)).toThrow();
  const r = {
    refundNo: 'RF1',
    orderNo: 'TR1',
    requestNo: 'R1',
    memberId: 1,
    paymentNo: 'P1',
    amount: '17.00',
    reason: '申请',
    status: 'REQUESTED',
    requestedAt: '2026-10-25 10:00:00',
  };
  expect(decodeRefund(r).status).toBe('REQUESTED');
  expect(decodeRefund({ ...r, status: 'REJECTED' }).status).toBe('REJECTED');
  expect(decodeRefund({ ...r, status: 'REFUNDING' }).status).toBe('REFUNDING');
  expect(decodeRefund({ ...r, status: 'REFUNDED' }).status).toBe('REFUNDED');
  expect(() => decodeRefund({ ...r, status: 'SUCCESS' })).toThrow();
});

it('订单日志保留系统编号0与源状态，并拒绝冒充系统或失真用户编号', async () => {
  const { decodeOrderLog } = await import('./fulfillmentDecoders');
  const source = {
    id: '1',
    userId: 0,
    userType: 0,
    orderId: '2',
    beforeStatus: 0,
    afterStatus: 40,
    operateType: 41,
    content: '系统取消订单',
    createTime: '2026-10-28 12:00:00',
  };
  expect(decodeOrderLog(source).userId).toBe('0');
  expect(decodeOrderLog({ ...source, userId: '9223372036854775807', userType: 2 }).userId).toBe(
    '9223372036854775807',
  );
  expect(() => decodeOrderLog({ ...source, userId: 1 })).toThrow();
  expect(() => decodeOrderLog({ ...source, userType: 1 })).toThrow();
  expect(() => decodeOrderLog({ ...source, afterStatus: 99 })).toThrow();
});

it('售后日志独立使用退款和取消状态，数值主键不能以业务单号替代', async () => {
  const { decodeAfterSaleLog } = await import('./fulfillmentDecoders');
  const row = {
    id: '7',
    afterSaleId: '2',
    userId: 0,
    userType: 0,
    beforeStatus: 40,
    afterStatus: 50,
    operateType: 31,
    content: '退款成功',
    createTime: '2026-10-28 12:00:00',
  };
  expect(decodeAfterSaleLog(row).afterStatus).toBe(50);
  expect(
    decodeAfterSaleLog({ ...row, userId: '1', userType: 1, afterStatus: 61 }).afterSaleId,
  ).toBe('2');
  expect(() => decodeAfterSaleLog({ ...row, afterSaleId: 'RF1' })).toThrow();
  expect(() => decodeAfterSaleLog({ ...row, afterStatus: 0 })).toThrow();
});

it('订单商品快照保留长编号、规格和图片，未知历史不能变成空规格', () => {
  const item = {
    productId: '7',
    productCode: 'SKU',
    productName: '下单商品',
    unitPrice: '17.00',
    quantity: 1,
    lineAmount: '17.00',
  };
  const read = (extra: Record<string, unknown>) =>
    decodeDetail({ ...order, items: [{ ...item, ...extra }] }).items[0];
  expect(read({}).properties).toBeNull();
  expect(read({ properties: {} }).properties).toEqual({});
  const row = read({
    spuId: '9223372036854775807',
    spuName: '原商品',
    properties: { 颜色: '黑色' },
    picUrl: 'https://example.com/old.png',
  });
  expect(row.spuId).toBe('9223372036854775807');
  expect(row.properties).toEqual({ 颜色: '黑色' });
  expect(row.picUrl).toBe('https://example.com/old.png');
  expect(() => read({ properties: { 颜色: 1 } })).toThrow();
  expect(() => read({ properties: [] })).toThrow();
});
