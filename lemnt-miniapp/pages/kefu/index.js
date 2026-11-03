const config = require('../../config');
const runtime = require('../../services/orderRuntime');
const { createKefuClient } = require('../../services/kefu');

Page({
  data: {
    ready: config.memberApiReady,
    loading: false,
    error: '',
    loggedIn: false,
    conversation: null,
    messages: [],
    content: '',
    imageMode: false,
  },
  onLoad() {
    runtime.attach(this);
    this.kefu = createKefuClient({
      request: (options) =>
        new Promise((resolve, reject) =>
          wx.request({
            ...options,
            success: resolve,
            fail: () => reject(Error('网络未确认，请刷新核验')),
          }),
        ),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
      getMemberToken: () => getApp().globalData.memberSession?.accessToken,
    });
  },
  onShow() {
    const loggedIn = !!runtime.current();
    this.setData({ loggedIn });
    if (!loggedIn) return;
    if (runtime.requireLogin()) this.reload();
    this.timer = setInterval(() => this.silentRefresh(), 5000);
  },
  onHide() {
    clearInterval(this.timer);
  },
  onUnload() {
    clearInterval(this.timer);
  },
  onSessionLost() {
    this.setData({
      conversation: null,
      messages: [],
      loggedIn: false,
      error: '会员已切换，请重新登录',
    });
  },
  login() {
    wx.navigateTo({ url: '/pages/member-login/index' });
  },
  reload() {
    return runtime.run(this, async () => {
      await this.load();
      await this.kefu.read();
    });
  },
  async silentRefresh() {
    if (this.data.loading) return;
    try {
      await this.load();
    } catch (e) {
      /* 轮询失败不覆盖现有消息 */
    }
  },
  async load() {
    const conversation = await this.kefu.conversation();
    const messages = conversation ? await this.kefu.messages(50) : [];
    this.setData({ conversation, messages, error: '' });
  },
  input(event) {
    this.setData({ content: event.detail.value });
  },
  toggleImage(event) {
    this.setData({ imageMode: event.detail.value });
  },
  send() {
    return runtime.run(this, async () => {
      const content = this.data.content.trim();
      if (!content) throw new Error('请输入消息内容');
      await this.kefu.send(content, this.data.imageMode ? 2 : 1);
      this.setData({ content: '' });
      await this.load();
    });
  },
  refresh() {
    return this.reload();
  },
  goImage(event) {
    const url = event.currentTarget.dataset.url;
    if (/^\/pages\//.test(url)) {
      wx.navigateTo({ url });
      return;
    }
    wx.setClipboardData({ data: url, success: () => wx.showToast({ title: '链接已复制' }) });
  },
});
