const config = require('../../config');
const { createCombinationClient } = require('../../services/combination');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', loggedIn: false, id: '', detail: null, rows: [], pageNum: 1, total: 0 },
  onLoad(options) {
    this.client = createCombinationClient({
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
    this.setData({ id: options.id || '' });
  },
  onShow() {
    this.setData({ loggedIn: !!getApp().globalData.memberSession });
    if (!this.data.ready) return;
    if (this.data.id) return this.loadDetail();
    return this.reload();
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '拼团记录读取失败' }); } finally { this.setData({ loading: false }); }
  },
  async read(pageNum) {
    const result = await this.client.records({ pageNum });
    this.setData({ rows: result.records, total: result.total, pageNum });
  },
  reload() { return this.run(() => this.read(1)); },
  loadDetail() {
    return this.run(async () => {
      if (!/^[1-9]\d{0,18}$/.test(this.data.id)) throw Error('拼团记录编号不合法');
      this.setData({ detail: await this.client.record(this.data.id) });
    });
  },
  page(e) {
    if (this.data.id) return;
    return this.run(async () => {
      const next = this.data.pageNum + Number(e.currentTarget.dataset.delta);
      if (next < 1 || (next - 1) * 20 >= this.data.total) return;
      await this.read(next);
    });
  },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
});
