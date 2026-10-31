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
  refunds: (query: object) => ['trade', 'fulfillment', 'refunds', query] as const,
};
