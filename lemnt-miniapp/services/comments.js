const { id } = require("./storefront");
function object(value) {
  if (!value || typeof value !== "object" || Array.isArray(value))
    throw Error("评价响应不合法");
  return value;
}
function text(value) {
  if (typeof value !== "string") throw Error("评价文本不合法");
  return value;
}
function score(value) {
  if (!Number.isInteger(value) || value < 1 || value > 5)
    throw Error("评分须为1至5分");
  return value;
}
function image(value) {
  return typeof value === "string" && /^https:\/\/[^\s]+$/.test(value)
    ? value
    : null;
}
function list(value, decode) {
  if (!Array.isArray(value)) throw Error("评价列表不合法");
  return value.map(decode);
}
function comment(value) {
  const row = object(value);
  if (
    typeof row.replyStatus !== "boolean" ||
    (row.anonymous != null && typeof row.anonymous !== "boolean")
  )
    throw Error("评价标记不合法");
  return {
    id: id(row.id),
    spuId: id(row.spuId),
    skuId: id(row.skuId),
    userNickname: row.userNickname == null ? null : text(row.userNickname),
    userAvatar: image(row.userAvatar),
    anonymous: row.anonymous == null ? null : row.anonymous,
    score: score(row.score),
    descriptionScores:
      row.descriptionScores == null ? null : score(row.descriptionScores),
    benefitScores: row.benefitScores == null ? null : score(row.benefitScores),
    content: text(row.content),
    picUrls:
      row.picUrls == null ? null : list(row.picUrls, image).filter(Boolean),
    spuName: row.spuName == null ? null : text(row.spuName),
    skuProperties:
      row.skuProperties == null
        ? null
        : list(row.skuProperties, (v) => {
            const p = object(v);
            return text(p.propertyName) + "：" + text(p.valueName);
          }).join(" / "),
    replyStatus: row.replyStatus,
    replyContent: row.replyContent == null ? null : text(row.replyContent),
    replyTime: row.replyTime == null ? null : text(row.replyTime),
    createTime: text(row.createTime),
  };
}
/** 只读接口公开；写入令牌来自会员上下文，不能指定会员或显示状态。 */
function createCommentClient({
  request,
  baseUrl,
  ready = false,
  getMemberToken = () => null,
}) {
  async function call(method, data) {
    if (!ready) throw Error("商城服务暂未启用");
    if (!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))
      throw Error("请配置合法HTTPS服务地址");
    const token = method === "POST" ? getMemberToken() : null;
    if (method === "POST" && !token) throw Error("请先登录");
    let response;
    try {
      response = await request({
        url: baseUrl.replace(/\/$/, "") + "/mall/app/comments",
        method,
        data,
        timeout: 15000,
        ...(token ? { header: { Authorization: "Bearer " + token } } : {}),
      });
    } catch (reason) {
      throw Object.assign(Error(reason.message || "评价结果未确认"), {
        unconfirmed: method === "POST",
      });
    }
    if (token && token !== getMemberToken())
      throw Object.assign(Error("会员已切换，不能沿用原会员响应"), {
        unconfirmed: true,
      });
    if (response.statusCode !== 200 || response.data?.code !== 200)
      throw Object.assign(
        Error(response.data?.detail || response.data?.msg || "评价请求失败"),
        {
          unconfirmed:
            method === "POST" &&
            (response.statusCode >= 500 || response.statusCode < 400),
        },
      );
    return object(response.data);
  }
  return {
    page: async ({ spuId, type = 0, pageNum = 1 }) => {
      if (
        !Number.isInteger(type) ||
        type < 0 ||
        type > 3 ||
        !Number.isSafeInteger(pageNum) ||
        pageNum < 1 ||
        pageNum > 1000000
      )
        throw Error("评价分页条件不合法");
      const result = await call("GET", {
        spuId: id(spuId),
        type,
        pageNum,
        pageSize: 20,
      });
      if (!Number.isSafeInteger(result.total) || result.total < 0)
        throw Error("评价总数不合法");
      return { records: list(result.rows, comment), total: result.total };
    },
    create: async (input) => {
      if (
        typeof input.orderNo !== "string" ||
        !input.orderNo.trim() ||
        input.orderNo.length > 40
      )
        throw Error("订单编号不合法");
      if (
        typeof input.content !== "string" ||
        !input.content.trim() ||
        input.content.length > 1000
      )
        throw Error("请输入1至1000字评价");
      if (
        typeof input.anonymous !== "boolean" ||
        !Array.isArray(input.picUrls) ||
        input.picUrls.length > 9 ||
        input.picUrls.some((url) => !image(url) || url.length > 1024)
      )
        throw Error("匿名标记或图片地址不合法");
      const payload = {
        orderNo: input.orderNo,
        skuId: id(input.skuId),
        ...(input.orderItemId == null
          ? {}
          : { orderItemId: id(input.orderItemId) }),
        descriptionScores: score(input.descriptionScores),
        benefitScores: score(input.benefitScores),
        anonymous: input.anonymous,
        picUrls: [...input.picUrls],
        content: input.content,
      };
      const result = await call("POST", payload);
      try {
        return { id: id(object(result.data).id) };
      } catch (reason) {
        throw Object.assign(reason, { unconfirmed: true });
      }
    },
  };
}
module.exports = { createCommentClient };
