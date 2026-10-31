const config = require('../../config');
const { createBargainClient } = require('../../services/bargain');
const { createMemberClient } = require('../../services/member');
const { createPromotionOrderHelper } = require('../../services/promotionOrderPage');

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', activityId: '', recordId: '', activity: null, detail: null, loggedIn: false, address: null, order: null },
  onLoad(options) {
    this.client = createBargainClient({
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })),
    });
    this.memberClient = createMemberClient({ request: (options) => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('请求未确认，请刷新核验')) })), baseUrl: config.apiBaseUrl, ready: config.memberApiReady, getSession: () => getApp().globalData.memberSession, setSession: (value) => { getApp().globalData.memberSession = value; } });
    this.helper = createPromotionOrderHelper({ type: 'BARGAIN', client: this.client, memberClient: this.memberClient });
    this.setData({ activityId: options.activityId || '', recordId: options.recordId || '' });
  },
  onShow() {
    this.setData({ loggedIn: !!getApp().globalData.memberSession });
    if (this.data.ready) {
      this.loadAddress();
      return this.reload();
    }
  },
  async loadAddress() {
    try { this.setData({ address: await this.helper.loadAddress() }); }
    catch (e) { this.setData({ error: e.message || '收货地址读取失败' }); }
  },
  async run(work) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try { await work(); } catch (e) { this.setData({ error: e.message || '砍价详情读取失败' }); } finally { this.setData({ loading: false }); }
  },
  reload() {
    return this.run(async () => {
      if (this.data.recordId) {
        this.setData({ detail: await this.client.record(this.data.recordId), activity: null });
        return;
      }
      if (!/^[1-9]\d{0,18}$/.test(this.data.activityId)) throw Error('砍价活动编号不合法');
      this.setData({ activity: await this.client.activity(this.data.activityId), detail: null });
    });
  },
  start() {
    if (!this.data.loggedIn) return this.login();
    return this.run(async () => {
      if (!this.data.activityId) throw Error('请先选择砍价活动');
      const created = await this.client.createRecord(this.data.activityId);
      this.setData({ recordId: created.id });
      this.setData({ detail: await this.client.record(created.id) });
    });
  },
  help() {
    if (!this.data.loggedIn) return this.login();
    return this.run(async () => {
      if (!this.data.recordId) throw Error('请先查看砍价记录');
      await this.client.help(this.data.recordId);
      this.setData({ detail: await this.client.record(this.data.recordId) });
    });
  },
  buy() {
    return this.run(async () => {
      if (!this.data.detail || this.data.detail.record.status !== 2) throw Error('砍价尚未成功');
      const address = await this.helper.loadAddress();
      if (!address) throw Error('请先在会员中心添加收货地址');
      const order = await this.helper.submit({ recordId: this.data.detail.record.id, skuId: this.data.detail.record.skuId, quantity: 1, addressId: address.id });
      this.setData({ address, order });
    });
  },
  login() { wx.navigateTo({ url: '/pages/member-login/index' }); },
});
