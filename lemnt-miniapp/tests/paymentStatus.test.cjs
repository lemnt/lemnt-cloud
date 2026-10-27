const {test}=require('node:test');const assert=require('node:assert/strict');const {decodePaymentStatus}=require('../services/paymentStatus');
const value={orderNo:'TR1',orderStatus:'PENDING_PAYMENT',payableAmount:'10.00',localPaymentStatus:'WAITING',channelVerified:false};
test('本地状态明确保持未核验渠道，异常状态可展示',()=>{const v=decodePaymentStatus(value,'TR1');assert.equal(v.channelVerified,false);assert.match(v.label,/本地/);assert.match(decodePaymentStatus({...value,localPaymentStatus:'RECONCILE_REQUIRED'},'TR1').label,/异常/);});
test('其他订单、未知状态、数值金额和声称渠道核验的结果均拒绝',()=>{for(const v of [{...value,orderNo:'OTHER'},{...value,localPaymentStatus:'UNPAID'},{...value,payableAmount:10},{...value,channelVerified:true}])assert.throws(()=>decodePaymentStatus(v,'TR1'));});
let definition;global.Page=v=>definition=v;require('../pages/index/index');
function page(){let member='1';global.getApp=()=>({globalData:{memberSession:{memberId:member}}});return {p:{...definition,data:{...definition.data,receipt:{orderNo:'TR1'}},receiptMember:'1',setData(v){Object.assign(this.data,v);}},switchMember:()=>member='2'};}
test('查询本地支付记录不改变订单或调用支付操作',async()=>{const {p}=page();p.client={paymentStatus:async()=>value};await p.refreshLocalPayment();assert.equal(p.data.payment.localPaymentStatus,'WAITING');assert.deepEqual(p.data.receipt,{orderNo:'TR1'});});
test('支付查询途中换号时清空旧数据',async()=>{const {p,switchMember}=page();p.client={paymentStatus:async()=>{switchMember();return value;}};await p.refreshLocalPayment();assert.equal(p.data.payment,null);assert.equal(p.data.receipt,null);});

test('已退款订单仍展示原支付成功事实并保留订单退款状态',()=>{const result=decodePaymentStatus({...value,orderStatus:'REFUNDED',localPaymentStatus:'SUCCESS'},'TR1');assert.equal(result.orderStatus,'REFUNDED');assert.equal(result.localPaymentStatus,'SUCCESS');assert.equal(result.channelVerified,false);});
