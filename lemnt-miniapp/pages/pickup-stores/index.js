const config = require('../../config');
const { createPickUpClient } = require('../../services/pickup');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', stores: [] },
  onLoad() {
    this.client = createPickUpClient({
      request: options => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('自提门店读取失败，请重试')) })),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
    });
    if (this.data.ready) return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      this.setData({ stores: await this.client.stores() });
    } catch (error) {
      this.setData({ stores: [], error: error.message || '自提门店读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  refresh() { return this.load(); },
});
