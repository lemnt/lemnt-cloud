function object(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('分销响应格式不合法');
  return value;
}
function id(value, field = '编号') {
  const text = typeof value === 'number' && Number.isSafeInteger(value) ? String(value) : value;
  if (typeof text !== 'string' || !/^[1-9]\d{0,18}$/.test(text)) throw new Error(`${field}不合法`);
  return text;
}
function text(value, field) {
  if (typeof value !== 'string') throw new Error(`${field}不合法`);
  return value;
}
function money(value) {
  const textValue = typeof value === 'number' ? value.toFixed(2) : value;
  if (typeof textValue !== 'string' || !/^\d{1,17}\.\d{2}$/.test(textValue))
    throw new Error('佣金金额不合法');
  return textValue;
}
function user(value) {
  const row = object(value);
  return {
    id: id(row.id, '会员编号'),
    bindUserId: row.bindUserId == null ? null : id(row.bindUserId, '推广员编号'),
    bindNickname: row.bindNickname == null ? null : text(row.bindNickname, '推广员昵称'),
    brokerageEnabled: row.brokerageEnabled === true,
    brokeragePrice: money(row.brokeragePrice),
    frozenPrice: money(row.frozenPrice),
    firstLevelCount: Number.isSafeInteger(row.firstLevelCount) ? row.firstLevelCount : 0,
    secondLevelCount: Number.isSafeInteger(row.secondLevelCount) ? row.secondLevelCount : 0,
  };
}
function record(value) {
  const row = object(value);
  return {
    id: id(row.id, '记录编号'),
    bizId: text(row.bizId, '业务编号'),
    bizType: row.bizType,
    title: text(row.title, '标题'),
    price: money(row.price),
    totalPrice: money(row.totalPrice),
    status: row.status,
    frozenDays: row.frozenDays == null ? null : Number(row.frozenDays),
    unfreezeTime: row.unfreezeTime == null ? null : text(row.unfreezeTime, '解冻时间'),
    createTime: text(row.createTime, '创建时间'),
  };
}
function withdraw(value) {
  const row = object(value);
  return {
    id: id(row.id, '提现编号'),
    price: money(row.price),
    feePrice: money(row.feePrice),
    totalPrice: money(row.totalPrice),
    type: row.type,
    status: row.status,
    auditReason: row.auditReason == null ? null : text(row.auditReason, '审核说明'),
    createTime: text(row.createTime, '创建时间'),
  };
}
function createBrokerageClient({ request, baseUrl, ready = false, getMemberToken }) {
  async function call(path, method = 'GET', data) {
    if (!ready) throw new Error('会员接口尚未接通');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const token = getMemberToken();
    if (!token) throw new Error('请先完成会员登录');
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + '/trade/app/brokerage' + path,
      method,
      data,
      timeout: 15000,
      header: { Authorization: `Bearer ${token}` },
    });
    if (getMemberToken() !== token) throw new Error('会员已切换，请重新操作');
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(
        (response.data && (response.data.detail || response.data.msg)) || '分销请求失败',
      );
    }
    if (method === 'GET' && (path === '/records' || path === '/withdraws')) {
      return { records: response.data.rows || [], total: response.data.total || 0 };
    }
    return response.data.data;
  }
  return {
    user: () => call('/user').then(user),
    bind: (bindUserId) => {
      if (!/^[1-9]\d{0,18}$/.test(String(bindUserId))) throw new Error('推广员编号不合法');
      return call('/bind', 'POST', { bindUserId });
    },
    records: (input) =>
      call('/records', 'GET', { status: input && input.status, pageNum: 1, pageSize: 20 }).then(
        (page) => ({ records: page.records.map(record), total: page.total }),
      ),
    withdraws: (input) =>
      call('/withdraws', 'GET', { status: input && input.status, pageNum: 1, pageSize: 20 }).then(
        (page) => ({ records: page.records.map(withdraw), total: page.total }),
      ),
    createWithdraw: (input) => {
      if (!input || !/^\d{1,9}(\.\d{1,2})?$/.test(String(input.price)) || Number(input.price) <= 0)
        throw new Error('提现金额不合法');
      if (![1, 2, 3, 4].includes(input.type)) throw new Error('提现方式不合法');
      return call('/withdraws', 'POST', {
        price: Number(input.price).toFixed(2),
        type: input.type,
        userName: input.userName || null,
        userAccount: input.userAccount || null,
        qrCodeUrl: input.qrCodeUrl || null,
        bankName: input.bankName || null,
        bankAddress: input.bankAddress || null,
      });
    },
  };
}
module.exports = { createBrokerageClient, user, record, withdraw };
