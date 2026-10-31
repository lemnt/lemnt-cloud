function obj(v){if(!v||typeof v!=='object'||Array.isArray(v))throw Error('砍价响应格式不合法');return v;}
function text(v){if(typeof v!=='string')throw Error('砍价文本格式不合法');return v;}
function id(v){const value=typeof v==='number'&&Number.isSafeInteger(v)?String(v):v;if(typeof value!=='string'||!/^[1-9]\d{0,18}$/.test(value)||BigInt(value)>9223372036854775807n)throw Error('砍价编号不合法');return value;}
function optionalId(v){return v==null?null:id(v);}
function integer(v){if(typeof v!=='number'||!Number.isSafeInteger(v)||v<0)throw Error('砍价数值格式不合法');return v;}
function money(v){if(typeof v!=='string'||!/^\d{1,17}\.\d{2}$/.test(v))throw Error('砍价金额格式不合法');return v;}
function list(v,decode){if(!Array.isArray(v))throw Error('砍价列表格式不合法');return v.map(decode);}
function activity(v){obj(v);return {id:id(v.id),name:text(v.name),spuId:id(v.spuId),spuName:v.spuName==null?'':text(v.spuName),picUrl:typeof v.picUrl==='string'&&/^https:\/\//.test(v.picUrl)?v.picUrl:'',skuPrice:v.skuPrice==null?null:money(v.skuPrice),skuId:id(v.skuId),bargainFirstPrice:money(v.bargainFirstPrice),bargainMinPrice:money(v.bargainMinPrice),stock:integer(v.stock),totalStock:integer(v.totalStock),helpMaxCount:integer(v.helpMaxCount),bargainCount:integer(v.bargainCount),totalLimitCount:integer(v.totalLimitCount),startTime:text(v.startTime),endTime:text(v.endTime),successCount:integer(v.successCount)};}
function record(v){obj(v);if(![1,2,3].includes(v.status))throw Error('砍价记录状态不合法');return {id:id(v.id),userId:id(v.userId),activityId:id(v.activityId),spuId:id(v.spuId),spuName:v.spuName==null?'':text(v.spuName),picUrl:typeof v.picUrl==='string'&&/^https:\/\//.test(v.picUrl)?v.picUrl:'',skuId:id(v.skuId),bargainFirstPrice:money(v.bargainFirstPrice),bargainPrice:money(v.bargainPrice),status:v.status,endTime:v.endTime==null?null:text(v.endTime),orderId:optionalId(v.orderId)};}
function help(v){obj(v);return {id:id(v.id),userId:id(v.userId),reducePrice:money(v.reducePrice)};}
const orderRules=require('./promotionOrder');
function createBargainClient({request,baseUrl,getMemberToken,ready=false}){
 async function call(path,method,data,auth=false){if(!ready)throw Error('营销服务暂未启用');if(!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))throw Error('请配置合法HTTPS服务地址');const header={};if(auth){const token=getMemberToken();if(!token)throw Error('请先完成会员登录');header.Authorization='Bearer '+token;}const r=await request({url:baseUrl.replace(/\/$/,'')+'/mall/app/bargain'+path,method,data,header,timeout:15000});if(r.statusCode!==200||r.data?.code!==200)throw Error(r.data?.msg||'砍价读取失败，请重试');return r.data;}
 return {
  activities:async q=>{if(!Number.isSafeInteger(q.pageNum)||q.pageNum<1)throw Error('页码不合法');const r=await call('/activities','GET',{pageNum:q.pageNum,pageSize:20});return {records:list(r.rows,activity),total:integer(r.total)};},
  activity:async key=>{const value=id(key);const r=await call('/activities/'+value);if(id(r.data.id)!==value)throw Error('砍价活动编号不一致');return activity(r.data);},
  createRecord:async activityId=>({id:id(await call('/records','POST',{activityId:id(activityId)},true).then(r=>r.data))}),
  help:async recordId=>help(await call('/helps','POST',{recordId:id(recordId)},true).then(r=>r.data)),
  record:async key=>{const value=id(key);const r=await call('/records/'+value,'GET',undefined,true);obj(r.data);const detail={activity:activity(r.data.activity),record:record(r.data.record),helps:list(r.data.helps,h=>{obj(h);return {id:id(h.id),userId:id(h.userId),reducePrice:money(h.reducePrice)};}),helped:r.data.helped===true};if(detail.record.id!==value)throw Error('砍价记录编号不一致');return detail;},
  order:async p=>{const r=await call('/orders','POST',{recordId:orderRules.id(p.recordId),skuId:orderRules.id(p.skuId),quantity:orderRules.quantity(p.quantity),addressId:orderRules.addressId(p.addressId),requestNo:orderRules.requestNo(p.requestNo)},true);return orderRules.decodeOrder(r.data);},
  myRecords:async q=>{const r=await call('/records','GET',{activityId:q.activityId? id(q.activityId):undefined,status:q.status,pageNum:q.pageNum||1,pageSize:20},true);return {records:list(r.rows,record),total:integer(r.total)};},
 };
}
module.exports={createBargainClient};
