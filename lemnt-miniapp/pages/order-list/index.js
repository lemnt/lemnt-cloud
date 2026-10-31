const runtime = require('../../services/orderRuntime');

const STATUS_LABEL = Object.freeze({
  RESERVING: '处理中', PENDING_PAYMENT: '待付款', PAID: '待发货', SHIPPED: '待收货',
  COMPLETED: '已完成', CLOSED: '已取消', REFUNDED: '已退款',
});
const TABS = Object.freeze([
  { label: '全部', status: '', commentStatus: null },
  { label: '待付款', status: 'PENDING_PAYMENT', commentStatus: null },
  { label: '待发货', status: 'PAID', commentStatus: null },
  { label: '待收货', status: 'SHIPPED', commentStatus: null },
  { label: '待评价', status: 'COMPLETED', commentStatus: false },
  { label: '已完成', status: 'COMPLETED', commentStatus: true },
  { label: '已取消', status: 'CLOSED', commentStatus: null },
  { label: '退款/售后', status: 'REFUNDED', commentStatus: null },
]);

function number(value) {
  return typeof value === 'number' && value >= 0 ? value : 0;
}

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false,
    tabs: TABS, active: 0, counts: [0, 0, 0, 0, 0, 0, 0, 0],
    records: [], total: 0, pageNum: 0, hasMore: false,
  },
  onLoad() { runtime.attach(this); },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false }); },
  onSessionLost() {
    this.setData({ records: [], counts: [0, 0, 0, 0, 0, 0, 0, 0], total: 0, pageNum: 0, hasMore: false, loggedIn: false, error: '会员已切换，请重新登录' });
  },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  reload() {
    return runtime.run(this, async () => {
      await this.loadCounts();
      await this.loadPage(1, true);
    });
  },
  switchTab(event) {
    const active = Number(event.currentTarget.dataset.index);
    if (!Number.isInteger(active) || active < 0 || active >= TABS.length || active === this.data.active) return;
    this.setData({ active, records: [], total: 0, pageNum: 0, hasMore: false });
    return runtime.run(this, () => this.loadPage(1, true));
  },
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    return runtime.run(this, () => this.loadPage(this.data.pageNum + 1, false));
  },
  async loadCounts() {
    const value = await this.client.orderStatusCounts();
    if (!value || typeof value !== 'object') throw new Error('订单数量响应不合法');
    const counts = [value.pendingPayment, value.awaitingShipment, value.awaitingReceipt,
      value.awaitingComment, value.completed, value.closed, value.refunded].map(number);
    this.setData({ counts: [counts.reduce((sum, item) => sum + item, 0), ...counts] });
  },
  async loadPage(pageNum, reset) {
    const tab = TABS[this.data.active];
    const input = { pageNum, pageSize: 10, status: tab.status || undefined };
    if (tab.commentStatus !== null) input.commentStatus = tab.commentStatus;
    const page = await this.client.orderPage(input);
    if (!page || !Array.isArray(page.records)) throw new Error('订单列表响应不合法');
    const records = page.records.map(this.toRecord);
    this.setData({
      records: reset ? records : this.data.records.concat(records),
      total: number(page.total),
      pageNum,
      hasMore: page.records.length === 10 && (reset ? records.length : this.data.records.length + records.length) < number(page.total),
    });
  },
  toRecord(value) {
    if (!value || typeof value.orderNo !== 'string' || !STATUS_LABEL[value.status]
      || typeof value.totalAmount !== 'string' || !Array.isArray(value.items)) {
      throw new Error('订单数据不合法');
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
  goDetail(event) {
    wx.navigateTo({ url: '/pages/order-detail/index?orderNo=' + encodeURIComponent(event.currentTarget.dataset.order) });
  },
  goComment(event) {
    wx.navigateTo({ url: '/pages/order-comment/index?orderNo=' + encodeURIComponent(event.currentTarget.dataset.order) });
  },
  goAfterSale(event) {
    wx.navigateTo({ url: '/pages/after-sale-create/index?orderNo=' + encodeURIComponent(event.currentTarget.dataset.order) });
  },
  cancel(event) {
    const orderNo = event.currentTarget.dataset.order;
    return runtime.run(this, async () => {
      if (!await runtime.confirm('取消订单', '确认取消该未支付订单？')) return;
      await this.client.cancelOrder(orderNo);
      await this.reloadData();
    });
  },
  receive(event) {
    const orderNo = event.currentTarget.dataset.order;
    return runtime.run(this, async () => {
      if (!await runtime.confirm('确认收货', '确认已收到商品？')) return;
      await this.client.receiveOrder(orderNo);
      await this.reloadData();
    });
  },
  async reloadData() {
    await this.loadCounts();
    await this.loadPage(1, true);
  },
});
