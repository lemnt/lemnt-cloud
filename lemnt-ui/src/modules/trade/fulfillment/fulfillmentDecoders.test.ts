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
  expect(() => decodeOrder({ ...order, status: 'REFUNDED' })).toThrow();
});
it('分页及退款状态不能将未知值误作已退款', () => {
  expect(() => decodePage({ records: [], total: -1 }, decodeOrder)).toThrow();
  expect(() => decodePage({ records: null, total: 0 }, decodeOrder)).toThrow();
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
  expect(() => decodeRefund({ ...r, status: 'SUCCESS' })).toThrow();
});
