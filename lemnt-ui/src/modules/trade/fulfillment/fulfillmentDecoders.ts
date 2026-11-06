import type { OrderRefundableItem } from './types';
import { decodeSpecs, positiveId } from '../../mall/checkout/checkoutDecoders';
import type { CheckoutStatus, CheckoutItem } from '../../mall/checkout/types';
import type { Page, OrderSummary, OrderDetail, Shipment, RefundRequest } from './types';
import type {
  OrderPromotionDetail,
  AfterSalePromotionDetail,
  PricingSnapshotStatus,
  BenefitStatus,
} from './types';
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
/** 只接受实际保存的规格映射；历史缺失保持为空。 */
function productSnapshot(row: Record<string, unknown>) {
  return {
    spuId: row.spuId == null ? null : positiveId(row.spuId),
    spuName: optional(row.spuName),
    specs: decodeSpecs(row.specs),
    picUrl: optional(row.picUrl),
  };
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
    v !== 'CLOSED' &&
    v !== 'REFUNDED'
  )
    throw new Error('未知订单状态');
  return v;
}
export function decodePage<T>(v: unknown, decode: (v: unknown) => T): Page<T> {
  const r = object(v);
  if (!Array.isArray(r.rows)) throw new Error('缺少分页列表');
  return { records: r.rows.map(decode), total: integer(r.total) };
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
    ...productSnapshot(r),
    ...(r.orderItemId == null ? {} : { orderItemId: positiveId(r.orderItemId) }),
    ...(r.commentStatus == null ? {} : { commentStatus: bool(r.commentStatus) }),
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
    ...(r.commentStatus == null ? {} : { commentStatus: bool(r.commentStatus) }),
    ...(r.finishTime == null ? {} : { finishTime: text(r.finishTime) }),
    shipment: r.shipment == null ? null : decodeShipment(r.shipment),
  };
}
export function decodeRefund(v: unknown): RefundRequest {
  const r = object(v);
  if (
    r.status !== 'REQUESTED' &&
    r.status !== 'REJECTED' &&
    r.status !== 'AWAITING_RETURN' &&
    r.status !== 'RETURNED' &&
    r.status !== 'WAIT_REFUND' &&
    r.status !== 'RETURN_REJECTED' &&
    r.status !== 'REFUNDING' &&
    r.status !== 'REFUNDED' &&
    r.status !== 'CANCELLED'
  )
    throw new Error('未知退款申请状态');
  return {
    ...productSnapshot(r),
    skuId: r.skuId == null ? null : positiveId(r.skuId),
    refundNo: text(r.refundNo),
    orderNo: text(r.orderNo),
    requestNo: text(r.requestNo),
    memberId: positiveId(r.memberId),
    paymentNo: optional(r.paymentNo),
    amount: money(r.amount),
    reason: text(r.reason),
    status: r.status,
    decisionReason: optional(r.decisionReason),
    requestedAt: text(r.requestedAt),
    decidedAt: optional(r.decidedAt),
    id: r.id == null ? null : positiveId(r.id),
    orderId: r.orderId == null ? null : positiveId(r.orderId),
    orderItemId: r.orderItemId == null ? null : positiveId(r.orderItemId),
    quantity: r.quantity == null ? null : integer(r.quantity),
    type: r.type === 'REFUND_ONLY' || r.type === 'RETURN_REFUND' ? r.type : null,
    returnCarrierCode: optional(r.returnCarrierCode),
    returnTrackingNo: optional(r.returnTrackingNo),
    returnShippedAt: optional(r.returnShippedAt),
    returnReceivedAt: optional(r.returnReceivedAt),
    refundTime: optional(r.refundTime),
  };
}

/** 系统操作者允许编号0，会员和后台编号必须是正数。 */
export function decodeOrderLog(value: unknown): import('./types').OrderLog {
  const row = object(value);
  const userType = integer(row.userType);
  if (![0, 1, 2].includes(userType)) throw new Error('未知日志用户类型');
  const userId =
    userType === 0 && (row.userId === 0 || row.userId === '0') ? '0' : positiveId(row.userId);
  if (userType === 0 && userId !== '0') throw new Error('系统操作者编号不一致');
  const beforeStatus = row.beforeStatus == null ? null : integer(row.beforeStatus);
  const afterStatus = integer(row.afterStatus);
  if (
    (beforeStatus !== null && ![0, 10, 20, 30, 40].includes(beforeStatus)) ||
    ![0, 10, 20, 30, 40].includes(afterStatus)
  )
    throw new Error('未知日志订单状态');
  return {
    id: positiveId(row.id),
    userId,
    userType,
    orderId: positiveId(row.orderId),
    beforeStatus,
    afterStatus,
    operateType: integer(row.operateType),
    content: text(row.content),
    createTime: text(row.createTime),
  };
}

/** 售后状态与订单状态不同，不能复用订单日志解码。 */
export function decodeAfterSaleLog(value: unknown): import('./types').AfterSaleLog {
  const row = object(value);
  const userType = integer(row.userType);
  if (![0, 1, 2].includes(userType)) throw new Error('售后日志用户类型不合法');
  const userId =
    userType === 0 && (row.userId === 0 || row.userId === '0') ? '0' : positiveId(row.userId);
  if (userType === 0 && userId !== '0') throw new Error('系统用户编号不一致');
  const beforeStatus = row.beforeStatus == null ? null : integer(row.beforeStatus);
  const afterStatus = integer(row.afterStatus);
  const states = [10, 20, 30, 40, 50, 61, 62, 63];
  if ((beforeStatus !== null && !states.includes(beforeStatus)) || !states.includes(afterStatus))
    throw new Error('售后日志状态不合法');
  return {
    id: positiveId(row.id),
    userId,
    userType,
    afterSaleId: positiveId(row.afterSaleId),
    beforeStatus,
    afterStatus,
    operateType: integer(row.operateType),
    content: text(row.content),
    createTime: text(row.createTime),
  };
}

