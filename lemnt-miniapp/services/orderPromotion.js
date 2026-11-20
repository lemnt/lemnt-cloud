const BENEFIT_LABELS = Object.freeze({
  NOT_STARTED: '退款完成后执行',
  PENDING: '处理中',
  SUCCESS: '已完成',
  FAILED: '处理失败，待重试',
  UNKNOWN: '历史未记录',
});
function base(value) {
  if (
    !value ||
    !['COMPLETE', 'LEGACY_PARTIAL'].includes(value.snapshotStatus) ||
    !Array.isArray(value.promotions)
  )
    throw new Error('优惠快照响应不合法');
  return value;
}
function money(value, signed = false) {
  if (value == null) return null;
  if (typeof value !== 'string' || !(signed ? /^-?\d+\.\d{2}$/ : /^\d+\.\d{2}$/).test(value))
    throw new Error('优惠金额不合法');
  return value;
}
function points(value) {
  if (value == null) return null;
  const result = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof result !== 'string' || !/^\d+$/.test(result)) throw new Error('优惠积分不合法');
  const normalized = result.replace(/^0+(?=\d)/, '');
  if (normalized.length > 19 || (normalized.length === 19 && normalized > '9223372036854775807'))
    throw new Error('优惠积分不合法');
  return normalized;
}
function source(value, index, order) {
  if (!value || typeof value.promotionType !== 'string') throw new Error('优惠来源不合法');
  const result = {
    ...value,
    viewKey: String(index),
    discountAmount: money(value.discountAmount),
    deliveryDiscountPrice: money(value.deliveryDiscountPrice),
  };
  if (order) {
    if (!Array.isArray(value.items)) throw new Error('缺少商品行优惠分摊');
    result.usePoint = points(value.usePoint);
    result.givePoint = points(value.givePoint);
    result.items = value.items.map((item, i) => ({
      ...item,
      viewKey: String(i),
      discountAmount: money(item.discountAmount),
      deliveryDiscountPrice: money(item.deliveryDiscountPrice),
      usePoint: points(item.usePoint),
      givePoint: points(item.givePoint),
    }));
  } else {
    result.returnUsePoint = points(value.returnUsePoint);
    result.deductGivePoint = points(value.deductGivePoint);
  }
  return result;
}
function orderPromotion(value, orderNo) {
  const row = base(value);
  if (row.orderNo !== orderNo || !row.summary) throw new Error('优惠订单不匹配');
  return {
    ...row,
    promotions: row.promotions.map((p, i) => source(p, i, true)),
    summary: {
      ...row.summary,
      discountAmount: money(row.summary.discountAmount),
      deliveryDiscountPrice: money(row.summary.deliveryDiscountPrice),
      adjustmentAmount: money(row.summary.adjustmentAmount, true),
    },
  };
}
function afterSalePromotion(value, no) {
  const row = base(value);
  if (
    row.afterSaleNo !== no ||
    !Object.prototype.hasOwnProperty.call(BENEFIT_LABELS, row.benefitStatus)
  )
    throw new Error('售后权益响应不匹配');
  return {
    ...row,
    promotions: row.promotions.map((p, i) => source(p, i, false)),
    returnUsePoint: points(row.returnUsePoint),
    deductGivePoint: points(row.deductGivePoint),
    amount: money(row.amount),
    discountAmount: money(row.discountAmount),
    deliveryDiscountPrice: money(row.deliveryDiscountPrice),
    benefitStatusLabel: BENEFIT_LABELS[row.benefitStatus],
  };
}
module.exports = { orderPromotion, afterSalePromotion };
