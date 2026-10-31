const config = require('../../config');
const { createSeckillClient } = require('../../services/seckill');
const { createMemberClient } = require('../../services/member');
const { createPromotionOrderHelper } = require('../../services/promotionOrderPage');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', id: '', activity: null, address: null, order: null },
  onLoad(options) {
    this.client = createSeckillClient({
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
    this.memberClient = createMemberClient({ request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })), baseUrl: config.apiBaseUrl, ready: config.memberApiReady, getSession: () => getApp().globalData.memberSession, setSession: (value) => { getApp().globalData.memberSession = value; } });
    this.helper = createPromotionOrderHelper({ type: 'SECKILL', client: this.client, memberClient: this.memberClient });
    this.setData({ id: options.id || '' });
    if (this.data.ready) return this.reload();
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '秒杀详情读取失败' }); } finally { this.setData({ loading: false }); }
  },
  reload() {
    return this.run(async () => {
      if (!/^[1-9]\d{0,18}$/.test(this.data.id)) throw Error('秒杀活动编号不合法');
      this.setData({ activity: await this.client.activity(this.data.id) });
    });
  },
  onShow() { if (this.data.ready) return this.loadAddress(); },
  async loadAddress() {
    try { this.setData({ address: await this.helper.loadAddress() }); }
    catch (e) { this.setData({ error: e.message || '收货地址读取失败' }); }
  },
  order() {
    return this.run(async () => {
      if (!this.data.activity) throw Error('请先刷新秒杀详情');
      const address = await this.helper.loadAddress();
      if (!address) throw Error('请先在会员中心添加收货地址');
      const product = this.data.activity.products[0];
      if (!product) throw Error('秒杀商品不存在');
      const order = await this.helper.submit({ activityId: this.data.activity.id, skuId: product.skuId, quantity: 1, addressId: address.id });
      this.setData({ address, order });
    });
  },
});
