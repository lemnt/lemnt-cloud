const { id } = require("./storefront");
function object(value) {
  if (!value || typeof value !== "object" || Array.isArray(value))
    throw Error("文章响应不合法");
  return value;
}
function text(value) {
  if (typeof value !== "string") throw Error("文章文本不合法");
  return value;
}
function integer(value) {
  if (typeof value !== "number" || !Number.isSafeInteger(value) || value < 0)
    throw Error("文章数值不合法");
  return value;
}
function image(value) {
  return typeof value === "string" && /^https:\/\/[^\s]+$/.test(value)
    ? value
    : null;
}
function article(value) {
  const row = object(value);
  return {
    id: id(row.id),
    title: text(row.title),
    categoryId: id(row.categoryId),
    author: row.author == null ? null : text(row.author),
    picUrl: image(row.picUrl),
    introduction: row.introduction == null ? null : text(row.introduction),
    content: text(row.content),
    createTime: text(row.createTime),
    browseCount: integer(row.browseCount),
    spuId: row.spuId === 0 || row.spuId === "0" ? null : id(row.spuId),
  };
}
function rows(value, decode) {
  if (!Array.isArray(value)) throw Error("文章列表不合法");
  return value.map(decode);
}
/** 公开内容接口不附带会员Token，精确匹配网关公开路由。 */
function createArticleClient({ request, baseUrl, ready = false }) {
  async function call(path, data, method = "GET") {
    if (!ready) throw Error("商城服务暂未启用");
    if (!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))
      throw Error("请配置合法HTTPS服务地址");
    const response = await request({
      url: baseUrl.replace(/\/$/, "") + "/mall/app" + path,
      method,
      data,
      timeout: 15000,
    });
    if (response.statusCode !== 200 || response.data?.code !== 200)
      throw Error(response.data?.msg || "文章操作结果未确认");
    return object(response.data);
  }
  return {
    categories: async () =>
      rows((await call("/article-categories")).data, (v) => {
        const row = object(v);
        return {
          id: id(row.id),
          name: text(row.name),
          picUrl: image(row.picUrl),
        };
      }),
    page: async ({ categoryId, pageNum }) => {
      if (!Number.isSafeInteger(pageNum) || pageNum < 1 || pageNum > 1000000)
        throw Error("页码不合法");
      const result = await call("/articles", {
        categoryId: categoryId == null ? undefined : id(categoryId),
        pageNum,
        pageSize: 20,
      });
      return {
        records: rows(result.rows, article),
        total: integer(result.total),
      };
    },
    recommended: async ({ recommendHot, recommendBanner } = {}) => {
      for (const value of [recommendHot, recommendBanner])
        if (value != null && typeof value !== "boolean")
          throw Error("推荐筛选不合法");
      return rows(
        (await call("/articles/recommended", { recommendHot, recommendBanner }))
          .data,
        article,
      );
    },
    detail: async ({ id: key, title }) => {
      if (key != null) key = id(key);
      else if (typeof title !== "string" || !title.trim() || title.length > 255)
        throw Error("请选择文章");
      const result = await call("/articles/detail", { id: key, title });
      if (result.data == null) return null;
      const row = article(result.data);
      if (key && row.id !== key) throw Error("文章编号不一致");
      return row;
    },
    // 增量不是幂等操作，超时后不能自动重试。
    browse: async (key) => {
      const result = await call(
        "/articles/" + id(key) + "/browse-count",
        undefined,
        "PUT",
      );
      if (object(result.data).success !== true)
        throw Error("浏览次数结果未确认");
      return true;
    },
  };
}
module.exports = { createArticleClient };