function snapshotStatus(value: unknown): PricingSnapshotStatus {
  if (value !== 'COMPLETE' && value !== 'LEGACY_PARTIAL') throw new Error('未知优惠快照状态');
  return value;
}
function nullableMoney(value: unknown, signed = false): string | null {
  if (value == null) return null;
  if (signed && typeof value === 'string' && /^-?\d+\.\d{2}$/.test(value)) return value;
  return money(value);
}
function pointText(value: unknown): string | null {
  if (value == null) return null;
  const result = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof result !== 'string' || !/^\d+$/.test(result) || BigInt(result) > 9223372036854775807n)
    throw new Error('积分数量不合法');
  return result;
}
function promotionMoney(row: Record<string, unknown>) {
  return {
    promotionType: text(row.promotionType),
    promotionName: optional(row.promotionName),
    discountAmount: nullableMoney(row.discountAmount),
    deliveryDiscountPrice: nullableMoney(row.deliveryDiscountPrice),
  };
}
/** 解析原订单优惠，历史未知与明确无优惠严格区分。 */
export function decodeOrderPromotions(value: unknown): OrderPromotionDetail {
  const row = object(value);
  const summary = object(row.summary);
  if (!Array.isArray(row.promotions)) throw new Error('缺少优惠列表');
  return {
    orderNo: text(row.orderNo),
    snapshotStatus: snapshotStatus(row.snapshotStatus),
    summary: {
      originalAmount: nullableMoney(summary.originalAmount),
      discountAmount: nullableMoney(summary.discountAmount),
      deliveryOriginalPrice: nullableMoney(summary.deliveryOriginalPrice),
      deliveryDiscountPrice: nullableMoney(summary.deliveryDiscountPrice),
      adjustmentAmount: nullableMoney(summary.adjustmentAmount, true),
      payAmount: money(summary.payAmount),
    },
    promotions: row.promotions.map((value) => {
      const promotion = object(value);
      if (!Array.isArray(promotion.items)) throw new Error('缺少商品行优惠分摊');
      const coupons =
        promotion.giveCouponTemplateCounts == null
          ? null
          : object(promotion.giveCouponTemplateCounts);
      const couponCounts: Record<string, number> | null = coupons == null ? null : {};
      if (coupons && couponCounts)
        for (const [key, count] of Object.entries(coupons)) {
          positiveId(key);
          const quantity = integer(count);
          if (quantity < 1) throw new Error('赠券数量不合法');
          couponCounts[key] = quantity;
        }
      return {
        ...promotionMoney(promotion),
        usePoint: pointText(promotion.usePoint),
        givePoint: pointText(promotion.givePoint),
        giveCouponTemplateCounts: couponCounts,
        items: promotion.items.map((value) => {
          const item = object(value);
          return {
            orderItemId: positiveId(item.orderItemId),
            discountAmount: nullableMoney(item.discountAmount),
            deliveryDiscountPrice: nullableMoney(item.deliveryDiscountPrice),
            usePoint: pointText(item.usePoint),
            givePoint: pointText(item.givePoint),
          };
        }),
      };
    }),
  };
}
/** 应返积分仅为计划值，必须结合权益执行状态展示。 */
export function decodeAfterSalePromotions(value: unknown): AfterSalePromotionDetail {
  const row = object(value);
  const benefitStatus = row.benefitStatus;
  if (
    benefitStatus !== 'NOT_STARTED' &&
    benefitStatus !== 'PENDING' &&
    benefitStatus !== 'SUCCESS' &&
    benefitStatus !== 'FAILED' &&
    benefitStatus !== 'UNKNOWN'
  )
    throw new Error('未知权益处理状态');
  if (!Array.isArray(row.promotions)) throw new Error('缺少退款优惠归因列表');
  return {
    afterSaleNo: text(row.afterSaleNo),
    orderItemId: row.orderItemId == null ? null : positiveId(row.orderItemId),
    quantity: row.quantity == null ? null : integer(row.quantity),
    amount: money(row.amount),
    discountAmount: nullableMoney(row.discountAmount),
    deliveryDiscountPrice: nullableMoney(row.deliveryDiscountPrice),
    returnUsePoint: pointText(row.returnUsePoint),
    deductGivePoint: pointText(row.deductGivePoint),
    ruleVersion: row.ruleVersion == null ? null : integer(row.ruleVersion),
    snapshotStatus: snapshotStatus(row.snapshotStatus),
    benefitStatus: benefitStatus as BenefitStatus,
    promotions: row.promotions.map((value) => {
      const promotion = object(value);
      return {
        ...promotionMoney(promotion),
        returnUsePoint: pointText(promotion.returnUsePoint),
        deductGivePoint: pointText(promotion.deductGivePoint),
      };
    }),
  };
}

/** 已退和可申请数量来自原退单事实，客户端不另存处理中数量。 */
export function decodeRefundableItems(value: unknown): OrderRefundableItem[] {
  if (!Array.isArray(value)) throw new Error('缺少可售后商品列表');
  return value.map((value) => {
    const row = object(value);
    const quantity = integer(row.quantity);
    const refundedQuantity = row.refundedQuantity == null ? null : integer(row.refundedQuantity);
    const refundableQuantity = integer(row.refundableQuantity);
    if ((refundedQuantity != null && refundedQuantity > quantity) || refundableQuantity > quantity)
      throw new Error('售后数量超出购买数量');
    return {
      orderItemId: positiveId(row.orderItemId),
      quantity,
      refundedQuantity,
      refundableQuantity,
      refundableAmount: money(row.refundableAmount),
    };
  });
}
