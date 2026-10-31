const runtime = require('../../services/orderRuntime');

const STATUS_LABEL = Object.freeze({
  REQUESTED: '待商家审核', AWAITING_RETURN: '待买家退货', RETURNED: '待商家收货', WAIT_REFUND: '待退款',
  RETURN_REJECTED: '退货被拒', REJECTED: '已拒绝', REFUNDING: '退款中', REFUNDED: '已退款', CANCELLED: '已取消',
});
const TYPE_LABEL = Object.freeze({ REFUND_ONLY: '仅退款', RETURN_REFUND: '退货退款' });

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false, sale: null,
    showReturn: false, carrier: '', tracking: '',
  },
  onLoad(query) {
    runtime.attach(this);
    this.no = decodeURIComponent(query.no || '');
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onSessionLost() { this.setData({ sale: null, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  reload() {
    if (!this.no) { this.setData({ error: '缺少售后单号' }); return; }
    return runtime.run(this, async () => this.setData({ sale: this.toSale(await this.client.afterSaleDetail(this.no)) }));
  },
  toSale(value) {
    if (!value || value.refundNo !== this.no || !STATUS_LABEL[value.status]
      || !TYPE_LABEL[value.type] || typeof value.amount !== 'string') {
      throw new Error('售后详情响应不合法');
    }
    return {
      ...value,
      statusLabel: STATUS_LABEL[value.status],
      typeLabel: TYPE_LABEL[value.type],
      canCancel: ['REQUESTED', 'AWAITING_RETURN', 'RETURNED'].includes(value.status),
      canReturn: value.status === 'AWAITING_RETURN' && value.type === 'RETURN_REFUND' && !value.returnTrackingNo,
    };
  },
  toggleReturn() { this.setData({ showReturn: !this.data.showReturn, error: '' }); },
  inputCarrier(event) { this.setData({ carrier: event.detail.value }); },
  inputTracking(event) { this.setData({ tracking: event.detail.value }); },
  submitReturn() {
    return runtime.run(this, async () => {
      const carrierCode = (this.data.carrier || '').trim();
      const trackingNo = (this.data.tracking || '').trim();
      if (!carrierCode) throw new Error('请填写快递公司');
      if (!trackingNo) throw new Error('请填写快递单号');
      await this.client.returnAfterSale(this.no, { carrierCode, trackingNo });
      this.setData({ showReturn: false, carrier: '', tracking: '' });
      this.setData({ sale: this.toSale(await this.client.afterSaleDetail(this.no)) });
    });
  },
  cancel() {
    return runtime.run(this, async () => {
      if (!await runtime.confirm('取消售后', '确认取消该售后申请？')) return;
      await this.client.cancelAfterSale(this.no);
      this.setData({ sale: this.toSale(await this.client.afterSaleDetail(this.no)) });
    });
  },
});
