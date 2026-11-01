const config = require('../../config');
const { createDiyClient, components } = require('../../services/diy');

function text(value) { return typeof value === 'string' ? value : ''; }

function view(item) {
  const data = item.property || {};
  if (item.id === 'Carousel') {
    const items = Array.isArray(data.items) ? data.items : [];
    return {
      key: item.id, type: 'carousel',
      items: items.filter(row => row && row.type !== 'video').map(row => ({ imgUrl: text(row.imgUrl), url: text(row.url) })),
    };
  }
  if (item.id === 'ImageBar') return { key: item.id, type: 'image', imgUrl: text(data.imgUrl), url: text(data.url) };
  if (item.id === 'TitleBar') return { key: item.id, type: 'title', title: text(data.title), description: text(data.description) };
  if (item.id === 'NoticeBar') {
    const first = Array.isArray(data.contents) ? data.contents[0] : null;
    return { key: item.id, type: 'notice', text: first ? text(first.text) : '', url: first ? text(first.url) : '' };
  }
  if (item.id === 'MenuGrid') {
    const list = Array.isArray(data.list) ? data.list : [];
    return {
      key: item.id, type: 'grid',
      items: list.map(row => ({ iconUrl: text(row.iconUrl), title: text(row.title), url: text(row.url) })),
    };
  }
  if (item.id === 'Divider') return { key: item.id, type: 'divider' };
  return { key: item.id, type: 'unsupported', name: item.id };
}

Page({
  data: { ready: config.memberApiReady, loading: false, error: '', template: null, items: [] },
  onLoad() {
    this.client = createDiyClient({
      request: options => new Promise((resolve, reject) => wx.request({ ...options, success: resolve, fail: () => reject(Error('装修读取失败，请重试')) })),
      baseUrl: config.apiBaseUrl,
      ready: config.memberApiReady,
    });
    if (this.data.ready) return this.load();
  },
  async load() {
    if (this.data.loading) return;
    this.setData({ loading: true, error: '' });
    try {
      const template = await this.client.used();
      this.setData({
        template,
        items: template ? components(template.home).map(view) : [],
      });
    } catch (e) {
      this.setData({ template: null, items: [], error: e.message || '装修页面读取失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  tap(event) {
    const url = event.currentTarget.dataset.url;
    if (!url) return;
    if (/^\/pages\//.test(url)) { wx.navigateTo({ url }); return; }
    wx.setClipboardData({ data: url, success: () => wx.showToast({ title: '链接已复制' }) });
  },
  refresh() { return this.load(); },
});
