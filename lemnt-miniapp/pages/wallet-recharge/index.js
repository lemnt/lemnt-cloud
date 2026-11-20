const config = require('../../config');
const runtime = require('../../services/orderRuntime');
const { decodePrepay, loginCode, requestPayment } = require('../../services/payment');

const STATUS_LABEL = Object.freeze({
  PENDING: '待支付',
  PAID: '已到账',
  REFUNDING: '退款中',
  REFUNDED: '已退款',
});
const MONEY = /^\d+\.\d{2}$/;

Page({
  data: {
    ready: runtime.ready,
    loading: false,
    error: '',
    loggedIn: false,
    packages: [],
    selectedPackage: null,
    customAmount: '',
    recharges: [],
    total: 0,
    pageNum: 0,
    hasMore: false,
  },
  onLoad() {
    runtime.attach(this);
    this.requestNo = null;
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() {
    this.setData({
      packages: [],
      selectedPackage: null,
      customAmount: '',
      recharges: [],
      total: 0,
      pageNum: 0,
      hasMore: false,
    });
  },
  onSessionLost() {
    this.setData({
      packages: [],
      selectedPackage: null,
      customAmount: '',
      recharges: [],
      total: 0,
      pageNum: 0,
      hasMore: false,
      loggedIn: false,
      error: '会员已切换，请重新登录',
    });
  },
  login() {
    wx.navigateTo({ url: '/pages/member-login/index' });
  },
  resetRequest() {
    this.requestNo = null;
  },
  reload() {
    return runtime.run(this, async () => {
      const packages = await this.client.rechargePackages();
      if (!Array.isArray(packages)) throw new Error('充值套餐响应不合法');
      this.setData({
        packages: packages.map(this.toPackage),
        selectedPackage: null,
        customAmount: '',
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
    if (!value) {
      this.setData({ error: '套餐不存在，请刷新' });
      return;
    }
    this.resetRequest();
    this.setData({ selectedPackage: value, customAmount: '', error: '' });
  },
  inputAmount(event) {
    const value = event.detail.value;
    if (value !== this.data.customAmount) this.resetRequest();
    this.setData({ customAmount: value, selectedPackage: null, error: '' });
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
      const amount = this.data.customAmount;
      if (!pkg && !amount) throw new Error('请选择套餐或填写充值金额');
      if (!this.requestNo) {
        this.requestNo =
          'MINI-RC-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
      }
      const created = this.toRecharge(
        await this.client.createRecharge({
          ...(pkg ? { packageId: Number(pkg.id) } : { amount }),
          requestNo: this.requestNo,
        }),
      );
      wx.showToast({ title: '充值单已创建', icon: 'success' });
      this.resetRequest();
      this.setData({ selectedPackage: null, customAmount: '' });
      await this.loadPage(1, true);
      return created;
    });
  },
  payRecharge(event) {
    return runtime.run(this, async () => {
      const no = event.currentTarget.dataset.no;
      if (!this.data.recharges.some((item) => item.rechargeNo === no && item.status === 'PENDING'))
        throw new Error('充值单无需支付');
      if (!config.paymentAppKey) throw new Error('未配置支付应用');
      await this.memberClient.wechatLogin(await loginCode());
      const payment = decodePrepay(await this.client.rechargePrepay(no, config.paymentAppKey), no);
      if (payment.channel !== 'WECHAT' || !payment.params.paySign)
        throw new Error('微信支付参数不合法');
      await requestPayment(payment.params);
      await this.loadPage(1, true);
    });
  },
  toPackage(value) {
    if (
      !value ||
      !Number.isSafeInteger(value.id) ||
      typeof value.payPrice !== 'string' ||
      !MONEY.test(value.payPrice) ||
      typeof value.bonusPrice !== 'string' ||
      !MONEY.test(value.bonusPrice)
    ) {
      throw new Error('充值套餐数据不合法');
    }
    return { ...value, total: (Number(value.payPrice) + Number(value.bonusPrice)).toFixed(2) };
  },
  toRecharge(value) {
    if (
      !value ||
      typeof value.rechargeNo !== 'string' ||
      !STATUS_LABEL[value.status] ||
      typeof value.totalPrice !== 'string' ||
      !MONEY.test(value.totalPrice)
    ) {
      throw new Error('充值单数据不合法');
    }
    return { ...value, statusLabel: STATUS_LABEL[value.status] };
  },
});
