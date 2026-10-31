const {
  createImageClient,
  selectImages,
  pictureUrls,
  removePicture,
} = require("../../services/images");
const config = require("../../config");
const { createCommentClient } = require("../../services/comments");
const { createTradeClient } = require("../../services/trade");
const { id } = require("../../services/storefront");
/** 整单评价逐行提交，保留部分成功和未知结果的原始请求。 */
Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    eligible: false,
    rows: [],
    error: "",
    success: "",
  },
  onLoad(options) {
    if (
      typeof options.orderNo !== "string" ||
      !/^[A-Za-z0-9_-]{1,40}$/.test(options.orderNo)
    ) {
      this.setData({ error: "订单编号不合法" });
      return;
    }
    this.orderNo = options.orderNo;
    const settings = {
      ready: config.memberApiReady,
      baseUrl: config.apiBaseUrl,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error("网络结果未确认")),
          }),
        ),
    };
    this.ownerToken = settings.getMemberToken();
    this.client = createCommentClient(settings);
    this.trade = createTradeClient(settings);
    return this.load();
  },
  sameOwner() {
    return (
      !!this.ownerToken &&
      this.ownerToken === getApp().globalData.memberSession?.accessToken
    );
  },
  clearOwner() {
    this.setData({
      rows: [],
      eligible: false,
      success: "",
      error: "会员已切换，请从本人订单重新进入",
    });
  },
  onShow() {
    if (this.ownerToken && !this.sameOwner()) this.clearOwner();
  },
  async load() {
    if (this.data.loading || this.data.rows.length || !this.orderNo) return;
    this.setData({ loading: true, error: "" });
    try {
      if (!this.sameOwner()) throw Error("请从本人订单重新进入");
      const order = await this.trade.order(this.orderNo);
      if (!this.sameOwner()) {
        this.clearOwner();
        return;
      }
      if (
        order?.orderNo !== this.orderNo ||
        order.status !== "COMPLETED" ||
        !Array.isArray(order.items) ||
        !order.items.length
      )
        throw Error("只能评价本人已完成订单商品");
      const seen = new Set();
      const rows = order.items.map((line) => {
        const orderItemId = id(line.orderItemId);
        if (seen.has(orderItemId)) throw Error("订单项编号重复");
        seen.add(orderItemId);
        return {
          orderItemId,
          skuId: id(line.productId),
          productName: line.productName || "",
          commentStatus: line.commentStatus,
          descriptionScores: "5",
          benefitScores: "5",
          content: "",
          pictures: "",
          imageUrls: [],
          anonymous: false,
          submitted: line.commentStatus === true,
          pending: null,
        };
      });
      this.setData({ rows, eligible: true });
    } catch (error) {
      this.setData({ eligible: false, error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  input(event) {
    if (this.data.loading || !this.sameOwner()) return;
    const { index, field } = event.currentTarget.dataset;
    const rows = this.data.rows;
    const row = rows[Number(index)];
    if (!row || row.submitted || row.pending || row.commentStatus !== false)
      return;
    if (
      ![
        "content",
        "pictures",
        "descriptionScores",
        "benefitScores",
        "anonymous",
      ].includes(field)
    )
      return;
    row[field] = event.detail.value;
    if (field === "pictures") row.imageUrls = pictureUrls(row.pictures);
    this.setData({ rows });
  },
  /** 成功项跳过；未知项只重发原内容，不自动回滚其他已成功项。 */
  previewImages(event) {
    if (!this.sameOwner()) return;
    const row = this.data.rows[Number(event.currentTarget.dataset.index)];
    if (!row) return;
    const urls = pictureUrls(row.pictures),
      current = event.currentTarget.dataset.url;
    if (urls.includes(current)) wx.previewImage({ urls, current });
  },
  removeImage(event) {
    if (this.data.loading || !this.sameOwner()) return;
    const row = this.data.rows[Number(event.currentTarget.dataset.index)];
    if (!row || row.pending || row.submitted || row.commentStatus !== false)
      return;
    row.pictures = removePicture(row.pictures, event.currentTarget.dataset.url);
    row.imageUrls = pictureUrls(row.pictures);
    this.setData({ rows: this.data.rows });
  },
  async uploadImages(event) {
    if (this.data.loading || !this.sameOwner()) return;
    const row = this.data.rows[Number(event.currentTarget.dataset.index)];
    if (!row || row.submitted || row.pending || row.commentStatus !== false)
      return;
    const pictures = row.pictures
      .split(/\r?\n/)
      .map((v) => v.trim())
      .filter(Boolean);
    if (pictures.length >= 9) return;
    this.setData({ loading: true, error: "" });
    try {
      const client = createImageClient({
        baseUrl: config.apiBaseUrl,
        getMemberToken: () =>
          this.ownerToken === getApp().globalData.memberSession?.accessToken
            ? this.ownerToken
            : null,
        uploadFile: (options) =>
          new Promise((resolve, reject) =>
            wx.uploadFile({
              ...options,
              success: resolve,
              fail: () => reject(Error("上传结果未确认，请核对后重选")),
            }),
          ),
      });
      await selectImages({
        count: 9 - pictures.length,
        client,
        append: (url) => {
          if (!this.sameOwner()) throw Error("会员已切换");
          pictures.push(url);
          row.pictures = pictures.join("\n");
          row.imageUrls = [...pictures];
          this.setData({ rows: this.data.rows });
        },
      });
    } catch (error) {
      if (!this.sameOwner()) this.clearOwner();
      else this.setData({ error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  async submit() {
    if (this.data.loading || !this.data.eligible) return;
    if (!this.sameOwner()) {
      this.clearOwner();
      return;
    }
    this.setData({ loading: true, error: "", success: "" });
    try {
      for (const row of this.data.rows) {
        if (row.submitted) continue;
        if (!this.sameOwner()) {
          this.clearOwner();
          return;
        }
        if (row.commentStatus !== false)
          throw Error("订单项评价状态未知，请从订单刷新后重新进入");
        const payload = row.pending || {
          orderNo: this.orderNo,
          orderItemId: row.orderItemId,
          skuId: row.skuId,
          descriptionScores: Number(row.descriptionScores),
          benefitScores: Number(row.benefitScores),
          content: row.content,
          anonymous: row.anonymous,
          picUrls: row.pictures
            .split(/\r?\n/)
            .map((v) => v.trim())
            .filter(Boolean),
        };
        try {
          const result = await this.client.create(payload);
          if (!this.sameOwner()) {
            this.clearOwner();
            return;
          }
          row.submitted = true;
          row.pending = null;
          row.commentId = result.id;
          this.setData({ rows: this.data.rows });
        } catch (error) {
          if (!this.sameOwner()) {
            this.clearOwner();
            return;
          }
          if (error.unconfirmed) row.pending = payload;
          this.setData({ rows: this.data.rows });
          throw Error(
            (error.unconfirmed ? "结果未确认，重试会核验原评价：" : "") +
              error.message,
          );
        }
      }
      this.setData({ success: "本单商品评价均已确认" });
    } catch (error) {
      this.setData({ error: error.message });
    } finally {
      this.setData({ loading: false });
    }
  },
});
