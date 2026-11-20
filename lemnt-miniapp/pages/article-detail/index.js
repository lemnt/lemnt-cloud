const config = require('../../config');
const { createArticleClient } = require('../../services/articles');

Page({
  data: { article: null, loading: false, error: '' },
  onLoad(options) {
    this.key = { id: options.id, title: options.title };
    this.client = createArticleClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      request: (opts) =>
        new Promise((resolve, reject) => wx.request({ ...opts, success: resolve, fail: reject })),
    });
    return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      const article = await this.client.detail(this.key);
      this.setData({ article });
      this.client.browse(article.id).catch(() => {});
    } catch (error) {
      this.setData({ error: error.message || '文章读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
});
