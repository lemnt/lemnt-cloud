const runtime = require('../../services/memberRuntime');
Page({
  data: { ready: runtime.ready, loading: false, error: '', success: '', mobile: '', password: '' },
  onLoad() { runtime.attach(this); },
  onUnload() { this.setData({ password: '' }); },
  input(e) {
    const key = e.currentTarget.dataset.field;
    if (['mobile', 'password'].includes(key)) this.setData({ [key]: runtime.input(e) });
  },
  submit() {
    return runtime.run(this, async () => {
      await this.client.passwordLogin(this.data.mobile, this.data.password);
      this.setData({ password: '', success: '登录成功' });
      wx.redirectTo({ url: '/pages/member/index' });
    });
  },
});
