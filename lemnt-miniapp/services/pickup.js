function object(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('自提门店响应不合法');
  return value;
}
function id(value) {
  const text = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof text !== 'string' || !/^[1-9]\d{0,18}$/.test(text)) throw new Error('门店编号不合法');
  return text;
}
function text(value, field) {
  if (typeof value !== 'string') throw new Error(`${field}不合法`);
  return value;
}
function optional(value, field) {
  return value == null ? null : text(value, field);
}
function store(value) {
  const row = object(value);
  return {
    id: id(row.id), name: text(row.name, '门店名称'),
    introduction: optional(row.introduction, '简介'), phone: text(row.phone, '电话'),
    areaId: id(row.areaId), detailAddress: text(row.detailAddress, '地址'),
    logo: optional(row.logo, 'logo'), openingTime: text(row.openingTime, '开门时间'),
    closingTime: text(row.closingTime, '关门时间'),
    latitude: optional(row.latitude, '纬度'), longitude: optional(row.longitude, '经度'),
  };
}
function createPickUpClient({ request, baseUrl, ready = false, getMemberToken = () => null }) {
  async function call(path) {
    if (!ready) throw new Error('会员接口尚未接通');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const token = getMemberToken();
    if (!token) throw new Error('请先完成会员登录');
    const response = await request({ url: baseUrl.replace(/\/$/, '') + '/trade/app' + path, method: 'GET', timeout: 15000, header: { Authorization: `Bearer ${token}` } });
    if (getMemberToken() !== token) throw new Error('会员已切换，请重新操作');
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(response.data && (response.data.detail || response.data.msg) || '自提门店读取失败');
    }
    return response.data.data;
  }
  return {
    stores: () => call('/delivery/pick-up-stores').then(value => {
      if (!Array.isArray(value)) throw new Error('自提门店列表不合法');
      return value.map(store);
    }),
  };
}
module.exports = { createPickUpClient, store };
