function obj(v){if(!v||typeof v!=='object'||Array.isArray(v))throw Error('活动订单响应格式不合法');return v;}
function text(v){if(typeof v!=='string')throw Error('活动订单文本格式不合法');return v;}
function integer(v){if(typeof v!=='number'||!Number.isSafeInteger(v)||v<0)throw Error('活动订单数值格式不合法');return v;}
function money(v){if(typeof v!=='string'||!/^\d{1,17}\.\d{2}$/.test(v))throw Error('活动订单金额格式不合法');return v;}
function id(v){const value=typeof v==='number'&&Number.isSafeInteger(v)?String(v):v;if(typeof value!=='string'||!/^[1-9]\d{0,18}$/.test(value)||BigInt(value)>9223372036854775807n)throw Error('活动订单编号不合法');return value;}
function decodeOrder(v){obj(v);if(typeof v.status!=='string')throw Error('活动订单状态不合法');return {orderNo:text(v.orderNo),totalAmount:money(v.totalAmount),status:text(v.status),usePoint:integer(v.usePoint),givePoint:integer(v.givePoint)};}
function requestNo(v){if(typeof v!=='string'||!/^[A-Za-z0-9_-]{1,64}$/.test(v))throw Error('请求编号不合法');return v;}
function addressId(v){return v==null?undefined:id(v);}
function quantity(v){if(!Number.isSafeInteger(v)||v<1||v>999)throw Error('购买数量不合法');return v;}
module.exports={decodeOrder,requestNo,addressId,quantity,id};
