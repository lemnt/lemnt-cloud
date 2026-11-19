const { id } = require('./storefront');

/** 文章读取全走公开商城入口，保留长ID和服务端页面边界。 */
function createArticleClient({ request, baseUrl, ready }) {
  async function call(path, data, method = 'GET') {
    if (!ready || !/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))
      throw Error('商城服务尚未启用');
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + '/mall/app/' + path,
      data,
      method,
      timeout: 15000,
    });
    if (response.statusCode !== 200 || response.data?.code !== 200)
      throw Error(response.data?.msg || '文章读取失败');
    return response.data;
  }
  function decode(row, detail = false) {
    if (!row || typeof row.title !== 'string' || (detail && typeof row.content !== 'string'))
      throw Error('文章数据不合法');
    return { ...row, id: id(row.id), spuId: row.spuId == null ? null : id(row.spuId) };
  }
  return {
    async categories() {
      const { data } = await call('article-categories');
      if (!Array.isArray(data)) throw Error('文章分类响应不合法');
      return data.map((row) => {
        if (typeof row.name !== 'string') throw Error('文章分类名称不合法');
        return { id: id(row.id), name: row.name };
      });
    },
    async page(pageNum, { categoryId, recommendHot, recommendBanner } = {}) {
      const result = await call('articles', {
        pageNum,
        pageSize: 20,
        categoryId,
        recommendHot,
        recommendBanner,
      });
      if (!Array.isArray(result.rows) || !Number.isSafeInteger(result.total) || result.total < 0)
        throw Error('文章分页响应不合法');
      return { records: result.rows.map((row) => decode(row)), total: result.total };
    },
    async detail(key) {
      const data = key.id ? { id: id(key.id) } : { title: key.title };
      const result = await call('articles/detail', data);
      const row = decode(result.data, true);
      if (data.id && row.id !== data.id) throw Error('文章编号不一致');
      return row;
    },
    async browse(articleId) {
      return (await call('articles/' + id(articleId) + '/browse', undefined, 'PUT')).data === true;
    },
  };
}
module.exports = { createArticleClient };
