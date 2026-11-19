const config = require('../../config');
const { createArticleClient } = require('../../services/articles');

Page({
  data: {
    rows: [],
    filters: ['全部', '热门', '推荐轮播'],
    filter: 0,
    categories: [{ name: '全部分类' }],
    category: 0,
    pageNum: 1,
    total: 0,
    loading: false,
    error: '',
  },
  onLoad() {
    this.client = createArticleClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({ ...options, success: resolve, fail: reject }),
        ),
    });
    return this.load(1);
  },
  async load(pageNum) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      if (this.data.categories.length === 1) {
        const categories = await this.client.categories();
        this.setData({ categories: [{ name: '全部分类' }, ...categories] });
      }
      const result = await this.client.page(pageNum, {
        categoryId: this.data.categories[this.data.category]?.id,
        recommendHot: this.data.filter === 1 ? true : undefined,
        recommendBanner: this.data.filter === 2 ? true : undefined,
      });
      this.setData({ rows: result.records, total: result.total, pageNum });
    } catch (error) {
      this.setData({ rows: [], total: 0, error: error.message || '读取文章失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  filter(e) {
    this.setData({ filter: Number(e.detail.value) });
    return this.load(1);
  },
  category(e) {
    this.setData({ category: Number(e.detail.value) });
    return this.load(1);
  },
  retry() {
    return this.load(this.data.pageNum);
  },
  turn(e) {
    const page = this.data.pageNum + Number(e.currentTarget.dataset.delta);
    if (page >= 1 && (page - 1) * 20 < this.data.total) return this.load(page);
  },
});
