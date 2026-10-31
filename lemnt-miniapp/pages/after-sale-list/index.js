const runtime = require('../../services/orderRuntime');

const STATUS_LABEL = Object.freeze({
  REQUESTED: '待商家审核', AWAITING_RETURN: '待买家退货', RETURNED: '待商家收货', WAIT_REFUND: '待退款',
  RETURN_REJECTED: '退货被拒', REJECTED: '已拒绝', REFUNDING: '退款中', REFUNDED: '已退款', CANCELLED: '已取消',
});

Page({
  data: { ready: runtime.ready, loading: false, error: '', loggedIn: false, records: [], total: 0, pageNum: 0, hasMore: false },
  onLoad() { runtime.attach(this); },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false }); },
  onSessionLost() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  reload() { return runtime.run(this, () => this.loadPage(1, true)); },
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    return runtime.run(this, () => this.loadPage(this.data.pageNum + 1, false));
  },
  async loadPage(pageNum, reset) {
    const page = await this.client.afterSalePage({ pageNum, pageSize: 10 });
    if (!page || !Array.isArray(page.records)) throw new Error('售后列表响应不合法');
    const records = page.records.map(value => {
      if (!value || typeof value.refundNo !== 'string' || !STATUS_LABEL[value.status]
        || typeof value.amount !== 'string') {
        throw new Error('售后数据不合法');
      }
      return { ...value, statusLabel: STATUS_LABEL[value.status] };
    });
    const merged = reset ? records : this.data.records.concat(records);
    this.setData({ records: merged, total: page.total, pageNum, hasMore: records.length === 10 && merged.length < page.total });
  },
  goDetail(event) {
    wx.navigateTo({ url: '/pages/after-sale-detail/index?no=' + encodeURIComponent(event.currentTarget.dataset.no) });
  },
});
