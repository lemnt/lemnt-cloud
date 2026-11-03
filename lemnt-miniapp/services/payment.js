const CHANNELS = Object.freeze({
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  SANDBOX: '沙箱',
});
const MONEY = /^\d+\.\d{2}$/;

function decodeChannels(value) {
  if (!Array.isArray(value)) throw new Error('支付渠道响应不合法');
  const seen = new Set();
  return value.map((item) => {
    if (!item || !Object.prototype.hasOwnProperty.call(CHANNELS, item.code))
      throw new Error('支付渠道响应不合法');
    if (seen.has(item.code)) throw new Error('支付渠道重复');
    seen.add(item.code);
    return {
      code: item.code,
      name: typeof item.name === 'string' && item.name.trim() ? item.name : CHANNELS[item.code],
    };
  });
}

function decodePrepay(value, orderNo) {
  if (
    !value ||
    typeof value.paymentNo !== 'string' ||
    !/^PAY[A-Z0-9]{4,37}$/.test(value.paymentNo) ||
    value.orderNo !== orderNo ||
    !Object.prototype.hasOwnProperty.call(CHANNELS, value.channel) ||
    typeof value.amount !== 'string' ||
    !MONEY.test(value.amount) ||
    !(
      value.payUrl === null ||
      value.payUrl === undefined ||
      (typeof value.payUrl === 'string' && /^https?:\/\//.test(value.payUrl))
    ) ||
    !value.params ||
    typeof value.params !== 'object' ||
    Array.isArray(value.params) ||
    Object.values(value.params).some((item) => typeof item !== 'string')
  ) {
    throw new Error('支付单响应不合法');
  }
  return {
    paymentNo: value.paymentNo,
    orderNo: value.orderNo,
    channel: value.channel,
    channelLabel: CHANNELS[value.channel],
    amount: value.amount,
    payUrl: value.payUrl || '',
    params: { ...value.params },
  };
}

module.exports = { CHANNELS, decodeChannels, decodePrepay };
