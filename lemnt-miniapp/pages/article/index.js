const config = require("../../config");
const { createArticleClient } = require("../../services/articles");
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: "",
    countError: "",
    article: null,
  },
  onLoad(options) {
    this.query = {
      id: options.id || undefined,
      title: options.title || undefined,
    };
    this.client = createArticleClient({
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error("网络结果未确认")),
          }),
        ),
    });
    return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: "", countError: "", article: null });
    try {
      const article = await this.client.detail(this.query);
      this.setData({ article });
      if (article) {
        wx.setNavigationBarTitle({ title: article.title });
        try {
          await this.client.browse(article.id);
        } catch (error) {
          this.setData({
            countError: "正文已加载，浏览次数更新未确认：" + error.message,
          });
        }
      }
    } catch (error) {
      this.setData({ article: null, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  product() {
    const id = this.data.article?.spuId;
    if (id)
      wx.navigateTo({
        url: "/pages/product/index?id=" + encodeURIComponent(id),
      });
  },
});
