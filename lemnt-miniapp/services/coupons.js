function couponId(value) {
  const id = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || (id.length === 19 && id > '9223372036854775807')) throw new Error('优惠券编号不合法');
  return id;
}
function ownedCoupons(rows, member) {
  if (!Array.isArray(rows)) throw new Error('优惠券响应格式不合法');
  const seen = new Set();
  return rows.map(row => {
    if (!row || couponId(row.memberId) !== member || !['AVAILABLE','LOCKED','USED','EXPIRED'].includes(row.status)) throw new Error('优惠券归属或状态不合法');
    const id = couponId(row.id);
    if (seen.has(id)) throw new Error('优惠券记录重复');
    seen.add(id);
    return { id, templateId: couponId(row.templateId), status: row.status };
  });
}
module.exports = { couponId, ownedCoupons };
