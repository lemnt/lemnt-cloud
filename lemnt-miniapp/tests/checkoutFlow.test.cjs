const {test}=require('node:test');const assert=require('node:assert/strict');
const {createCheckoutFlow}=require('../services/checkoutFlow');
const input={productIds:['5'],settlementId:'12345678-1234-1234-1234-123456789abc'};
function setup() {
 const values=new Map(),calls=[]; let member='1',fail=true;
 const storage={get:k=>values.get(k),set:(k,v)=>values.set(k,v),remove:k=>values.delete(k)};
 const flow=createCheckoutFlow({storage,currentMember:()=>member,newRequestNo:()=> 'R1',client:{checkout:async data=>{calls.push(data);if(fail)throw Error('lost response');return {orderNo:'TR1',status:'RESERVING',payableAmount:'10.00'};}}});
 return {flow,storage,values,calls,setMember:v=>member=v,succeed:()=>fail=false};
}
test('未知结果保留原载荷，重试不能替换成新报价',async()=>{
 const s=setup();await assert.rejects(s.flow.submit(input));assert.equal(s.flow.pending().requestNo,'R1');
 s.succeed();await s.flow.submit({...input,settlementId:'22345678-1234-1234-1234-123456789abc',productIds:['6']});assert.deepEqual(s.calls[0],s.calls[1]);assert.equal(s.flow.pending(),null);
});
test('存储失败禁止发请求，损坏记录禁止自动覆盖',async()=>{
 let called=false;const flow=createCheckoutFlow({storage:{get:()=>null,set:()=>{throw Error('full');}},currentMember:()=> '1',newRequestNo:()=> 'R1',client:{checkout:async()=>called=true}});
 await assert.rejects(flow.submit(input));assert.equal(called,false);
 const s=setup();s.values.set('lemnt.checkout.v2.1',{bad:true});await assert.rejects(s.flow.submit(input));assert.equal(s.calls.length,0);
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

test('旧版未确认记录不能被新版结算静默覆盖',async()=>{
 const values=new Map([['lemnt.checkout.v1.1',{requestNo:'OLD'}]]);let calls=0;
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k)},currentMember:()=> '1',newRequestNo:()=> 'R1',client:{checkout:async()=>calls++}});
 await assert.rejects(flow.submit(input),/旧版/);assert.equal(calls,0);assert.equal(values.size,1);
});

test('仅明确确认后可清除当前会员本机记录，不影响其他会员',()=>{
 const values=new Map([['lemnt.checkout.v1.1',{}],['lemnt.checkout.v2.1',{}],['lemnt.checkout.v2.2',{}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=> '1'});
 assert.throws(()=>flow.discard(false));assert.equal(values.size,3);flow.discard(true);assert.equal(values.size,2);assert.ok(values.has('lemnt.checkout.v2.1'));assert.ok(values.has('lemnt.checkout.v2.2'));assert.throws(()=>flow.discard(true));
});

test('原请求查回订单不重新提交，并支持旧版记录',async()=>{
 for(const version of ['v1','v2']){
 const key='lemnt.checkout.'+version+'.1';const values=new Map([[key,{requestNo:'OLD'}]]);let submitted=0;
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=> '1',client:{checkout:async()=>submitted++,checkoutRequest:async key=>{assert.equal(key,'OLD');return {orderNo:'TR1',status:'PAID',payableAmount:'10.00'};}}});
 assert.equal((await flow.recover()).orderNo,'TR1');assert.equal(values.size,0);assert.equal(submitted,0);
 }
});
test('查无结果或响应不合法不会删除待核验请求',async()=>{
 const values=new Map([['lemnt.checkout.v2.1',{requestNo:'R1'}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=> '1',client:{checkoutRequest:async()=>null}});
 await assert.rejects(flow.recover());assert.equal(values.size,1);
});
test('查回期间换号或本机请求变化时保留记录',async()=>{
 for(const kind of ['member','record']){
 let member='1';const key='lemnt.checkout.v2.1';const values=new Map([[key,{requestNo:'R1'}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=>member,client:{checkoutRequest:async()=>{if(kind==='member')member='2';else values.set(key,{requestNo:'R2'});return {orderNo:'TR1',status:'PAID',payableAmount:'10.00'};}}});
 await assert.rejects(flow.recover());assert.equal(values.size,1);
 }
});

test('找回旧版请求不会清除并存的新版请求',async()=>{
 const values=new Map([['lemnt.checkout.v1.1',{requestNo:'OLD'}],['lemnt.checkout.v2.1',{requestNo:'NEW',settlementId:'12345678-1234-1234-1234-123456789abc'}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=> '1',client:{checkoutRequest:async()=>({orderNo:'TR1',status:'PAID',payableAmount:'10.00'})}});
 await flow.recover();assert.equal(flow.pending().requestNo,'NEW');assert.equal(values.size,1);
});

test('服务端确认终止或找回订单后才删除新版记录',async()=>{
 for(const result of [{abandoned:true},{abandoned:false,order:{orderNo:'TR1',status:'RESERVING',payableAmount:'10.00'}}]){
 const key='lemnt.checkout.v2.1';const values=new Map([[key,{settlementId:'12345678-1234-1234-1234-123456789abc',requestNo:'R1'}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=> '1',client:{abandonCheckout:async()=>result}});
 assert.equal(await flow.abandon(true),result);assert.equal(values.size,0);
 }
});
test('终止响应丢失、格式不明或换号都保留请求',async()=>{
 for(const kind of ['network','invalid','member']){
 let member='1';const key='lemnt.checkout.v2.1';const values=new Map([[key,{settlementId:'12345678-1234-1234-1234-123456789abc',requestNo:'R1'}]]);
 const flow=createCheckoutFlow({storage:{get:k=>values.get(k),remove:k=>values.delete(k)},currentMember:()=>member,client:{abandonCheckout:async()=>{if(kind==='network')throw Error('lost');if(kind==='member')member='2';return kind==='invalid'?{abandoned:false}:{abandoned:true};}}});
 await assert.rejects(flow.abandon(true));assert.equal(values.size,1);
 }
});
