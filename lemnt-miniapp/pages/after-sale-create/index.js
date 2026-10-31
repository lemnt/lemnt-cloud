const runtime = require('../../services/orderRuntime');

const TYPES = Object.freeze(['REFUND_ONLY', 'RETURN_REFUND']);
const TYPE_LABELS = Object.freeze(['仅退款', '退货退款']);

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false, orderNo: '',
    items: [], selectedIndex: -1, quantity: 1, typeIndex: 0, typeLabels: TYPE_LABELS, reason: '',
  },
  onLoad(query) {
    runtime.attach(this);
    this.orderNo = decodeURIComponent(query.orderNo || '');
    this.requestNo = null;
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onSessionLost() { this.setData({ items: [], selectedIndex: -1, quantity: 1, reason: '', loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  resetRequest() { this.requestNo = null; },
  newRequest() {
    return 'MINI-AS-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 12);
  },
  reload() {
    if (!this.orderNo) { this.setData({ error: '缺少订单编号' }); return; }
    return runtime.run(this, async () => {
      const value = await this.client.afterSaleItems(this.orderNo);
      if (!Array.isArray(value)) throw new Error('可售后商品响应不合法');
      const items = value.map(item => {
        if (!item || !Number.isSafeInteger(item.orderItemId) || item.orderItemId <= 0
          || !Number.isInteger(item.refundableQuantity) || item.refundableQuantity < 0
          || typeof item.refundableAmount !== 'string' || !/^\d+\.\d{2}$/.test(item.refundableAmount)) {
          throw new Error('可售后商品数据不合法');
        }
        return item;
      });
      const selectedIndex = items.findIndex(item => item.refundableQuantity > 0);
      this.resetRequest();
      this.setData({ items, selectedIndex, quantity: selectedIndex >= 0 ? 1 : 0 });
    });
  },
  select(event) {
    const selectedIndex = Number(event.currentTarget.dataset.index);
    const item = this.data.items[selectedIndex];
    if (!item || item.refundableQuantity <= 0) { this.setData({ error: '该商品已无可售后数量' }); return; }
    this.resetRequest();
    this.setData({ selectedIndex, quantity: 1, error: '' });
  },
  changeQuantity(event) {
    const kind = event.currentTarget.dataset.kind;
    const item = this.data.items[this.data.selectedIndex];
    if (!item) { this.setData({ error: '请先选择商品' }); return; }
    let quantity = this.data.quantity + (kind === 'plus' ? 1 : kind === 'minus' ? -1 : 0);
    if (quantity < 1) quantity = 1;
    if (quantity > item.refundableQuantity) quantity = item.refundableQuantity;
    this.resetRequest();
    this.setData({ quantity });
  },
  changeType(event) {
    this.resetRequest();
    this.setData({ typeIndex: Number(event.detail.value) });
  },
  inputReason(event) {
    this.resetRequest();
    this.setData({ reason: event.detail.value });
  },
  submit() {
    return runtime.run(this, async () => {
      const item = this.data.items[this.data.selectedIndex];
      if (!item) throw new Error('请选择售后商品');
      const reason = (this.data.reason || '').trim();
      if (!reason) throw new Error('请填写申请原因');
      if (this.data.quantity < 1 || this.data.quantity > item.refundableQuantity) throw new Error('售后数量超出可退范围');
      if (!this.requestNo) this.requestNo = this.newRequest();
      const result = await this.client.createAfterSale(this.orderNo, {
        requestNo: this.requestNo,
        orderItemId: item.orderItemId,
        quantity: this.data.quantity,
        type: TYPES[this.data.typeIndex],
        reason,
      });
      if (!result || typeof result.refundNo !== 'string') throw new Error('售后结果无法确认，请稍后查询');
      wx.showToast({ title: '申请已提交', icon: 'success' });
      wx.redirectTo({ url: '/pages/after-sale-detail/index?no=' + encodeURIComponent(result.refundNo) });
    });
  },
});
