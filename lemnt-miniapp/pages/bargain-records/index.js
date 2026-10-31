const config = require('../../config');
const { createBargainClient } = require('../../services/bargain');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', loggedIn: false, rows: [], pageNum: 1, total: 0 },
  onLoad() {
    this.client = createBargainClient({
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
  },
  onShow() {
    this.setData({ loggedIn: !!getApp().globalData.memberSession });
    if (this.data.ready && this.data.loggedIn) return this.reload();
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '砍价记录读取失败' }); } finally { this.setData({ loading: false }); }
  },
  async read(pageNum) {
    const result = await this.client.myRecords({ pageNum });
    this.setData({ rows: result.records, total: result.total, pageNum });
  },
  reload() { return this.run(() => this.read(1)); },
  page(e) {
    return this.run(async () => {
      const next = this.data.pageNum + Number(e.currentTarget.dataset.delta);
      if (next < 1 || (next - 1) * 20 >= this.data.total) return;
      await this.read(next);
    });
  },
  open(e) { wx.navigateTo({ url: '/pages/bargain-detail/index?recordId=' + e.currentTarget.dataset.id }); },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
});
