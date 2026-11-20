const config = require('../../config');
const { createPromotionHubClient } = require('../../services/promotionHub');

const DETAIL = {
  SECKILL: '/pages/seckill-detail/index?id=',
  COMBINATION: '/pages/combination-detail/index?id=',
  BARGAIN: '/pages/bargain-detail/index?id=',
  POINT: '/pages/point-mall-detail/index?id=',
};

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', sections: [] },
  onLoad() {
    this.client = createPromotionHubClient({
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error('活动读取失败，请重试')),
          }),
        ),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
    });
    if (this.data.ready) return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      const sections = await this.client.sections(10);
      this.setData({ sections: sections.filter((section) => section.items.length) });
    } catch (error) {
      this.setData({ sections: [], error: error.message || '活动读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  open(event) {
    const { type, id } = event.currentTarget.dataset;
    if (DETAIL[type]) {
      wx.navigateTo({ url: DETAIL[type] + encodeURIComponent(id) });
      return;
    }
    if (type === 'DISCOUNT' || type === 'REWARD') {
      wx.navigateTo({
        url: '/pages/promotion-rule/index?type=' + type + '&id=' + encodeURIComponent(id),
      });
    }
  },
  refresh() {
    return this.load();
  },
});
