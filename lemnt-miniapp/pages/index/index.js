const config = require('../../config');
const { createTradeClient } = require('../../services/trade');
Page({
  data: { ready: config.memberApiReady, loading: false, error: '', count: null },
  async loadCart() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      const client = createTradeClient({
        request: options => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: reject })),
        baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
        getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      });
      const rows = await client.cart();
      if (!Array.isArray(rows)) throw new Error('购物车响应格式不合法');
      this.setData({ count: rows.length });
    } catch (error) {
      this.setData({ error: error.message || '网络请求失败' });
    } finally { this.setData({ loading: false }); }
  },
});
