const config = require("../../config");
const {
  createProductInteractionClient,
} = require("../../services/productInteractions");
Page({
  data: {
    ready: config.memberApiReady,
    loggedIn: false,
    loading: false,
    error: "",
    rows: [],
    selected: [],
    pageNum: 1,
    total: 0,
  },
  onLoad() {
    this.client = createProductInteractionClient({
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error("网络结果未确认，请刷新核验")),
          }),
        ),
    });
  },
  onShow() {
    this.setData({
      rows: [],
      selected: [],
      total: 0,
      pageNum: 1,
      error: "",
      loggedIn: !!getApp().globalData.memberSession,
    });
    if (this.data.loggedIn && this.data.ready) return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: "" });
    try {
      const result = await this.client.history(this.data.pageNum);
      this.setData({ rows: result.records, selected: [], total: result.total });
    } catch (error) {
      this.setData({ rows: [], selected: [], total: 0, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  async remove(event) {
    if (this.data.loading) return;
    const spuId = event.currentTarget.dataset.id;
    let failure = "";
    this.setData({ loading: true, error: "" });
    try {
      await this.client.deleteHistory([spuId]);
      this.setData({ pageNum: 1 });
    } catch (error) {
      failure = error.message;
    } finally {
      this.setData({ loading: false });
    }
    await this.load();
    if (failure) this.setData({ error: failure });
  },
  async page(event) {
    if (this.data.loading) return;
    const next = this.data.pageNum + Number(event.currentTarget.dataset.delta);
    if (next < 1 || (next - 1) * 20 >= this.data.total) return;
    this.setData({ pageNum: next });
    return this.load();
  },
  selection(event) {
    if (this.data.loading) return;
    const selected = event.detail.value;
    this.setData({
      selected,
      rows: this.data.rows.map((row) => ({
        ...row,
        checked: selected.includes(row.spuId),
      })),
    });
  },
  selectAll() {
    if (this.data.loading) return;
    const selected =
      this.data.selected.length === this.data.rows.length
        ? []
        : this.data.rows.map((row) => row.spuId);
    this.setData({
      selected,
      rows: this.data.rows.map((row) => ({
        ...row,
        checked: selected.includes(row.spuId),
      })),
    });
  },
  async removeSelected() {
    if (this.data.loading || !this.data.selected.length) return;
    const selected = [...this.data.selected];
    let failure = "";
    this.setData({ loading: true, error: "" });
    try {
      await this.client.deleteHistory(selected);
      this.setData({ pageNum: 1 });
    } catch (error) {
      failure = "部分操作结果未确认，请核验列表：" + error.message;
    } finally {
      this.setData({ loading: false, selected: [] });
    }
    await this.load();
    if (failure) this.setData({ error: failure });
  },
  product(event) {
    wx.navigateTo({
      url:
        "/pages/product/index?id=" +
        encodeURIComponent(event.currentTarget.dataset.id),
    });
  },
  login() {
    wx.navigateTo({ url: "/pages/member-login/index" });
  },
  async clear() {
    if (this.data.loading) return;
    const token = getApp().globalData.memberSession?.accessToken;
    const answer = await new Promise((resolve) =>
      wx.showModal({
        title: "清空足迹",
        content: "确认清空本人浏览足迹？",
        success: resolve,
      }),
    );
    if (!answer.confirm) return;
    if (token !== getApp().globalData.memberSession?.accessToken) {
      this.setData({ rows: [], error: "会员已切换，请重新确认" });
      return;
    }
    let failure = "";
    this.setData({ loading: true, error: "" });
    try {
      await this.client.clearHistory();
      this.setData({ pageNum: 1 });
    } catch (error) {
      failure = error.message;
    } finally {
      this.setData({ loading: false });
    }
    await this.load();
    if (failure) this.setData({ error: failure });
  },
});
