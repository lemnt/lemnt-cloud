function object(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('客服响应格式不合法');
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
function conversation(value) {
  if (value == null) return null;
  const row = object(value);
  return {
    id: id(row.id, '会话编号'),
    lastMessageTime: row.lastMessageTime == null ? null : text(row.lastMessageTime, '最后消息时间'),
    lastMessageContent: row.lastMessageContent == null ? null : text(row.lastMessageContent, '最后消息'),
    lastMessageContentType: row.lastMessageContentType == null ? null : Number(row.lastMessageContentType),
    unreadCount: Number.isSafeInteger(row.unreadCount) && row.unreadCount >= 0 ? row.unreadCount : 0,
  };
}
function message(value) {
  const row = object(value);
  if (row.senderType !== 1 && row.senderType !== 2) throw new Error('客服发送人类型不合法');
  if (row.contentType !== 1 && row.contentType !== 2) throw new Error('客服消息类型不合法');
  return {
    id: id(row.id, '消息编号'),
    senderType: row.senderType,
    contentType: row.contentType,
    content: text(row.content, '消息内容'),
    readStatus: row.readStatus === true,
    createTime: text(row.createTime, '消息时间'),
  };
}
function createKefuClient({ request, baseUrl, ready = false, getMemberToken }) {
  async function call(path, method = 'GET', data) {
    if (!ready) throw new Error('会员接口尚未接通');
    if (!/^https:\/\/[^/]+/.test(baseUrl)) throw new Error('请配置合法 HTTPS 服务地址');
    const token = getMemberToken();
    if (!token) throw new Error('请先完成会员登录');
    const response = await request({
      url: baseUrl.replace(/\/$/, '') + '/mall/app/kefu' + path,
      method, data, timeout: 15000, header: { Authorization: `Bearer ${token}` },
    });
    if (getMemberToken() !== token) throw new Error('会员已切换，请重新操作');
    if (response.statusCode !== 200 || !response.data || response.data.code !== 200) {
      throw new Error(response.data && (response.data.detail || response.data.msg) || '客服请求失败');
    }
    return response.data.data;
  }
  return {
    conversation: () => call('/conversation').then(conversation),
    messages: limit => {
      if (!Number.isInteger(limit) || limit < 1 || limit > 200) throw new Error('消息条数不合法');
      return call('/messages', 'GET', { limit }).then(value => {
        if (!Array.isArray(value)) throw new Error('客服消息列表不合法');
        return value.map(message);
      });
    },
    send: (content, contentType = 1) => {
      if (![1, 2].includes(contentType)) throw new Error('消息类型不合法');
      if (typeof content !== 'string' || !content.trim() || content.length > 2000) throw new Error('消息不能为空且不超过2000字');
      return call('/messages', 'POST', { contentType, content: content.trim() }).then(value => id(value, '消息编号'));
    },
    read: () => call('/read', 'PUT'),
  };
}
module.exports = { createKefuClient, conversation, message };
