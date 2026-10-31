const runtime = require('../../services/orderRuntime');

const STATUS_LABEL = Object.freeze({
  RESERVING: '处理中', PENDING_PAYMENT: '待付款', PAID: '待发货', SHIPPED: '待收货',
  COMPLETED: '已完成', CLOSED: '已取消', REFUNDED: '已退款',
});

Page({
  data: { ready: runtime.ready, loading: false, error: '', loggedIn: false, order: null, tracks: [], showTracks: false },
  onLoad(query) {
    runtime.attach(this);
    this.orderNo = decodeURIComponent(query.orderNo || '');
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onSessionLost() { this.setData({ order: null, tracks: [], showTracks: false, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  reload() {
    if (!this.orderNo) { this.setData({ error: '缺少订单编号' }); return; }
    return runtime.run(this, async () => this.setData({ order: this.toOrder(await this.client.orderDetail(this.orderNo)) }));
  },
  toOrder(value) {
    if (!value || value.orderNo !== this.orderNo || !STATUS_LABEL[value.status]
      || typeof value.totalAmount !== 'string' || !Array.isArray(value.items)) {
      throw new Error('订单详情响应不合法');
    }
    return {
      ...value,
      statusLabel: STATUS_LABEL[value.status],
      canCancel: ['RESERVING', 'PENDING_PAYMENT'].includes(value.status),
      canReceive: value.status === 'SHIPPED',
      canComment: value.status === 'COMPLETED' && value.commentStatus !== true,
      canAfterSale: ['PAID', 'SHIPPED', 'COMPLETED'].includes(value.status),
    };
  },
  toggleTracks() {
    if (this.data.showTracks) { this.setData({ showTracks: false, tracks: [] }); return; }
    return runtime.run(this, async () => {
      const value = await this.client.orderTracks(this.orderNo);
      if (!Array.isArray(value)) throw new Error('物流轨迹响应不合法');
      this.setData({ tracks: value, showTracks: true });
    });
  },
  goComment() {
    wx.navigateTo({ url: '/pages/order-comment/index?orderNo=' + encodeURIComponent(this.orderNo) });
  },
  goAfterSale() {
    wx.navigateTo({ url: '/pages/after-sale-create/index?orderNo=' + encodeURIComponent(this.orderNo) });
  },
  cancel() {
    return runtime.run(this, async () => {
      if (!await runtime.confirm('取消订单', '确认取消该未支付订单？')) return;
      await this.client.cancelOrder(this.orderNo);
      this.setData({ order: this.toOrder(await this.client.orderDetail(this.orderNo)) });
    });
  },
  receive() {
    return runtime.run(this, async () => {
      if (!await runtime.confirm('确认收货', '确认已收到商品？')) return;
      await this.client.receiveOrder(this.orderNo);
      this.setData({ order: this.toOrder(await this.client.orderDetail(this.orderNo)) });
    });
  },
});
