import type { CheckoutStatus, CheckoutItem } from '../../mall/checkout/types';
export interface Page<T> {
  records: T[];
  total: number;
}
export interface PageQuery {
  orderNo?: string;
  status?: string;
  pageNum: number;
  pageSize: number;
}
export interface OrderSummary {
  orderNo: string;
  memberId: string;
  totalAmount: string;
  status: CheckoutStatus;
  createTime: string;
  inventoryConfirmed: boolean;
  activeRefund: boolean;
  carrierCode: string | null;
  trackingNo: string | null;
  shippedAt: string | null;
  receivedAt: string | null;
}
export interface ShipmentRequest {
  requestNo: string;
  carrierCode: string;
  trackingNo: string;
  recipientName: string;
  phone: string;
  address: string;
}
export interface Shipment extends ShipmentRequest {
  orderNo: string;
  shippedAt: string;
  receivedAt: string | null;
}
export interface OrderDetail extends Pick<
  OrderSummary,
  'orderNo' | 'memberId' | 'totalAmount' | 'status' | 'inventoryConfirmed' | 'activeRefund'
> {
  items: CheckoutItem[];
  shipment: Shipment | null;
}
export type RefundStatus = 'REQUESTED' | 'REJECTED';
export interface RefundRequest {
  refundNo: string;
  orderNo: string;
  requestNo: string;
  memberId: string;
  paymentNo: string;
  amount: string;
  reason: string;
  status: RefundStatus;
  decisionReason: string | null;
  requestedAt: string;
  decidedAt: string | null;
}
export interface RefundCreateRequest {
  requestNo: string;
  reason: string;
}
