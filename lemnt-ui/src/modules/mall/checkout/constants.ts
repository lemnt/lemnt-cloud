export const couponKindOptions = [
  { label: '满减', value: 'FIXED' },
  { label: '折扣（80表示8折）', value: 'PERCENT' },
];
export const couponKindEnum = { FIXED: { text: '满减' }, PERCENT: { text: '折扣' } };
export const couponStatusEnum = {
  AVAILABLE: { text: '可用（仍需核验有效期）' },
  LOCKED: { text: '订单锁定' },
  USED: { text: '已核销' },
  EXPIRED: { text: '已过期' },
};
export const checkoutStatusEnum = {
  RESERVING: { text: '库存确认中' },
  PENDING_PAYMENT: { text: '待支付' },
  PAID: { text: '已支付' },
  SHIPPED: { text: '已发货' },
  COMPLETED: { text: '已完成' },
  CLOSED: { text: '已关闭' },
  REFUNDED: { text: '已退款' },
};
export const MAX_COUPON_AMOUNT = 99999999999.99;
