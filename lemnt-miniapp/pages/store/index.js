const config = require('../../config');
const { createStoreClient } = require('../../services/storefront');
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: '',
    rows: [],
    banners: [],
    categories: [{ id: null, name: '全部分类' }],
    categoryIndex: 0,
    sortIndex: 0,
    sorts: [
      { name: '综合排序' },
      { name: '价格从低到高', field: 'price', asc: true },
      { name: '价格从高到低', field: 'price', asc: false },
      { name: '销量从高到低', field: 'salesCount', asc: false },
      { name: '销量从低到高', field: 'salesCount', asc: true },
      { name: '最新创建', field: 'createTime', asc: false },
      { name: '最早创建', field: 'createTime', asc: true },
    ],
    name: '',
    pageNum: 1,
    total: 0,
  },
  onLoad() {
    this.client = createStoreClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      request: (o) =>
        new Promise((resolve, reject) =>
          wx.request({ ...o, success: resolve, fail: () => reject(Error('商品读取失败，请重试')) }),
        ),
    });
    if (this.data.ready) {
      this.loadBanners();
      return this.load(true);
    }
  },
  input(e) {
    this.setData({ name: e.detail.value });
  },
  async load(reset = false) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      if (this.data.categories.length === 1) {
        const categories = await this.client.categories();
        const byId = new Map(categories.map((c) => [c.id, c]));
        this.setData({
          categories: [
            { id: null, name: '全部分类' },
            ...categories.map((c) => ({
              ...c,
              name: c.parentId ? (byId.get(c.parentId)?.name || '分类') + ' / ' + c.name : c.name,
            })),
          ],
        });
      }
      const pageNum = reset ? 1 : this.data.pageNum;
      const categoryId = this.data.categories[this.data.categoryIndex]?.id;
      const sort = this.data.sorts[this.data.sortIndex];
      const result = await this.client.products({
        name: this.data.name,
        categoryId,
        pageNum,
        sortField: sort.field,
        sortAsc: sort.asc,
      });
      this.setData({ rows: result.records, total: result.total, pageNum });
    } catch (e) {
      this.setData({ rows: [], total: 0, error: e.message || '商品读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  search() {
    return this.load(true);
  },
  category(e) {
    if (this.data.loading) return;
    this.setData({ categoryIndex: Number(e.detail.value) });
    return this.load(true);
  },
  sort(e) {
    if (this.data.loading) return;
    this.setData({ sortIndex: Number(e.detail.value) });
    return this.load(true);
  },
  async page(e) {
    if (this.data.loading) return;
    const next = this.data.pageNum + Number(e.currentTarget.dataset.delta);
    if (next < 1 || (next - 1) * 20 >= this.data.total) return;
    this.setData({ pageNum: next });
    return this.load();
  },
  async loadBanners() {
    try {
      this.setData({ banners: await this.client.banners(1) });
    } catch (e) {
      this.setData({ banners: [] });
    }
  },
  banner(e) {
    const url = e.currentTarget.dataset.url;
    if (/^\/pages\//.test(url)) {
      wx.navigateTo({ url });
      return;
    }
    wx.setClipboardData({ data: url, success: () => wx.showToast({ title: '链接已复制' }) });
  },
  product(e) {
    wx.navigateTo({
      url: '/pages/product/index?id=' + encodeURIComponent(e.currentTarget.dataset.id),
    });
  },
});
