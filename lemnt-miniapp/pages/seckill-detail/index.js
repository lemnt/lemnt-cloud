const config = require('../../config');
const { createSeckillClient } = require('../../services/seckill');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', id: '', activity: null },
  onLoad(options) {
    this.client = createSeckillClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
    this.setData({ id: options.id || '' });
    if (this.data.ready) return this.reload();
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '秒杀详情读取失败' }); } finally { this.setData({ loading: false }); }
  },
  reload() {
    return this.run(async () => {
      if (!/^[1-9]\d{0,18}$/.test(this.data.id)) throw Error('秒杀活动编号不合法');
      this.setData({ activity: await this.client.activity(this.data.id) });
    });
  },
  order() {
    if (!this.data.activity) throw Error('请先刷新秒杀详情');
    wx.showToast({ title: '活动下单链路待订单批次接入', icon: 'none' });
  },
});
