const {test}=require('node:test');const assert=require('node:assert/strict');
const {createCheckoutFlow}=require('../services/checkoutFlow');
const input={productIds:['5'],quoteToken:'a'.repeat(64)};
function setup() {
 const values=new Map(),calls=[]; let member='1',fail=true;
 const storage={get:k=>values.get(k),set:(k,v)=>values.set(k,v),remove:k=>values.delete(k)};
 const flow=createCheckoutFlow({storage,currentMember:()=>member,newRequestNo:()=> 'R1',client:{checkout:async data=>{calls.push(data);if(fail)throw Error('lost response');return {orderNo:'TR1',status:'RESERVING',payableAmount:'10.00'};}}});
 return {flow,storage,values,calls,setMember:v=>member=v,succeed:()=>fail=false};
}
test('未知结果保留原载荷，重试不能替换成新报价',async()=>{
 const s=setup();await assert.rejects(s.flow.submit(input));assert.equal(s.flow.pending().requestNo,'R1');
 s.succeed();await s.flow.submit({...input,quoteToken:'b'.repeat(64),productIds:['6']});assert.deepEqual(s.calls[0],s.calls[1]);assert.equal(s.flow.pending(),null);
});
test('存储失败禁止发请求，损坏记录禁止自动覆盖',async()=>{
 let called=false;const flow=createCheckoutFlow({storage:{get:()=>null,set:()=>{throw Error('full');}},currentMember:()=> '1',newRequestNo:()=> 'R1',client:{checkout:async()=>called=true}});
 await assert.rejects(flow.submit(input));assert.equal(called,false);
 const s=setup();s.values.set('lemnt.checkout.v1.1',{bad:true});await assert.rejects(s.flow.submit(input));assert.equal(s.calls.length,0);
});
test('会员隔离且请求完成前切换身份不会返回或清除原会员记录',async()=>{
 const s=setup();await assert.rejects(s.flow.submit(input));s.setMember('2');assert.equal(s.flow.pending(),null);s.setMember('1');assert.equal(s.flow.pending().requestNo,'R1');
 let member='1';const values=new Map();const flow=createCheckoutFlow({storage:{get:k=>values.get(k),set:(k,v)=>values.set(k,v),remove:k=>values.delete(k)},currentMember:()=>member,newRequestNo:()=> 'R1',client:{checkout:async()=>{member='2';return {orderNo:'TR1',status:'RESERVING',payableAmount:'10.00'};}}});
 await assert.rejects(flow.submit(input));assert.equal(values.size,1);
});
test('重复点击被阻止，无效订单响应保留请求',async()=>{
 let finish;const values=new Map();const flow=createCheckoutFlow({storage:{get:k=>values.get(k),set:(k,v)=>values.set(k,v),remove:k=>values.delete(k)},currentMember:()=> '1',newRequestNo:()=> 'R1',client:{checkout:()=>new Promise(resolve=>finish=resolve)}});
 const first=flow.submit(input);await assert.rejects(flow.submit(input));finish({status:'SUCCESS'});await assert.rejects(first);assert.equal(values.size,1);
});
