const runtime = require('../../services/orderRuntime');

const STATUS_LABEL = Object.freeze({ PENDING: '待支付', PAID: '已到账', REFUNDED: '已退款' });
const MONEY = /^\d+\.\d{2}$/;

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false,
    packages: [], selectedPackage: null, recharges: [], total: 0, pageNum: 0, hasMore: false,
  },
  onLoad() { runtime.attach(this); this.requestNo = null; },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() { this.setData({ packages: [], selectedPackage: null, recharges: [], total: 0, pageNum: 0, hasMore: false }); },
  onSessionLost() { this.setData({ packages: [], selectedPackage: null, recharges: [], total: 0, pageNum: 0, hasMore: false, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  resetRequest() { this.requestNo = null; },
  reload() {
    return runtime.run(this, async () => {
      const packages = await this.client.rechargePackages();
      if (!Array.isArray(packages)) throw new Error('充值套餐响应不合法');
      this.setData({
        packages: packages.map(this.toPackage),
        selectedPackage: null,
      });
      this.resetRequest();
      await this.loadPage(1, true);
    });
  },
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    return runtime.run(this, () => this.loadPage(this.data.pageNum + 1, false));
  },
  choosePackage(event) {
    const index = Number(event.currentTarget.dataset.index);
    const value = this.data.packages[index];
    if (!value) { this.setData({ error: '套餐不存在，请刷新' }); return; }
    this.resetRequest();
    this.setData({ selectedPackage: value, error: '' });
  },
  async loadPage(pageNum, reset) {
    const page = await this.client.myRecharges({ pageNum, pageSize: 10 });
    if (!page || !Array.isArray(page.records)) throw new Error('充值单响应不合法');
    const records = page.records.map(this.toRecharge);
    const merged = reset ? records : (this.data.recharges || []).concat(records);
    this.setData({
      recharges: merged,
      total: page.total,
      pageNum,
      hasMore: records.length === 10 && merged.length < page.total,
    });
  },
  recharge() {
    return runtime.run(this, async () => {
      const pkg = this.data.selectedPackage;
      if (!pkg) throw new Error('请选择充值套餐');
      if (!this.requestNo) {
        this.requestNo = 'MINI-RC-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
      }
      const created = this.toRecharge(await this.client.createRecharge({ packageId: Number(pkg.id), requestNo: this.requestNo }));
      wx.showToast({ title: '充值单已创建', icon: 'success' });
      this.resetRequest();
      this.setData({ selectedPackage: null });
      await this.loadPage(1, true);
      return created;
    });
  },
  toPackage(value) {
    if (!value || !Number.isSafeInteger(value.id) || typeof value.payPrice !== 'string' || !MONEY.test(value.payPrice)
      || typeof value.bonusPrice !== 'string' || !MONEY.test(value.bonusPrice)) {
      throw new Error('充值套餐数据不合法');
    }
    return { ...value, total: (Number(value.payPrice) + Number(value.bonusPrice)).toFixed(2) };
  },
  toRecharge(value) {
    if (!value || typeof value.rechargeNo !== 'string' || !STATUS_LABEL[value.status]
      || typeof value.totalPrice !== 'string' || !MONEY.test(value.totalPrice)) {
      throw new Error('充值单数据不合法');
    }
    return { ...value, statusLabel: STATUS_LABEL[value.status] };
  },
});
