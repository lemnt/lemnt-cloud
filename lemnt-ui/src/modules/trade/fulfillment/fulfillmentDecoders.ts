import { positiveId } from '../checkout/checkoutDecoders';
import type { CheckoutStatus, CheckoutItem } from '../checkout/types';
import type { Page, OrderSummary, OrderDetail, Shipment, RefundRequest } from './types';
function object(v: unknown): Record<string, unknown> {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw new Error('履约接口对象格式不合法');
  return v as Record<string, unknown>;
}
function text(v: unknown): string {
  if (typeof v !== 'string') throw new Error('履约接口文本格式不合法');
  return v;
}
function optional(v: unknown) {
  return v == null ? null : text(v);
}
function bool(v: unknown): boolean {
  if (typeof v !== 'boolean') throw new Error('履约接口布尔格式不合法');
  return v;
}
function money(v: unknown): string {
  const s = text(v);
  if (!/^\d+\.\d{2}$/.test(s)) throw new Error('金额须为两位小数字符串');
  return s;
}
function integer(v: unknown): number {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0)
    throw new Error('履约接口整数格式不合法');
  return v;
}
function state(v: unknown): CheckoutStatus {
  if (
    v !== 'RESERVING' &&
    v !== 'PENDING_PAYMENT' &&
    v !== 'PAID' &&
    v !== 'SHIPPED' &&
    v !== 'COMPLETED' &&
    v !== 'CLOSED'
  )
    throw new Error('未知订单状态');
  return v;
}
export function decodePage<T>(v: unknown, decode: (v: unknown) => T): Page<T> {
  const r = object(v);
  if (!Array.isArray(r.records)) throw new Error('缺少分页列表');
  return { records: r.records.map(decode), total: integer(r.total) };
}
function common(r: Record<string, unknown>) {
  return {
    orderNo: text(r.orderNo),
    memberId: positiveId(r.memberId),
    totalAmount: money(r.totalAmount),
    status: state(r.status),
    inventoryConfirmed: bool(r.inventoryConfirmed),
    activeRefund: bool(r.activeRefund),
  };
}
export function decodeOrder(v: unknown): OrderSummary {
  const r = object(v);
  return {
    ...common(r),
    createTime: text(r.createTime),
    carrierCode: optional(r.carrierCode),
    trackingNo: optional(r.trackingNo),
    shippedAt: optional(r.shippedAt),
    receivedAt: optional(r.receivedAt),
  };
}
export function decodeShipment(v: unknown): Shipment {
  const r = object(v);
  return {
    orderNo: text(r.orderNo),
    requestNo: text(r.requestNo),
    carrierCode: text(r.carrierCode),
    trackingNo: text(r.trackingNo),
    recipientName: text(r.recipientName),
    phone: text(r.phone),
    address: text(r.address),
    shippedAt: text(r.shippedAt),
    receivedAt: optional(r.receivedAt),
  };
}
function item(v: unknown): CheckoutItem {
  const r = object(v);
  return {
    productId: positiveId(r.productId),
    productCode: text(r.productCode),
    productName: text(r.productName),
    unitPrice: money(r.unitPrice),
    quantity: integer(r.quantity),
    lineAmount: money(r.lineAmount),
  };
}
export function decodeDetail(v: unknown): OrderDetail {
  const r = object(v);
  if (!Array.isArray(r.items)) throw new Error('缺少订单明细');
  return {
    ...common(r),
    items: r.items.map(item),
    shipment: r.shipment == null ? null : decodeShipment(r.shipment),
  };
}
export function decodeRefund(v: unknown): RefundRequest {
  const r = object(v);
  if (r.status !== 'REQUESTED' && r.status !== 'REJECTED') throw new Error('未知退款申请状态');
  return {
    refundNo: text(r.refundNo),
    orderNo: text(r.orderNo),
    requestNo: text(r.requestNo),
    memberId: positiveId(r.memberId),
    paymentNo: text(r.paymentNo),
    amount: money(r.amount),
    reason: text(r.reason),
    status: r.status,
    decisionReason: optional(r.decisionReason),
    requestedAt: text(r.requestedAt),
    decidedAt: optional(r.decidedAt),
  };
}
