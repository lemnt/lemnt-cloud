const config = require('../../config');
const { createTradeClient } = require('../../services/trade');
const { createCheckoutFlow } = require('../../services/checkoutFlow');
Page({
  data: { ready: config.memberApiReady, loading: false, error: '', rows: [], quote: null, receipt: null, pending: false },
  onLoad() {
    const session = () => getApp().globalData.memberSession;
    this.client = createTradeClient({
      request: options => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: reject })),
      baseUrl: config.apiBaseUrl, ready: config.memberApiReady,
      getMemberToken: () => session()?.accessToken,
    });
    this.flow = createCheckoutFlow({ client: this.client, currentMember: () => session()?.memberId,
      storage: { get: key => wx.getStorageSync(key), set: (key,value) => wx.setStorageSync(key,value), remove: key => wx.removeStorageSync(key) },
      newRequestNo: () => 'MINI-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2) });
  },
  async action(run) {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    const member = getApp().globalData.memberSession?.memberId;
    try { await run(member); }
    catch (error) { this.setData({ error: error.message || '请求失败，请重试' }); }
    finally { this.setData({ loading: false }); }
  },
  loadCart() { return this.action(async member => {
    const rows = await this.client.cart();
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    if (!Array.isArray(rows)) throw new Error('购物车响应格式不合法');
    const normalized = rows.map(row => {
      const id = typeof row.productId === 'number' && Number.isSafeInteger(row.productId) ? String(row.productId) : row.productId;
      if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || typeof row.selected !== 'boolean' || !Number.isInteger(row.quantity) || row.quantity < 1) throw new Error('购物车响应格式不合法');
      return { ...row, productId: id };
    });
    this.rowsMember = member;
    this.setData({ rows: normalized, quote: null, receipt: null, pending: !!this.flow.pending() });
  }); },
  preview() { return this.action(async member => {
    if (member !== this.rowsMember) throw new Error("会员已切换，请重新加载购物车");
    if (this.flow.pending()) throw new Error('存在未确认的提交，请先重试原请求');
    const productIds = this.data.rows.filter(row => row.selected && row.sellable).map(row => row.productId);
    if (!productIds.length) throw new Error('购物车中没有已选且可售的商品');
    const quote = await this.client.quote({ productIds });
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    if (!quote || !/^[a-f0-9]{64}$/.test(quote.quoteToken) || ['originalAmount','discountAmount','payableAmount'].some(key => typeof quote[key] !== 'string' || !/^\d+\.\d{2}$/.test(quote[key]))) throw new Error('报价响应不合法');
    this.quoteMember = member;
    this.quoteInput = { productIds, quoteToken: quote.quoteToken };
    this.setData({ quote });
  }); },
  submit() { return this.action(async member => {
    const pending = this.flow.pending();
    if (!pending && member !== this.quoteMember) throw new Error("会员已切换，请重新预览");
    if (!pending && (!this.data.quote || /^0+\.00$/.test(this.data.quote.payableAmount))) throw new Error('请先取得有效报价；暂不支持零元下单');
    try {
      const receipt = await this.flow.submit(this.quoteInput);
      this.setData({ receipt, pending: false, quote: null }); this.quoteInput = null;
    } catch (error) { this.setData({ pending: !!this.flow.pending() }); throw error; }
  }); },
  onHide() { this.setData({ rows: [], quote: null, receipt: null, error: '' }); this.quoteInput = null; },
});
