const config = require('../../config');
const { createSeckillClient } = require('../../services/seckill');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', configs: [], configId: '', rows: [], pageNum: 1, total: 0 },
  onLoad() {
    this.client = createSeckillClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
  },
  onShow() { if (this.data.ready) return this.reload(); },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '秒杀读取失败' }); } finally { this.setData({ loading: false }); }
  },
  async read(pageNum) {
    const result = await this.client.activities({ configId: this.data.configId, pageNum });
    this.setData({ rows: result.records, total: result.total, pageNum });
  },
  reload() {
    return this.run(async () => {
      const configs = await this.client.configs();
      let configId = this.data.configId;
      if (!configs.some((item) => item.id === configId)) {
        const now = await this.client.now();
        configId = now.config ? now.config.id : (configs[0] ? configs[0].id : '');
      }
      this.setData({ configs, configId, rows: [], pageNum: 1, total: 0 });
      if (configId) await this.read(1);
    });
  },
  selectConfig(e) {
    const configId = e.currentTarget.dataset.id;
    if (!this.data.configs.some((item) => item.id === configId)) throw Error('请刷新秒杀时段');
    return this.run(async () => {
      this.setData({ configId, rows: [], pageNum: 1, total: 0 });
      await this.read(1);
    });
  },
  page(e) {
    return this.run(async () => {
      const next = this.data.pageNum + Number(e.currentTarget.dataset.delta);
      if (next < 1 || (next - 1) * 20 >= this.data.total) return;
      await this.read(next);
    });
  },
  open(e) { wx.navigateTo({ url: '/pages/seckill-detail/index?id=' + e.currentTarget.dataset.id }); },
});
