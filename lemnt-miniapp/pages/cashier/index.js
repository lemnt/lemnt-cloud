const config = require('../../config');
const runtime = require('../../services/orderRuntime');
const { decodeChannels, decodePrepay } = require('../../services/payment');
const { decodePaymentStatus } = require('../../services/paymentStatus');

Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: '',
    loggedIn: false,
    orderNo: '',
    appKey: '',
    channels: [],
    channel: '',
    prepay: null,
    status: null,
    paid: false,
    message: '',
  },
  onLoad(query) {
    runtime.attach(this);
    this.setData({
      orderNo: decodeURIComponent(query.orderNo || ''),
      appKey: decodeURIComponent(query.appKey || config.paymentAppKey || ''),
      message: '',
    });
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (!loggedIn) return;
    if (runtime.requireLogin()) {
      return this.run(async () => {
        if (!this.data.orderNo) throw new Error('缺少订单编号');
        if (!this.data.appKey) throw new Error('未配置支付应用');
        await this.loadChannels();
        await this.loadStatus();
      });
    }
  },
  onSessionLost() {
    this.setData({
      channels: [],
      prepay: null,
      status: null,
      paid: false,
      loggedIn: false,
      error: '会员已切换，请重新登录',
    });
  },
  login() {
    wx.navigateTo({ url: '/pages/member-login/index' });
  },
  run(work) {
    return runtime.run(this, work);
  },
  async loadChannels() {
    const channels = decodeChannels(await this.client.paymentChannels(this.data.appKey));
    const channel =
      this.data.channel && channels.some((item) => item.code === this.data.channel)
        ? this.data.channel
        : channels[0]
          ? channels[0].code
          : '';
    this.setData({ channels, channel });
  },
  async loadStatus() {
    const status = decodePaymentStatus(
      await this.client.paymentStatus(this.data.orderNo),
      this.data.orderNo,
    );
    this.setData({
      status,
      paid: status.localPaymentStatus === 'SUCCESS',
      message: status.localPaymentStatus === 'SUCCESS' ? '支付成功' : status.label,
    });
  },
  chooseChannel(event) {
    const channel = event.currentTarget.dataset.code;
    if (!this.data.channels.some((item) => item.code === channel)) return;
    this.setData({ channel, prepay: null, message: '' });
  },
  pay() {
    return this.run(async () => {
      if (!this.data.channel) throw new Error('请选择支付渠道');
      const prepay = decodePrepay(
        await this.client.prepay({
          orderNo: this.data.orderNo,
          appKey: this.data.appKey,
          channel: this.data.channel,
        }),
        this.data.orderNo,
      );
      this.setData({
        prepay,
        message:
          prepay.channel === 'SANDBOX'
            ? '沙箱支付单已创建，请点击模拟支付'
            : prepay.payUrl
              ? '请在浏览器打开支付链接完成支付'
              : '该渠道尚未接入真实支付',
      });
    });
  },
  sandboxPay() {
    return this.run(async () => {
      if (!this.data.prepay || this.data.prepay.channel !== 'SANDBOX')
        throw new Error('请先创建沙箱支付单');
      await this.client.sandboxPay(this.data.prepay.paymentNo);
      await this.loadStatus();
    });
  },
  refreshStatus() {
    return this.run(() => this.loadStatus());
  },
  goOrder() {
    wx.navigateTo({
      url: '/pages/order-detail/index?orderNo=' + encodeURIComponent(this.data.orderNo),
    });
  },
});
