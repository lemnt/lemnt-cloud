function createCheckoutFlow({ storage, currentMember, client, newRequestNo }) {
  let busy = false;
  const copy = value => JSON.parse(JSON.stringify(value));
  function scope() {
    const member = currentMember();
    if (typeof member !== 'string' || !/^[1-9]\d{0,18}$/.test(member)) throw new Error('请先完成会员登录');
    if (storage.get('lemnt.checkout.v1.' + member)) throw new Error('存在旧版未确认结算，请先联系支持核验原订单');
    return 'lemnt.checkout.v2.' + member;
  }
  function validate(value) {
    if (!value || typeof value.settlementId !== 'string' || !/^[a-f0-9-]{36}$/.test(value.settlementId) || typeof value.requestNo !== 'string' || !value.requestNo || value.requestNo.length > 64) throw new Error('结算记录损坏，请联系支持核验订单，不能自动丢弃');
    return { settlementId: value.settlementId, requestNo: value.requestNo };
  }
  return {
    discard(acknowledged) {
      if (acknowledged !== true || busy) throw new Error('须先核验原订单，且不能在提交期间清除');
      const member = currentMember();
      if (typeof member !== 'string' || !/^[1-9]\d{0,18}$/.test(member)) throw new Error('请先完成会员登录');
      storage.remove('lemnt.checkout.v1.' + member);
      storage.remove('lemnt.checkout.v2.' + member);
    },
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
