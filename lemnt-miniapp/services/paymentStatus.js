const labels=Object.freeze({NOT_CREATED:'尚无本地支付单',WAITING:'本地等待支付结果',SUCCESS:'本地已确认支付',CLOSED:'本地支付单已关闭',RECONCILE_REQUIRED:'本地记录异常，请联系支持核验'});
function decodePaymentStatus(value,orderNo) {
 if(!value || value.orderNo!==orderNo || !['RESERVING','PENDING_PAYMENT','PAID','SHIPPED','COMPLETED','CLOSED'].includes(value.orderStatus) || typeof value.payableAmount!=='string' || !/^\d+\.\d{2}$/.test(value.payableAmount) || !Object.prototype.hasOwnProperty.call(labels,value.localPaymentStatus) || value.channelVerified!==false) throw new Error('本地支付查询响应不合法');
 return {orderNo:value.orderNo,orderStatus:value.orderStatus,payableAmount:value.payableAmount,localPaymentStatus:value.localPaymentStatus,channelVerified:false,label:labels[value.localPaymentStatus]};
}
module.exports={decodePaymentStatus};
