const config = require('../../config');
const { createPromotionHubClient } = require('../../services/promotionHub');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', type: '', id: '', detail: null },
  onLoad(query) {
    this.client = createPromotionHubClient({
      request: options => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('活动读取失败，请重试')) })),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
    });
    this.setData({ type: query.type === 'REWARD' ? 'REWARD' : 'DISCOUNT', id: query.id || '' });
    if (this.data.ready) return this.load();
  },
  async load() {
    if (!/^[1-9]\d{0,18}$/.test(this.data.id)) { this.setData({ error: '活动编号不合法' }); return; }
    this.setData({ loading: true, error: '' });
    try {
      const detail = this.data.type === 'REWARD'
        ? await this.client.reward(this.data.id) : await this.client.discount(this.data.id);
      this.setData({ detail });
    } catch (error) {
      this.setData({ detail: null, error: error.message || '活动读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  refresh() { return this.load(); },
});
