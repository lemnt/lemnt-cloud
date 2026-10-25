function createCheckoutFlow({ storage, currentMember, client, newRequestNo }) {
  let busy = false;
  const copy = value => JSON.parse(JSON.stringify(value));
  function scope() {
    const member = currentMember();
    if (typeof member !== 'string' || !/^[1-9]\d{0,18}$/.test(member)) throw new Error('请先完成会员登录');
    return 'lemnt.checkout.v1.' + member;
  }
  function validate(value) {
    if (!value || !Array.isArray(value.productIds) || !value.productIds.length ||
      value.productIds.some(id => typeof id !== 'string' || !/^[1-9]\d{0,18}$/.test(id)) ||
      typeof value.requestNo !== 'string' || !value.requestNo || value.requestNo.length > 64 ||
      typeof value.quoteToken !== 'string' || !/^[a-f0-9]{64}$/.test(value.quoteToken) ||
      (value.couponId !== undefined && !/^[1-9]\d{0,18}$/.test(value.couponId))) throw new Error('结算记录损坏，请联系支持核验订单，不能自动丢弃');
    return { productIds: value.productIds.slice(), requestNo: value.requestNo, quoteToken: value.quoteToken,
      ...(value.couponId === undefined ? {} : { couponId: value.couponId }) };
  }
  return {
    pending() { const saved = storage.get(scope()); return saved ? copy(validate(saved)) : null; },
    async submit(input) {
      if (busy) throw new Error('正在提交，请勿重复点击');
      busy = true;
      try {
        const key = scope();
        const saved = storage.get(key);
        const intent = saved ? validate(saved) : validate({ ...input, requestNo: newRequestNo() });
        // Storage errors abort BEFORE sending; unknown outcomes retain the exact immutable intent.
        if (!saved) storage.set(key, copy(intent));
        const result = await client.checkout(copy(intent));
        if (scope() !== key) throw new Error('会员已切换，原会员的结算记录已保留');
        if (!result || typeof result.orderNo !== 'string' || !result.orderNo ||
          !['RESERVING','PENDING_PAYMENT','PAID','SHIPPED','COMPLETED','CLOSED'].includes(result.status) ||
          typeof result.payableAmount !== 'string' || !/^\d+\.\d{2}$/.test(result.payableAmount)) throw new Error('订单响应无法确认，已保留请求，请用原请求重试');
        storage.remove(key);
        return result;
      } finally { busy = false; }
    },
  };
}
module.exports = { createCheckoutFlow };
