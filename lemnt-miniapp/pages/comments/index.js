const config = require("../../config");
const { createCommentClient } = require("../../services/comments");
const { id } = require("../../services/storefront");
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: "",
    rows: [],
    type: 0,
    pageNum: 1,
    total: 0,
    types: ["全部", "好评", "中评", "差评"],
  },
  onLoad(options) {
    try {
      this.spuId = id(options.spuId);
    } catch (error) {
      this.setData({ error: error.message });
      return;
    }
    this.client = createCommentClient({
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error("网络请求失败")),
          }),
        ),
    });
    return this.load();
  },
  async load() {
    if (this.data.loading || !this.spuId) return;
    this.setData({ loading: true, error: "" });
    try {
      const result = await this.client.page({
        spuId: this.spuId,
        type: this.data.type,
        pageNum: this.data.pageNum,
      });
      this.setData({ rows: result.records, total: result.total });
    } catch (error) {
      this.setData({ rows: [], total: 0, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  type(event) {
    if (this.data.loading) return;
    const type = Number(event.detail.value);
    if (!Number.isInteger(type) || type < 0 || type > 3) return;
    this.setData({ type, pageNum: 1 });
    return this.load();
  },
  page(event) {
    if (this.data.loading) return;
    const next = this.data.pageNum + Number(event.currentTarget.dataset.delta);
    if (next < 1 || (next - 1) * 20 >= this.data.total) return;
    this.setData({ pageNum: next });
    return this.load();
  },
});
