const config = require("../../config");
const { createCommentClient } = require("../../services/comments");
const { createTradeClient } = require("../../services/trade");
const { id } = require("../../services/storefront");
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    eligible: false,
    error: "",
    success: "",
    pending: false,
    submitted: false,
    spuId: null,
    productName: "",
    descriptionScores: "5",
    benefitScores: "5",
    content: "",
    pictures: "",
    anonymous: false,
  },
  onLoad(options) {
    try {
      this.skuId = id(options.skuId);
      this.requestedOrderItemId =
        options.orderItemId == null ? null : id(options.orderItemId);
      if (
        typeof options.orderNo !== "string" ||
        !options.orderNo.trim() ||
        options.orderNo.length > 40
      )
        throw Error("订单编号不合法");
      this.orderNo = options.orderNo;
    } catch (error) {
      this.setData({ error: error.message });
      return;
    }
    const transport = (options) =>
      new Promise((resolve, reject) =>
        wx.request({
          ...options,
          success: resolve,
          fail: () => reject(Error("网络结果未确认")),
        }),
      );
    const settings = {
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: transport,
    };
    this.client = createCommentClient(settings);
    this.trade = createTradeClient(settings);
    this.ownerToken = settings.getMemberToken();
    return this.load();
  },
  onShow() {
    if (
      this.ownerToken &&
      this.ownerToken !== getApp().globalData.memberSession?.accessToken
    ) {
      this.pending = null;
      this.setData({
        eligible: false,
        content: "",
        pictures: "",
        error: "会员已切换，请从本人订单重新进入",
        pending: false,
        submitted: false,
        success: "",
      });
    }
  },
  async load() {
    if (this.data.loading || !this.orderNo) return;
    this.setData({ loading: true, error: "", eligible: false });
    try {
      if (this.ownerToken !== getApp().globalData.memberSession?.accessToken)
        throw Error("请从本人订单重新进入");
      const order = await this.trade.order(this.orderNo);
      if (
        order?.orderNo !== this.orderNo ||
        order.status !== "COMPLETED" ||
        !Array.isArray(order.items)
      )
        throw Error("只能评价本人已完成订单商品");
      const line = order.items.find(
        (item) =>
          id(item.productId) === this.skuId &&
          (!this.requestedOrderItemId ||
            id(item.orderItemId) === this.requestedOrderItemId),
      );
      if (!line) throw Error("商品不在当前订单中");
      this.orderItemId = id(line.orderItemId);
      if (line.commentStatus === true && !this.pending)
        throw Error("该订单项已评价");
      this.setData({
        eligible: true,
        productName: line.productName || "",
        spuId: line.spuId == null ? null : id(line.spuId),
      });
    } catch (error) {
      this.setData({ eligible: false, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  input(event) {
    if (this.data.loading || this.data.pending || this.data.submitted) return;
    const name = event.currentTarget.dataset.field;
    if (
      ["content", "pictures", "descriptionScores", "benefitScores"].includes(
        name,
      )
    )
      this.setData({ [name]: event.detail.value });
  },
  anonymous(event) {
    if (!this.data.loading && !this.data.pending && !this.data.submitted)
      this.setData({ anonymous: event.detail.value });
  },
  async submit() {
    if (this.data.loading || !this.data.eligible || this.data.submitted) return;
    this.setData({ loading: true, error: "", success: "" });
    const payload = this.pending || {
      orderNo: this.orderNo,
      skuId: this.skuId,
      orderItemId: this.orderItemId,
      descriptionScores: Number(this.data.descriptionScores),
      benefitScores: Number(this.data.benefitScores),
      content: this.data.content,
      anonymous: this.data.anonymous,
      picUrls: this.data.pictures
        .split(/\r?\n/)
        .map((v) => v.trim())
        .filter(Boolean),
    };
    try {
      if (this.ownerToken !== getApp().globalData.memberSession?.accessToken)
        throw Error("会员已切换，请从本人订单重新进入");
      if (!this.orderItemId) throw Error("请先核验真实订单项信息");
      const result = await this.client.create(payload);
      this.pending = null;
      this.setData({
        pending: false,
        submitted: true,
        success: "评价已保存，编号 " + result.id,
      });
    } catch (error) {
      if (error.unconfirmed) {
        this.pending = payload;
        this.setData({
          pending: true,
          error: "结果未确认，请重试原评价核验：" + error.message,
        });
      } else {
        this.setData({ error: error.message });
      }
    } finally {
      this.setData({ loading: false });
    }
  },
  comments() {
    if (this.data.spuId)
      wx.navigateTo({
        url:
          "/pages/comments/index?spuId=" + encodeURIComponent(this.data.spuId),
      });
  },
});
