const runtime = require('../../services/orderRuntime');

const MONEY = /^\d+\.\d{2}$/;

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false,
    toMemberId: '', amount: '', remark: '', records: [], total: 0, pageNum: 0, hasMore: false,
  },
  onLoad() { runtime.attach(this); this.requestNo = null; },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false }); },
  onSessionLost() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  resetRequest() { this.requestNo = null; },
  reload() { return runtime.run(this, () => this.loadPage(1, true)); },
  inputTo(event) { this.resetRequest(); this.setData({ toMemberId: String(event.detail.value || '').trim() }); },
  inputAmount(event) { this.resetRequest(); this.setData({ amount: String(event.detail.value || '').trim() }); },
  inputRemark(event) { this.resetRequest(); this.setData({ remark: event.detail.value }); },
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    return runtime.run(this, () => this.loadPage(this.data.pageNum + 1, false));
  },
  async loadPage(pageNum, reset) {
    const page = await this.client.myTransfers({ pageNum, pageSize: 10 });
    if (!page || !Array.isArray(page.records)) throw new Error('转账记录响应不合法');
    const records = page.records.map(this.toTransfer);
    const merged = reset ? records : (this.data.records || []).concat(records);
    this.setData({
      records: merged,
      total: page.total,
      pageNum,
      hasMore: records.length === 10 && merged.length < page.total,
    });
  },
  submit() {
    return runtime.run(this, async () => {
      const toMemberId = Number(this.data.toMemberId);
      if (!Number.isSafeInteger(toMemberId) || toMemberId <= 0) throw new Error('请输入正确的转入会员编号');
      const amount = this.data.amount;
      if (!/^\d{1,9}(\.\d{1,2})?$/.test(amount) || Number(amount) <= 0) throw new Error('请输入正确的转账金额');
      if (!this.requestNo) {
        this.requestNo = 'MINI-TF-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
      }
      this.toTransfer(await this.client.walletTransfer({
        toMemberId,
        amount,
        remark: this.data.remark,
        requestNo: this.requestNo,
      }));
      wx.showToast({ title: '转账成功', icon: 'success' });
      this.resetRequest();
      this.setData({ toMemberId: '', amount: '', remark: '' });
      await this.loadPage(1, true);
    });
  },
  toTransfer(value) {
    if (!value || typeof value.transferNo !== 'string' || value.status !== 'SUCCESS'
      || typeof value.amount !== 'string' || !MONEY.test(value.amount)) {
      throw new Error('转账数据不合法');
    }
    return { ...value, statusLabel: '转账成功' };
  },
});
