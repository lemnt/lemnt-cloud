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
  recipientName?: string;
  phone?: string;
  address?: string;
}
export interface Shipment extends ShipmentRequest {
  recipientName: string;
  phone: string;
  address: string;
  orderNo: string;
  shippedAt: string;
  receivedAt: string | null;
}
export interface OrderDetail extends Pick<
  OrderSummary,
  'orderNo' | 'memberId' | 'totalAmount' | 'status' | 'inventoryConfirmed' | 'activeRefund'
> {
  items: CheckoutItem[];
  commentStatus?: boolean | null;
  finishTime?: string | null;
  shipment: Shipment | null;
}
export type RefundStatus =
  | 'REQUESTED'
  | 'REJECTED'
  | 'AWAITING_RETURN'
  | 'RETURNED'
  | 'WAIT_REFUND'
  | 'RETURN_REJECTED'
  | 'REFUNDING'
  | 'REFUNDED'
  | 'CANCELLED';
export interface RefundProperty {
  propertyId: string | null;
  propertyName: string;
  valueId: string | null;
  valueName: string;
}
export interface RefundRequest {
  skuId?: string | null;
  spuId?: string | null;
  spuName?: string | null;
  properties?: RefundProperty[] | null;
  picUrl?: string | null;
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
  id?: string | null;
  orderId?: string | null;
  orderItemId?: string | null;
  quantity?: number | null;
  type?: 'REFUND_ONLY' | 'RETURN_REFUND' | null;
  returnCarrierCode?: string | null;
  returnTrackingNo?: string | null;
  returnShippedAt?: string | null;
  returnReceivedAt?: string | null;
  refundTime?: string | null;
}
export interface RefundCreateRequest {
  requestNo: string;
  reason: string;
}

/** 订单操作日志，状态编号与源项目一致。 */
export interface OrderLog {
  id: string;
  userId: string;
  userType: number;
  orderId: string;
  beforeStatus: number | null;
  afterStatus: number;
  operateType: number;
  content: string;
  createTime: string;
}

/** 售后日志使用独立关联主键与源状态。 */
export interface AfterSaleLog {
  id: string;
  userId: string;
  userType: number;
  afterSaleId: string;
  beforeStatus: number | null;
  afterStatus: number;
  operateType: number;
  content: string;
  createTime: string;
}
