const config = require('../../config');
const { couponId, ownedCoupons } = require('../../services/coupons');
const { createTradeClient } = require('../../services/trade');
const { createCheckoutFlow } = require('../../services/checkoutFlow');
Page({
  data: { ready: config.memberApiReady, loading: false, error: '', rows: [], quote: null, receipt: null, pending: false, coupons: [], selectedCoupon: null },
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
    catch (error) {
      if (member !== getApp().globalData.memberSession?.memberId) {
        this.setData({ rows: [], quote: null, receipt: null, pending: false, coupons: [], selectedCoupon: null });
        this.quoteInput = null; this.rowsMember = null; this.quoteMember = null; this.receiptMember = null;
      }
      this.setData({ error: error.message || '请求失败，请重试' });
    }
    finally { this.setData({ loading: false }); }
  },
  loadCart() { return this.action(member => this.readCart(member)); },
  async readCart(member) {
    const rows = await this.client.cart();
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    if (!Array.isArray(rows)) throw new Error('购物车响应格式不合法');
    const normalized = rows.map(row => {
      const id = typeof row.productId === 'number' && Number.isSafeInteger(row.productId) ? String(row.productId) : row.productId;
      if (typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id) || typeof row.selected !== 'boolean' || !Number.isInteger(row.quantity) || row.quantity < 1) throw new Error('购物车响应格式不合法');
      return { ...row, productId: id };
    });
    this.rowsMember = member;
    this.setData({ rows: normalized, quote: null, pending: !!this.flow.pending() });
  },
  preview() { return this.action(async member => {
    if (member !== this.rowsMember) throw new Error("会员已切换，请重新加载购物车");
    if (this.flow.pending()) throw new Error('存在未确认的提交，请先重试原请求');
    this.quoteInput = null; this.setData({ quote: null });
    const productIds = this.data.rows.filter(row => row.selected && row.sellable).map(row => row.productId);
    if (!productIds.length) throw new Error('购物车中没有已选且可售的商品');
    const selectedCoupon = this.data.selectedCoupon;
    if (selectedCoupon && (this.couponsMember !== member || !this.data.coupons.some(c => c.id === selectedCoupon && c.status === 'AVAILABLE'))) throw new Error('请重新加载并选择本人优惠券');
    const request = { productIds, ...(selectedCoupon ? { couponId: selectedCoupon } : {}) };
    const quote = await this.client.quote(request);
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    if (!quote || typeof quote.settlementId !== 'string' || !/^[a-f0-9-]{36}$/.test(quote.settlementId) || ['originalAmount','discountAmount','payableAmount'].some(key => typeof quote[key] !== 'string' || !/^\d+\.\d{2}$/.test(quote[key]))) throw new Error('报价响应不合法');
    if ((quote.couponId == null ? null : couponId(quote.couponId)) !== selectedCoupon) throw new Error('报价优惠券与所选不一致');
    this.quoteMember = member;
    this.quoteInput = { settlementId: quote.settlementId };
    this.setData({ quote });
  }); },
  submit() { return this.action(async member => {
    const pending = this.flow.pending();
    if (!pending && member !== this.quoteMember) throw new Error("会员已切换，请重新预览");
    if (!pending && (!this.data.quote || /^0+\.00$/.test(this.data.quote.payableAmount))) throw new Error('请先取得有效报价；暂不支持零元下单');
    try {
      const receipt = await this.flow.submit(this.quoteInput);
      this.receiptMember = member;
      this.setData({ receipt, pending: false, quote: null }); this.quoteInput = null;
    } catch (error) { this.setData({ pending: !!this.flow.pending() }); throw error; }
  }); },
  changeCart(event) { return this.action(async member => {
    if (member !== this.rowsMember) throw new Error('会员已切换，请重新加载');
    if (this.flow.pending()) throw new Error('存在未确认结算，不能编辑购物车');
    const row = this.data.rows.find(item => item.productId === event.currentTarget.dataset.id);
    if (!row) throw new Error('商品已变更，请刷新');
    const kind = event.currentTarget.dataset.kind;
    const quantity = row.quantity + (kind === 'plus' ? 1 : kind === 'minus' ? -1 : 0);
    if (!['plus','minus','select'].includes(kind) || quantity < 1 || quantity > 999) throw new Error('数量须在1至999之间');
    this.quoteInput = null; this.setData({ quote: null });
    await this.client.saveCart(row.productId, { quantity, selected: kind === 'select' ? !row.selected : row.selected });
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    await this.readCart(member);
  }); },
  deleteCart(event) { return this.action(async member => {
    if (member !== this.rowsMember) throw new Error('会员已切换，请重新加载');
    if (this.flow.pending()) throw new Error('存在未确认结算，不能删除购物车商品');
    const id = event.currentTarget.dataset.id;
    if (!this.data.rows.some(row => row.productId === id)) throw new Error('商品不存在，请刷新');
    const result = await new Promise((resolve,reject) => wx.showModal({ title:'删除商品',content:'确认从购物车删除此商品？',success:resolve,fail:reject }));
    if (!result.confirm) return;
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    this.quoteInput = null; this.setData({ quote: null });
    if (!(await this.client.deleteCart(id))) throw new Error('商品未删除，请刷新核验');
    await this.readCart(member);
  }); },
  refreshOrder() { return this.action(async member => {
    if (!this.data.receipt || member !== this.receiptMember) throw new Error('请先查询本人订单');
    const no = this.data.receipt.orderNo;
    const result = await this.client.order(no);
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    if (!result || result.orderNo !== no || !['RESERVING','PENDING_PAYMENT','PAID','SHIPPED','COMPLETED','CLOSED'].includes(result.status) || typeof result.payableAmount !== 'string' || !/^\d+\.\d{2}$/.test(result.payableAmount)) throw new Error('订单响应不合法');
    this.setData({ receipt: result });
  }); },
  loadCoupons() { return this.action(async member => {
    if (this.flow.pending()) throw new Error('存在未确认结算，不能更换优惠券');
    this.quoteInput = null; this.couponsMember = null;
    this.setData({ coupons: [], selectedCoupon: null, quote: null });
    const rows = await this.client.coupons();
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换，请重新加载');
    const coupons = ownedCoupons(rows, member);
    this.couponsMember = member; this.setData({ coupons });
  }); },
  chooseCoupon(event) { return this.action(async member => {
    if (this.flow.pending()) throw new Error('存在未确认结算，不能更换优惠券');
    const value = event.currentTarget.dataset.id;
    const id = value === '' ? null : couponId(value);
    if (id && (this.couponsMember !== member || !this.data.coupons.some(c => c.id === id && c.status === 'AVAILABLE'))) throw new Error('优惠券不可选择，请刷新');
    this.quoteInput = null; this.setData({ selectedCoupon: id, quote: null });
  }); },
  recoverPending() { return this.action(async member => {
    const receipt = await this.flow.recover();
    this.receiptMember = member;this.quoteInput = null;
    this.setData({receipt,pending:!!this.flow.pending(),quote:null});
  }); },
  abandonPending() { return this.action(async member => {
    const confirm = await new Promise((resolve,reject) => wx.showModal({title:'终止未完成提单',content:'尚未生成订单时阻止后续提单；若订单已生成将找回原订单，不会自动关闭订单。确认继续？',success:resolve,fail:reject}));
    if (!confirm.confirm) return;
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换');
    const result = await this.flow.abandon(true);
    this.quoteInput=null;this.receiptMember=result.order?member:null;
    this.setData({pending:false,quote:null,receipt:result.order||null});
  }); },
  resetPending() { return this.action(async member => {
    const result = await new Promise((resolve,reject) => wx.showModal({title:'仅清除本机记录',content:'请先核验原订单。清除不会取消服务端订单；重新结算可能另建订单。确认已核验，仍要清除？',success:resolve,fail:reject}));
    if (!result.confirm) return;
    if (member !== getApp().globalData.memberSession?.memberId) throw new Error('会员已切换');
    this.flow.discard(true);this.quoteInput=null;this.setData({pending:!!this.flow.pending(),quote:null});
  }); },
  onHide() { this.setData({ rows: [], coupons: [], selectedCoupon: null, quote: null, receipt: null, error: '' }); this.quoteInput = null; },
});
