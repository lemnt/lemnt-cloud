const config = require("../../config");
const { createArticleClient } = require("../../services/articles");
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: "",
    categoryError: "",
    categories: [{ id: null, name: "全部" }],
    categoryIndex: 0,
    pageNum: 1,
    rows: [],
    total: 0,
  },
  onLoad() {
    this.client = createArticleClient({
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error("网络请求失败，请重试")),
          }),
        ),
    });
    return this.reload();
  },
  async reload() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: "", categoryError: "" });
    try {
      const categories = await this.client.categories();
      this.setData({
        categories: [{ id: null, name: "全部" }, ...categories],
        categoryIndex: 0,
        pageNum: 1,
      });
    } catch (error) {
      this.setData({
        categories: [{ id: null, name: "全部" }],
        categoryIndex: 0,
        categoryError: error.message,
      });
    } finally {
      this.setData({ loading: false });
    }
    return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: "" });
    try {
      const result = await this.client.page({
        categoryId: this.data.categories[this.data.categoryIndex]?.id,
        pageNum: this.data.pageNum,
      });
      this.setData({ rows: result.records, total: result.total });
    } catch (error) {
      this.setData({ rows: [], total: 0, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  category(event) {
    if (this.data.loading) return;
    const index = Number(event.detail.value);
    if (!Number.isSafeInteger(index) || !this.data.categories[index]) return;
    this.setData({ categoryIndex: index, pageNum: 1 });
    return this.load();
  },
  page(event) {
    if (this.data.loading) return;
    const next = this.data.pageNum + Number(event.currentTarget.dataset.delta);
    if (next < 1 || (next - 1) * 20 >= this.data.total) return;
    this.setData({ pageNum: next });
    return this.load();
  },
  read(event) {
    wx.navigateTo({
      url:
        "/pages/article/index?id=" +
        encodeURIComponent(event.currentTarget.dataset.id),
    });
  },
});
