function object(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('装修响应格式不合法');
  return value;
}
function id(value, field = '编号') {
  const text = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof text !== 'string' || !/^[1-9]\d{0,18}$/.test(text)) throw new Error(`${field}不合法`);
  return text;
}
function json(value, field) {
  if (typeof value !== 'string' || !value.trim()) throw new Error(`${field}不合法`);
  try {
    const parsed = JSON.parse(value);
    if (!parsed || typeof parsed !== 'object') throw new Error('not-object');
    return parsed;
  } catch (error) {
    throw new Error(`${field}不是合法JSON`);
  }
}
function components(property) {
  const value = property && Array.isArray(property.components) ? property.components : [];
  return value.map(item => {
    const row = object(item);
    if (typeof row.id !== 'string' || !row.id) throw new Error('装修组件类型不合法');
    return { id: row.id, property: object(row.property || {}) };
  });
}
function template(value) {
  if (value == null) return null;
  const row = object(value);
  const property = json(row.property, '模板属性');
  return {
    id: id(row.id, '模板编号'),
    name: typeof row.name === 'string' ? row.name : '',
    property,
    home: row.home == null ? {} : json(row.home, '首页属性'),
    user: row.user == null ? {} : json(row.user, '我的页面属性'),
  };
}
function page(value) {
  const row = object(value);
  return { id: id(row.id, '页面编号'), name: typeof row.name === 'string' ? row.name : '', property: json(row.property, '页面属性') };
}
function createDiyClient({ request, baseUrl, ready = false }) {
  async function call(path) {
    if (!ready) throw new Error('商城服务暂未启用');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const response = await request({ url: baseUrl.replace(/\/$/, '') + '/mall/app/diy' + path, method: 'GET', timeout: 15000 });
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(response.data && (response.data.detail || response.data.msg) || '装修读取失败');
    }
    return response.data.data;
  }
  return {
    used: () => call('/template/used').then(template),
    template: key => call('/template/' + id(key)).then(template),
    page: key => call('/page/' + id(key)).then(page),
  };
}
module.exports = { createDiyClient, template, page, components };
