const config = require('../config');
const { createTradeClient } = require('./trade');

function current() {
  return getApp().globalData.memberSession;
}

/** 为订单/售后页面注入 trade 客户端，身份只取会员会话令牌。 */
function attach(page) {
  page.client = createTradeClient({
    request: options => new Promise((resolve, reject) => wx.request({
      ...options,
      success: resolve,
      fail: () => reject(Error('网络未确认，请刷新核验后再操作')),
    })),
    baseUrl: config.apiBaseUrl,
    ready: config.memberApiReady,
    getMemberToken: () => current()?.accessToken,
  });
}

/** 统一加载态与错误提示，会员切换时清空页面数据。 */
async function run(page, work) {
  if (page.data.loading) return;
  page.setData({ loading: true, error: '' });
  const before = current()?.accessToken;
  try {
    await work();
  } catch (e) {
    if (before !== current()?.accessToken) page.onSessionLost();
    page.setData({ error: e.message || '操作失败，请重试' });
  } finally {
    page.setData({ loading: false });
  }
}

/** 未登录跳转登录页，返回是否已登录。 */
function requireLogin() {
  if (!current() || !current().accessToken) {
    wx.navigateTo({ url: '/pages/member-login/index' });
    return false;
  }
  return true;
}

function confirm(title, content) {
  return new Promise((resolve, reject) => {
    wx.showModal({ title, content, success: result => resolve(result.confirm), fail: reject });
  });
}

module.exports = { attach, run, current, requireLogin, confirm, ready: config.memberApiReady };
