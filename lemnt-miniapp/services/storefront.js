const { couponId: id, ownedCoupons } = require('./coupons');
function obj(v) {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw Error('商城响应格式不合法');
  return v;
}
function text(v) {
  if (typeof v !== 'string') throw Error('商城文本格式不合法');
  return v;
}
function integer(v) {
  if (typeof v !== 'number' || !Number.isSafeInteger(v) || v < 0) throw Error('商城数值格式不合法');
  return v;
}
function money(v) {
  if (typeof v !== 'string' || !/^\d{1,17}\.\d{2}$/.test(v)) throw Error('商城金额格式不合法');
  return v;
}
function image(v) {
  return typeof v === 'string' && /^https:\/\/[^\s]+$/.test(v) ? v : null;
}
function list(v, decode) {
  if (!Array.isArray(v)) throw Error('商城列表格式不合法');
  return v.map(decode);
}
function page(v, decode) {
  obj(v);
  return { records: list(v.records, decode), total: integer(v.total) };
}
function product(v) {
  obj(v);
  if (typeof v.available !== 'boolean') throw Error('可售状态不合法');
  return {
    id: id(v.id),
    name: text(v.name),
    categoryId: id(v.categoryId),
    brandId: id(v.brandId),
    image: image(v.image),
    minPrice: money(v.minPrice),
    maxPrice: money(v.maxPrice),
    available: v.available,
    displaySalesCount: v.displaySalesCount == null ? null : integer(v.displaySalesCount),
  };
}
/** 重量/体积只作展示，保留服务端小数文本；历史NULL不补零。 */
function dimension(v) {
  if (v == null) return null;
  if (typeof v !== 'string' && typeof v !== 'number') throw Error('配送参数格式不合法');
  const value = String(v);
  if (!/^\d+(?:\.\d+)?$/.test(value)) throw Error('配送参数格式不合法');
  return value;
}
function ids(values) {
  if (!Array.isArray(values) || values.length > 100) throw Error('分类编号集合不合法');
  return [...new Set(values.map(id))].join(',');
}
function detail(v, key) {
  obj(v);
  if (id(v.id) !== key) throw Error('商品详情编号不一致');
  const seen = new Set();
  return {
    id: key,
    name: text(v.name),
    description: v.description == null ? '' : text(v.description),
    image: image(v.image),
    displaySalesCount: v.displaySalesCount == null ? null : integer(v.displaySalesCount),
    introduction: v.introduction == null ? null : text(v.introduction),
    sliderPicUrls: v.sliderPicUrls == null ? null : list(v.sliderPicUrls, image).filter(Boolean),
    deliveryTypes:
      v.deliveryTypes == null
        ? null
        : list(v.deliveryTypes, (value) => {
            if (![1, 2].includes(value)) throw Error('配送方式不合法');
            return value;
          }),
    deliveryTemplateId: v.deliveryTemplateId == null ? null : id(v.deliveryTemplateId),
    skus: list(v.skus, (s) => {
      obj(s);
      const sku = id(s.id);
      if (seen.has(sku)) throw Error('规格记录重复');
      seen.add(sku);
      const pairs = Array.isArray(s.specs)
        ? s.specs.map((p) => {
            obj(p);
            if ((p.specId == null) !== (p.specValueId == null)) throw Error('规格编号必须成对');
            if (p.specId != null) {
              id(p.specId);
              id(p.specValueId);
            }
            return text(p.specName) + ':' + text(p.specValueName);
          })
        : Object.keys(obj(s.specs))
            .sort()
            .map((k) => text(k) + ':' + text(s.specs[k]));
      return {
        id: sku,
        name: text(s.name),
        price: money(s.price),
        marketPrice: s.marketPrice == null ? null : money(s.marketPrice),
        stock: integer(s.stock),
        weight: dimension(s.weight),
        volume: dimension(s.volume),
        image: image(s.image),
        label: pairs.length ? pairs.join(' / ') : text(s.name),
      };
    }),
  };
}
function decodeBanner(v) {
  obj(v);
  const banner = id(v.id);
  const pic = image(v.picUrl);
  if (!pic) throw Error('Banner 图片地址不合法');
  return { id: banner, title: text(v.title), url: text(v.url), picUrl: pic };
}
function createStoreClient({ request, baseUrl, ready = false, getMemberToken = () => null }) {
  async function call(path, data) {
    if (!ready) throw Error('商城服务暂未启用');
    if (!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl)) throw Error('请配置合法HTTPS服务地址');
    const memberToken = /^\/products\/[1-9]\d*$/.test(path) ? getMemberToken() : null;
    const r = await request({
      url:
        baseUrl.replace(/\/$/, '') +
        // 商品资源已迁出/mall前缀；装修Banner仍归promotion
        (/^\/(products|categories)(\/|$)/.test(path) ? '/app' : '/mall/app') +
        path,
      method: 'GET',
      ...(memberToken ? { header: { Authorization: 'Bearer ' + memberToken } } : {}),
      data,
      timeout: 15000,
    });
    if (r.statusCode !== 200 || r.data?.code !== 200)
      throw Error(r.data?.msg || '商品读取失败，请重试');
    return path === '/products' ? { records: r.data.rows, total: r.data.total } : r.data.data;
  }
  return {
    banners: (position) => {
      if (![1, 2, 3, 4, 5].includes(position)) throw Error('Banner 位置不合法');
      return call('/banners', { position }).then((v) => list(v, decodeBanner));
    },
    categories: (values) =>
      call('/categories', values == null ? undefined : { ids: ids(values) }).then((v) =>
        list(v, (x) => {
          obj(x);
          return {
            id: id(x.id),
            parentId: x.parentId == null ? null : id(x.parentId),
            name: text(x.name),
          };
        }),
      ),
    products: async (q) => {
      if (!Number.isSafeInteger(q.pageNum) || q.pageNum < 1 || q.pageNum > 1000000)
        throw Error('页码不合法');
      if (q.sortField != null && !['price', 'salesCount', 'createTime'].includes(q.sortField))
        throw Error('排序字段不合法');
      if (q.sortAsc != null && typeof q.sortAsc !== 'boolean') throw Error('排序方向不合法');
      return page(
        await call('/products', {
          name: q.name || undefined,
          categoryId: q.categoryId ? id(q.categoryId) : undefined,
          ...(q.categoryIds == null ? {} : { categoryIds: ids(q.categoryIds) }),
          ...(q.sortField == null ? {} : { sortField: q.sortField }),
          ...(q.sortAsc == null ? {} : { sortAsc: q.sortAsc }),
          pageNum: q.pageNum,
          pageSize: 20,
        }),
        product,
      );
    },
    product: async (key) => detail(await call('/products/' + id(key)), id(key)),
  };
}
function rule(t) {
  if (!['FIXED', 'PERCENT'].includes(t.kind)) throw Error('优惠券类型不合法');
  return t.kind === 'FIXED'
    ? '减' + money(t.fixedAmount) + '元'
    : '支付原价' +
        integer(t.discountPercent) +
        '%' +
        (t.discountLimit == null ? '' : '，最多减' + money(t.discountLimit) + '元');
}
function offer(v) {
  obj(v);
  const t = obj(v.template);
  if (
    typeof t.enabled !== 'boolean' ||
    typeof v.claimable !== 'boolean' ||
    typeof v.usable !== 'boolean'
  )
    throw Error('优惠券规则不合法');
  const couponId = v.couponId == null ? null : id(v.couponId);
  if (couponId && !['AVAILABLE', 'LOCKED', 'USED', 'EXPIRED'].includes(v.couponStatus))
    throw Error('优惠券状态不合法');
  if (!couponId && v.couponStatus != null) throw Error('优惠券状态不一致');
  if (v.usable && (!couponId || v.couponStatus !== 'AVAILABLE'))
    throw Error('优惠券可用状态不一致');
  if (t.kind === 'PERCENT' && integer(t.discountPercent) > 100) throw Error('优惠券折扣不合法');
  const total = t.totalCount === -1 ? -1 : integer(t.totalCount),
    issued = integer(t.issuedCount);
  if (total !== -1 && issued > total) throw Error('优惠券名额不一致');
  return {
    id: id(t.id),
    name: text(t.name),
    threshold: money(t.threshold),
    rule: rule(t),
    validFrom:
      t.validityType === 'TERM'
        ? '领取后' + integer(t.fixedStartTerm) + '天生效'
        : text(t.validFrom),
    validUntil:
      t.validityType === 'TERM' ? '有效' + integer(t.fixedEndTerm) + '天' : text(t.validUntil),
    remaining: total === -1 ? '不限' : total - issued,
    takeLimitCount:
      t.takeLimitCount === -1 ? '不限' : integer(t.takeLimitCount == null ? 1 : t.takeLimitCount),
    couponId,
    status: v.couponStatus || null,
    usable: v.usable,
    claimable: v.claimable,
  };
}
function decodeOffers(raw) {
  return page(raw, offer);
}
function decodeWallet(raw) {
  return page(raw, (v) => {
    obj(v);
    const t = obj(v.terms);
    if (
      !['AVAILABLE', 'LOCKED', 'USED', 'EXPIRED'].includes(v.couponStatus) ||
      typeof v.usable !== 'boolean' ||
      (v.usable && v.couponStatus !== 'AVAILABLE')
    )
      throw Error('持券状态不合法');
    return {
      id: id(v.couponId),
      templateId: id(v.templateId),
      couponId: id(v.couponId),
      name: text(t.name),
      threshold: money(t.threshold),
      rule: rule(t),
      validFrom: text(t.validFrom),
      validUntil: text(t.validUntil),
      status: v.couponStatus,
      usable: v.usable,
      claimable: false,
    };
  });
}
function decodeClaim(raw, member, template) {
  const value = ownedCoupons([raw], member)[0];
  if (value.templateId !== template) throw Error('领券结果与模板不一致');
  return value;
}
module.exports = { createStoreClient, decodeOffers, decodeWallet, decodeClaim, decodeBanner, id };
