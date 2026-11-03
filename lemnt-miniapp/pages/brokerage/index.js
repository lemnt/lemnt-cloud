const config = require('../../config');
const runtime = require('../../services/orderRuntime');
const { createBrokerageClient } = require('../../services/brokerage');

const TYPE_LABEL = { 1: '钱包', 2: '银行卡', 3: '微信收款码', 4: '支付宝收款码' };
const STATUS_LABEL = { 0: '待结算', 1: '已结算', 2: '已取消' };
const WITHDRAW_LABEL = {
  0: '审核中',
  10: '审核通过',
  11: '提现成功',
  20: '审核不通过',
  21: '提现失败',
};

Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: '',
    success: '',
    loggedIn: false,
    user: null,
    records: [],
    withdraws: [],
    bindId: '',
    price: '',
    type: 1,
    userName: '',
    userAccount: '',
    qrCodeUrl: '',
    bankName: '',
    bankAddress: '',
  },
  onLoad() {
    runtime.attach(this);
    this.brokerage = createBrokerageClient({
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error('网络未确认，请刷新核验')),
          }),
        ),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
    });
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) this.reload();
  },
  onSessionLost() {
    this.setData({
      user: null,
      records: [],
      withdraws: [],
      loggedIn: false,
      error: '会员已切换，请重新登录',
    });
  },
  login() {
    wx.navigateTo({ url: '/pages/member-login/index' });
  },
  reload() {
    return runtime.run(this, async () => {
      const user = await this.brokerage.user();
      const records = await this.brokerage.records();
      const withdraws = await this.brokerage.withdraws();
      this.setData({
        user,
        records: records.records.map((row) => ({
          ...row,
          statusLabel: STATUS_LABEL[row.status] || row.status,
        })),
        withdraws: withdraws.records.map((row) => ({
          ...row,
          typeLabel: TYPE_LABEL[row.type] || row.type,
          statusLabel: WITHDRAW_LABEL[row.status] || row.status,
        })),
      });
    });
  },
  input(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value });
  },
  chooseType(event) {
    this.setData({ type: Number(event.detail.value) + 1 });
  },
  bind() {
    return runtime.run(this, async () => {
      if (!this.data.bindId.trim()) throw new Error('请输入推广员编号');
      await this.brokerage.bind(this.data.bindId.trim());
      this.setData({ bindId: '', success: '推广员已绑定' });
      await this.reload();
    });
  },
  withdraw() {
    return runtime.run(this, async () => {
      await this.brokerage.createWithdraw({
        price: this.data.price,
        type: this.data.type,
        userName: this.data.userName,
        userAccount: this.data.userAccount,
        qrCodeUrl: this.data.qrCodeUrl,
        bankName: this.data.bankName,
        bankAddress: this.data.bankAddress,
      });
      this.setData({ price: '', success: '提现申请已提交' });
      await this.reload();
    });
  },
  refresh() {
    return this.reload();
  },
});
