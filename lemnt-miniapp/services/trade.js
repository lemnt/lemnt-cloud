// Transport is injected: no dependency on admin tokens, memberId inputs or wx globals.
// service 显式区分 mall/app 与 trade/app，避免依赖路径正则猜测归属。
function createTradeClient({ request, baseUrl, getMemberToken, ready = false }) {
  async function call(path, method = 'GET', data, service = 'mall') {
    if (!ready) throw new Error('会员接口尚未接通');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const token = getMemberToken();
    if (!token) throw new Error('请先完成会员登录');
    const prefix = service === 'trade' ? '/trade/app' : '/mall/app';
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + prefix + path,
      method, data, timeout: 15000,
      header: { Authorization: `Bearer ${token}` },
    });
    if (getMemberToken() !== token) throw new Error('会员已切换，请重新操作');
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(response.data && (response.data.detail || response.data.msg) || '请求失败，请查询订单结果后重试');
    }
    if (path === '/coupon-center' || path === '/coupon-wallet') {
      return { records: response.data.rows, total: response.data.total };
    }
    if (service === 'trade' && (path === '/orders' || path === '/after-sales' || path === '/wallet/transactions' || path === '/wallet/recharges')) {
      return { records: response.data.rows || [], total: response.data.total || 0 };
    }
    return response.data.data;
  }
  function id(value) {
    if (typeof value !== 'string' || !/^[1-9]\d{0,18}$/.test(value)) throw new Error('商品编号须为正整数字符串');
    return value;
  }
  function order(value) {
    if (typeof value !== 'string' || !value.trim() || value.length > 40) throw new Error('订单编号不合法');
    return encodeURIComponent(value);
  }
  function page(input = {}) {
    const pageNum = input.pageNum === undefined ? 1 : input.pageNum;
    const pageSize = input.pageSize === undefined ? 10 : input.pageSize;
    if (!Number.isInteger(pageNum) || pageNum < 1 || pageNum > 1000000) throw new Error('页码不合法');
    if (!Number.isInteger(pageSize) || pageSize < 1 || pageSize > 100) throw new Error('每页数量不合法');
    return { pageNum, pageSize };
  }
  function positiveId(value) {
    if (!Number.isSafeInteger(value) || value <= 0) throw new Error('商品行编号不合法');
    return value;
  }
  function quoteData(input) {
    if (!input || !Array.isArray(input.productIds) || !input.productIds.length) throw new Error('请选择商品');
    if (input.pointStatus !== undefined && typeof input.pointStatus !== 'boolean') throw new Error('积分选择不合法');
    return { addressId: id(input.addressId), ...(input.pointStatus === undefined ? {} : { pointStatus: input.pointStatus }), productIds: input.productIds.map(id), ...(input.couponId ? { couponId: id(input.couponId) } : {}) };
  }
  return {
    cart: () => call('/cart'),
    saveCart: (productId, input) => {
      if (!Number.isInteger(input.quantity) || input.quantity < 1 || typeof input.selected !== 'boolean') throw new Error('购物车参数不合法');
      return call('/cart/' + id(productId), 'PUT', { quantity: input.quantity, selected: input.selected });
    },
    deleteCart: productId => call('/cart/' + id(productId), 'DELETE'),
    coupons: () => call('/coupons'),
    couponCenter: pageNum => call('/coupon-center','GET',{pageNum,pageSize:20}),
    couponWallet: pageNum => call('/coupon-wallet','GET',{pageNum,pageSize:20}),
    claimCoupon: templateId => call('/coupon-center/'+id(templateId)+'/claim','POST'),
    quote: input => call('/checkout/quote', 'POST', quoteData(input)),
    checkout: input => {
      if (!input || typeof input.requestNo !== 'string' || !input.requestNo.trim() || input.requestNo.length > 64 || typeof input.settlementId !== 'string' || !/^[a-f0-9-]{36}$/.test(input.settlementId)) throw new Error('结算单编号或请求编号不合法');
      return call('/checkout', 'POST', { settlementId: input.settlementId, requestNo: input.requestNo });
    },
    order: orderNo => call('/orders/' + order(orderNo)),
    prepay: orderNo => call('/orders/' + order(orderNo) + '/prepay', 'POST', undefined, 'trade'),
    abandonCheckout: input => {
      if (!input || typeof input.settlementId !== 'string' || !/^[a-f0-9-]{36}$/.test(input.settlementId) || typeof input.requestNo !== 'string' || !input.requestNo.trim() || input.requestNo.length > 64) throw new Error('结算单编号或请求编号不合法');
      return call('/checkout/abandon','POST',{settlementId:input.settlementId,requestNo:input.requestNo});
    },
    checkoutRequest: requestNo => {
      if (typeof requestNo !== 'string' || !requestNo.trim() || requestNo.length > 64) throw new Error('请求编号不合法');
      return call('/checkout/requests/' + encodeURIComponent(requestNo));
    },
    paymentStatus: orderNo => call('/orders/' + order(orderNo) + '/payment', 'GET', undefined, 'trade'),
    cancelOrder: orderNo => call('/orders/' + order(orderNo) + '/cancel', 'POST', undefined, 'trade'),
    receiveOrder: orderNo => call('/orders/' + order(orderNo) + '/receive', 'POST', undefined, 'trade'),
    cancelAfterSale: no => call('/after-sales/' + order(no) + '/cancel', 'POST', undefined, 'trade'),

    // 钱包
    wallet: () => call('/wallet', 'GET', undefined, 'trade'),
    walletTransactions: input => {
      const paging = page(input);
      const bizType = input && typeof input.bizType === 'string' && input.bizType ? input.bizType : undefined;
      if (bizType !== undefined && !['RECHARGE','RECHARGE_REFUND','PAYMENT','PAYMENT_REFUND','UPDATE_BALANCE','TRANSFER'].includes(bizType)) throw new Error('业务类型不合法');
      return call('/wallet/transactions', 'GET', { ...paging, ...(bizType === undefined ? {} : { bizType }) }, 'trade');
    },

    // 充值
    rechargePackages: () => call('/recharge-packages', 'GET', undefined, 'trade'),
    createRecharge: input => {
      const packageId = input && input.packageId;
      if (!Number.isSafeInteger(packageId) || packageId <= 0) throw new Error('套餐编号不合法');
      const requestNo = input && input.requestNo;
      if (typeof requestNo !== 'string' || !requestNo.trim() || requestNo.length > 64) throw new Error('请求编号不合法');
      return call('/wallet/recharges', 'POST', { packageId, requestNo }, 'trade');
    },
    myRecharges: input => call('/wallet/recharges', 'GET', page(input), 'trade'),

    // 订单中心
    orderPage: input => {
      const paging = page(input);
      const status = input && typeof input.status === 'string' && input.status ? input.status : undefined;
      if (status !== undefined && !['RESERVING','PENDING_PAYMENT','PAID','SHIPPED','COMPLETED','REFUNDED','CLOSED'].includes(status)) throw new Error('订单状态不合法');
      const commentStatus = input && input.commentStatus === null ? undefined : input && input.commentStatus;
      if (commentStatus !== undefined && typeof commentStatus !== 'boolean') throw new Error('评价状态不合法');
      return call('/orders', 'GET', { ...paging, ...(status === undefined ? {} : { status }), ...(commentStatus === undefined ? {} : { commentStatus }) }, 'trade');
    },
    orderStatusCounts: () => call('/orders/status-counts', 'GET', undefined, 'trade'),
    orderDetail: orderNo => call('/orders/' + order(orderNo), 'GET', undefined, 'trade'),
    orderTracks: orderNo => call('/orders/' + order(orderNo) + '/tracks', 'GET', undefined, 'trade'),

    // 售后中心
    afterSalePage: input => {
      const paging = page(input);
      const status = input && typeof input.status === 'string' && input.status ? input.status : undefined;
      const orderNo = input && typeof input.orderNo === 'string' && input.orderNo ? input.orderNo : undefined;
      if (orderNo !== undefined && orderNo.length > 40) throw new Error('订单编号不合法');
      return call('/after-sales', 'GET', { ...paging, ...(status === undefined ? {} : { status }), ...(orderNo === undefined ? {} : { orderNo }) }, 'trade');
    },
    afterSaleDetail: no => call('/after-sales/' + order(no), 'GET', undefined, 'trade'),
    afterSaleItems: orderNo => call('/orders/' + order(orderNo) + '/after-sale-items', 'GET', undefined, 'trade'),
    createAfterSale: (orderNo, input) => {
      if (!input || typeof input.requestNo !== 'string' || !input.requestNo.trim() || input.requestNo.length > 64) throw new Error('请求编号不合法');
      if (!Number.isInteger(input.quantity) || input.quantity < 1 || input.quantity > 999) throw new Error('售后数量须在1至999之间');
      if (!['REFUND_ONLY','RETURN_REFUND'].includes(input.type)) throw new Error('售后方式不合法');
      if (typeof input.reason !== 'string' || !input.reason.trim() || input.reason.length > 500) throw new Error('申请原因不能为空且不超过500字');
      return call('/orders/' + order(orderNo) + '/after-sales', 'POST',
        { requestNo: input.requestNo, orderItemId: positiveId(input.orderItemId), quantity: input.quantity, type: input.type, reason: input.reason }, 'trade');
    },
    returnAfterSale: (no, input) => {
      if (!input || typeof input.carrierCode !== 'string' || !input.carrierCode.trim() || input.carrierCode.length > 40) throw new Error('快递公司不能为空且不超过40字');
      if (typeof input.trackingNo !== 'string' || !input.trackingNo.trim() || input.trackingNo.length > 100) throw new Error('快递单号不能为空且不超过100字');
      return call('/after-sales/' + order(no) + '/return', 'POST', { carrierCode: input.carrierCode, trackingNo: input.trackingNo }, 'trade');
    },
  };
}
module.exports = { createTradeClient };
