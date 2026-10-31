const runtime = require('../../services/orderRuntime');

Page({
  data: {
    ready: runtime.ready, loading: false, error: '', loggedIn: false, wallet: null,
    records: [], total: 0, pageNum: 0, hasMore: false,
  },
  onLoad() { runtime.attach(this); },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (loggedIn && runtime.requireLogin()) return this.reload();
  },
  onHide() { this.setData({ records: [], total: 0, pageNum: 0, hasMore: false }); },
  onSessionLost() { this.setData({ wallet: null, records: [], total: 0, pageNum: 0, hasMore: false, loggedIn: false, error: '会员已切换，请重新登录' }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
  reload() {
    return runtime.run(this, async () => {
      const wallet = this.toWallet(await this.client.wallet());
      this.setData({ wallet });
      await this.loadPage(1, true);
    });
  },
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    return runtime.run(this, () => this.loadPage(this.data.pageNum + 1, false));
  },
  async loadPage(pageNum, reset) {
    const page = await this.client.walletTransactions({ pageNum, pageSize: 10 });
    if (!page || !Array.isArray(page.records)) throw new Error('钱包流水响应不合法');
    const records = page.records.map(this.toTransaction);
    const merged = reset ? records : this.data.records.concat(records);
    this.setData({
      records: merged,
      total: page.total,
      pageNum,
      hasMore: records.length === 10 && merged.length < page.total,
    });
  },
  toWallet(value) {
    if (!value || typeof value.balance !== 'string' || !/^\d+\.\d{2}$/.test(value.balance)) {
      throw new Error('钱包数据不合法');
    }
    for (const key of ['freezePrice', 'totalExpense', 'totalRecharge']) {
      if (typeof value[key] !== 'string' || !/^\d+\.\d{2}$/.test(value[key])) {
        throw new Error('钱包数据不合法');
      }
    }
    return value;
  },
  toTransaction(value) {
    if (!value || typeof value.transactionNo !== 'string'
      || typeof value.amount !== 'string' || !/^-?\d+\.\d{2}$/.test(value.amount)
      || typeof value.balanceAfter !== 'string' || !/^\d+\.\d{2}$/.test(value.balanceAfter)) {
      throw new Error('钱包流水数据不合法');
    }
    return { ...value, increase: !value.amount.startsWith('-') };
  },
});
