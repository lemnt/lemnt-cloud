import type { OrderSummary } from './types';
export const refundStatusEnum = {
  REQUESTED: { text: '待处理（未退款）' },
  REJECTED: { text: '已驳回' },
  AWAITING_RETURN: { text: '等待退货' },
  RETURNED: { text: '买家已寄回' },
  WAIT_REFUND: { text: '等待退款' },
  RETURN_REJECTED: { text: '退货已拒收' },
  REFUNDING: { text: '退款处理中' },
  REFUNDED: { text: '已退款' },
  CANCELLED: { text: '会员已取消' },
};
export function canShip(row: Pick<OrderSummary, 'status' | 'inventoryConfirmed' | 'activeRefund'>) {
  return row.status === 'PAID' && row.inventoryConfirmed && !row.activeRefund;
}
export function canRequestRefund(row: Pick<OrderSummary, 'status' | 'activeRefund'>) {
  return row.status === 'PAID' && !row.activeRefund;
}
export const fulfillmentKeys = {
  all: ['trade', 'fulfillment'] as const,
  orders: (query: object) => ['trade', 'fulfillment', 'orders', query] as const,
  detail: (no?: string) => ['trade', 'fulfillment', 'detail', no] as const,
  refundableItems: (no?: string) => ['trade', 'fulfillment', 'refundable-items', no] as const,
  orderPromotions: (no?: string) => ['trade', 'fulfillment', 'order-promotions', no] as const,
  afterSalePromotions: (no: string) =>
    ['trade', 'fulfillment', 'after-sale-promotions', no] as const,
  refunds: (query: object) => ['trade', 'fulfillment', 'refunds', query] as const,
};

export const benefitStatusLabels = {
  NOT_STARTED: '退款完成后执行',
  PENDING: '处理中',
  SUCCESS: '已完成',
  FAILED: '处理失败，待重试',
  UNKNOWN: '历史未记录',
} as const;
