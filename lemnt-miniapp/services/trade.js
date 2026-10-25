// Transport is injected: no dependency on admin tokens, memberId inputs or wx globals.
function createTradeClient({ request, baseUrl, getMemberToken, ready = false }) {
  async function call(path, method = 'GET', data) {
    if (!ready) throw new Error('会员接口尚未接通');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const token = getMemberToken();
    if (!token) throw new Error('请先完成会员登录');
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + '/trade/app' + path,
      method, data, timeout: 15000,
      header: { Authorization: `Bearer ${token}` },
    });
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(response.data && response.data.msg || '请求失败，请查询订单结果后重试');
    }
    return response.data.data;
  }
  function id(value) {
    if (typeof value !== 'string' || !/^[1-9]\d{0,18}$/.test(value)) throw new Error('商品编号须为正整数字符串');
    return value;
  }
  function quoteData(input) {
    if (!input || !Array.isArray(input.productIds) || !input.productIds.length) throw new Error('请选择商品');
    return { productIds: input.productIds.map(id), ...(input.couponId ? { couponId: id(input.couponId) } : {}) };
  }
  function order(value) {
    if (typeof value !== 'string' || !value.trim() || value.length > 40) throw new Error('订单编号不合法');
    return encodeURIComponent(value);
  }
  return {
    cart: () => call('/cart'),
    saveCart: (productId, input) => {
      if (!Number.isInteger(input.quantity) || input.quantity < 1 || typeof input.selected !== 'boolean') throw new Error('购物车参数不合法');
      return call('/cart/' + id(productId), 'PUT', { quantity: input.quantity, selected: input.selected });
    },
    deleteCart: productId => call('/cart/' + id(productId), 'DELETE'),
    coupons: () => call('/coupons'),
    quote: input => call('/checkout/quote', 'POST', quoteData(input)),
    checkout: input => {
      if (!input || typeof input.requestNo !== 'string' || !input.requestNo.trim() || input.requestNo.length > 64 || !/^[a-f0-9]{64}$/.test(input.quoteToken)) throw new Error('结算请求编号或报价令牌不合法');
      return call('/checkout', 'POST', { ...quoteData(input), requestNo: input.requestNo, quoteToken: input.quoteToken });
    },
    order: orderNo => call('/orders/' + order(orderNo)),
    prepay: orderNo => call('/orders/' + order(orderNo) + '/prepay', 'POST'),
    paymentStatus: orderNo => call('/orders/' + order(orderNo) + '/payment'),
  };
}
module.exports = { createTradeClient };
