const { couponId: id } = require("./coupons");
function object(value) {
  if (!value || typeof value !== "object" || Array.isArray(value))
    throw Error("商品记录格式不合法");
  return value;
}
function success(value) {
  if (object(value).success !== true) throw Error("写入结果未确认，请刷新核验");
  return true;
}
function integer(value) {
  if (!Number.isSafeInteger(value) || value < 0) throw Error("分页数量不合法");
  return value;
}
function row(value, history) {
  const r = object(value);
  if (r.spuName != null && typeof r.spuName !== "string")
    throw Error("商品名称不合法");
  if (
    r.price != null &&
    (typeof r.price !== "string" || !/^\d+\.\d{2}$/.test(r.price))
  )
    throw Error("商品价格不合法");
  if (history && typeof r.createTime !== "string")
    throw Error("足迹时间不合法");
  return {
    id: id(r.id),
    spuId: id(r.spuId),
    spuName: r.spuName ?? null,
    picUrl:
      typeof r.picUrl === "string" && /^https:\/\//.test(r.picUrl)
        ? r.picUrl
        : null,
    price: r.price ?? null,
    ...(history ? { createTime: r.createTime } : {}),
  };
}
function createProductInteractionClient({
  request,
  baseUrl,
  ready = false,
  getMemberToken,
}) {
  async function call(path, method = "GET", data, paged = false) {
    if (!ready) throw Error("商城服务尚未启用");
    if (!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))
      throw Error("服务地址不合法");
    const token = getMemberToken();
    if (!token) throw Error("请先登录");
    const result = await request({
      url: baseUrl.replace(/\/$/, "") + "/mall/app" + path,
      method,
      data,
      timeout: 15000,
      header: { Authorization: `Bearer ${token}` },
    });
    if (getMemberToken() !== token) throw Error("会员已切换，请重新加载");
    if (result.statusCode !== 200 || result.data?.code !== 200)
      throw Error(
        result.data?.detail || result.data?.msg || "结果未确认，请刷新核验",
      );
    return paged
      ? { records: result.data.rows, total: result.data.total }
      : result.data.data;
  }
  async function page(path, pageNum, history) {
    if (!Number.isSafeInteger(pageNum) || pageNum < 1 || pageNum > 1000000)
      throw Error("页码不合法");
    const value = await call(path, "GET", { pageNum, pageSize: 20 }, true);
    if (!Array.isArray(value.records)) throw Error("缺少分页记录");
    return {
      records: value.records.map((v) => row(v, history)),
      total: integer(value.total),
    };
  }
  return {
    favorites: (pageNum) => page("/favorites", pageNum, false),
    history: (pageNum) => page("/browse-history", pageNum, true),
    favorite: async (spuId) =>
      id(object(await call("/favorites", "POST", { spuId: id(spuId) })).id),
    cancelFavorite: async (spuId) =>
      success(await call("/favorites/" + id(spuId), "DELETE")),
    isFavorite: async (spuId) => {
      const value = object(
        await call("/favorites/exists", "GET", { spuId: id(spuId) }),
      ).favorite;
      if (typeof value !== "boolean") throw Error("收藏状态未确认");
      return value;
    },
    favoriteCount: async () => {
      const value = object(await call("/favorites/count")).count;
      if (typeof value === "string" && /^\d+$/.test(value)) return value;
      return String(integer(value));
    },
    recordBrowse: async (spuId) =>
      success(await call("/browse-history", "POST", { spuId: id(spuId) })),
    deleteHistory: async (spuIds) => {
      if (!Array.isArray(spuIds) || !spuIds.length || spuIds.length > 100)
        throw Error("请选择1至100个商品");
      return success(
        await call("/browse-history", "DELETE", { spuIds: spuIds.map(id) }),
      );
    },
    clearHistory: async () =>
      success(await call("/browse-history/clear", "DELETE")),
  };
}
module.exports = { createProductInteractionClient };
