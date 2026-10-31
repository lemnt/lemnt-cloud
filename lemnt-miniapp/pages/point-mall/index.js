const config = require('../../config');
const { createPointClient } = require('../../services/point');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', rows: [], pageNum: 1, total: 0 },
  onLoad() {
    this.client = createPointClient({
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
  },
  onShow() { if (this.data.ready) return this.reload(); },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '积分商城读取失败' }); } finally { this.setData({ loading: false }); }
  },
  async read(pageNum) {
    const result = await this.client.activities({ pageNum });
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
  open(e) { wx.navigateTo({ url: '/pages/point-mall-detail/index?id=' + e.currentTarget.dataset.id }); },
});
