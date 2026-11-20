function object(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('活动聚合响应不合法');
  return value;
}
function id(value, field = '编号') {
  const text = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof text !== 'string' || !/^[1-9]\d{0,18}$/.test(text)) throw new Error(`${field}不合法`);
  return text;
}
function text(value, field) {
  if (typeof value !== 'string') throw new Error(`${field}不合法`);
  return value;
}
function money(value) {
  const textValue = typeof value === 'number' ? value.toFixed(2) : value;
  if (typeof textValue !== 'string' || !/^\d{1,17}\.\d{2}$/.test(textValue))
    throw new Error('活动金额不合法');
  return textValue;
}
function item(value) {
  const row = object(value);
  return {
    id: id(row.id, '活动编号'),
    name: text(row.name, '活动名称'),
    startTime: text(row.startTime, '开始时间'),
    endTime: text(row.endTime, '结束时间'),
  };
}
function section(value) {
  const row = object(value);
  return {
    type: text(row.type, '活动类型'),
    label: text(row.label, '活动名称'),
    items: Array.isArray(row.items) ? row.items.map(item) : [],
  };
}
function discount(value) {
  const row = object(value);
  return {
    id: id(row.id),
    name: text(row.name, '活动名称'),
    startTime: text(row.startTime, '开始时间'),
    endTime: text(row.endTime, '结束时间'),
    products: Array.isArray(row.products)
      ? row.products.map((raw) => {
          const p = object(raw);
          if (p.discountType !== 'PRICE' && p.discountType !== 'PERCENT')
            throw new Error('折扣类型不合法');
          return {
            skuId: id(p.skuId, 'SKU编号'),
            spuId: id(p.spuId, '商品编号'),
            discountType: p.discountType,
            discountPercent: p.discountPercent == null ? null : Number(p.discountPercent),
            discountPrice: p.discountPrice == null ? null : money(p.discountPrice),
          };
        })
      : [],
  };
}
function reward(value) {
  const row = object(value);
  return {
    id: id(row.id),
    name: text(row.name, '活动名称'),
    startTime: text(row.startTime, '开始时间'),
    endTime: text(row.endTime, '结束时间'),
    conditionType: row.conditionType === 'COUNT' ? 'COUNT' : 'PRICE',
    rules: Array.isArray(row.rules)
      ? row.rules.map((raw) => {
          const r = object(raw);
          return {
            limit: money(r.limit),
            discountPrice: money(r.discountPrice),
            freeDelivery: r.freeDelivery === true,
            point: r.point == null ? 0 : Number(r.point),
            giveCouponTemplateCounts:
              r.giveCouponTemplateCounts && typeof r.giveCouponTemplateCounts === 'object'
                ? r.giveCouponTemplateCounts
                : {},
          };
        })
      : [],
  };
}
function createPromotionHubClient({ request, baseUrl, ready = false }) {
  async function call(path) {
    if (!ready) throw new Error('商城服务暂未启用');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + '/mall/app/promotion-hub' + path,
      method: 'GET',
      timeout: 15000,
    });
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(
        (response.data && (response.data.detail || response.data.msg)) || '活动读取失败',
      );
    }
    return response.data.data;
  }
  return {
    sections: (limit) =>
      call('/sections?limit=' + encodeURIComponent(limit || 10)).then((value) => {
        if (!Array.isArray(value)) throw new Error('活动聚合列表不合法');
        return value.map(section);
      }),
    discount: (key) => call('/discount/' + id(key)).then(discount),
    reward: (key) => call('/reward/' + id(key)).then(reward),
  };
}
module.exports = { createPromotionHubClient, section, discount, reward };
