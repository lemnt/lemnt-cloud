/** 活动详情页下单：默认地址、请求编号复用和成功清理。 */
function createPromotionOrderHelper({type, client, memberClient}) {
  function memberId() { return getApp().globalData.memberSession?.memberId; }
  function key(member) { return 'promotion.order.' + type + '.' + member; }
  return {
    async loadAddress() {
      if (!memberId()) return null;
      const list = await memberClient.addresses();
      const address = list.find((item) => item.defaultStatus) || list[0];
      return address ? { id: address.id, label: address.name + ' ' + address.mobile + ' ' + address.detailAddress } : null;
    },
    async submit(payload) {
      const member = memberId();
      if (!member) throw Error('请先完成会员登录');
      if (!payload.addressId) throw Error('请先在会员中心添加收货地址');
      const storeKey = key(member);
      let requestNo = wx.getStorageSync(storeKey);
      if (!requestNo) {
        requestNo = 'MO-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2);
        wx.setStorageSync(storeKey, requestNo);
      }
      const order = await client.order({ ...payload, requestNo });
      wx.removeStorageSync(storeKey);
      return order;
    },
  };
}
module.exports = { createPromotionOrderHelper };
