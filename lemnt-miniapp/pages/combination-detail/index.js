const config = require('../../config');
const { createCombinationClient } = require('../../services/combination');
const { createMemberClient } = require('../../services/member');
const { createPromotionOrderHelper } = require('../../services/promotionOrderPage');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', id: '', activity: null, heads: [], address: null, order: null },
  onLoad(options) {
    this.client = createCombinationClient({
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
    this.memberClient = createMemberClient({ request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })), baseUrl: config.apiBaseUrl, ready: config.memberApiReady, getSession: () => getApp().globalData.memberSession, setSession: (value) => { getApp().globalData.memberSession = value; } });
    this.helper = createPromotionOrderHelper({ type: 'COMBINATION', client: this.client, memberClient: this.memberClient });
    this.setData({ id: options.id || '' });
    if (this.data.ready) return this.reload();
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '拼团详情读取失败' }); } finally { this.setData({ loading: false }); }
  },
  reload() {
    return this.run(async () => {
      if (!/^[1-9]\d{0,18}$/.test(this.data.id)) throw Error('拼团活动编号不合法');
      const activity = await this.client.activity(this.data.id);
      const heads = await this.client.heads(this.data.id);
      this.setData({ activity, heads });
    });
  },
  onShow() { if (this.data.ready) return this.loadAddress(); },
  async loadAddress() {
    try { this.setData({ address: await this.helper.loadAddress() }); }
    catch (e) { this.setData({ error: e.message || '收货地址读取失败' }); }
  },
  join(e) {
    return this.run(async () => {
      if (!this.data.activity) throw Error('请先刷新拼团详情');
      const address = await this.helper.loadAddress();
      if (!address) throw Error('请先在会员中心添加收货地址');
      const product = this.data.activity.products[0];
      if (!product) throw Error('拼团商品不存在');
      const headId = e && e.currentTarget && e.currentTarget.dataset && e.currentTarget.dataset.head ? e.currentTarget.dataset.head : undefined;
      const order = await this.helper.submit({ activityId: this.data.activity.id, skuId: product.skuId, quantity: 1, addressId: address.id, headId });
      this.setData({ address, order });
    });
  },
  openRecord(e) { wx.navigateTo({ url: '/pages/combination-records/index?id=' + e.currentTarget.dataset.id }); },
});
