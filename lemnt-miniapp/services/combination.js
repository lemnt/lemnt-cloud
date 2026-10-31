function obj(v){if(!v||typeof v!=='object'||Array.isArray(v))throw Error('拼团响应格式不合法');return v;}
function text(v){if(typeof v!=='string')throw Error('拼团文本格式不合法');return v;}
function id(v){const value=typeof v==='number'&&Number.isSafeInteger(v)?String(v):v;if(typeof value!=='string'||!/^[1-9]\d{0,18}$/.test(value)||BigInt(value)>9223372036854775807n)throw Error('拼团编号不合法');return value;}
function integer(v){if(typeof v!=='number'||!Number.isSafeInteger(v)||v<0)throw Error('拼团数值格式不合法');return v;}
function money(v){if(typeof v!=='string'||!/^\d{1,17}\.\d{2}$/.test(v))throw Error('拼团金额格式不合法');return v;}
function list(v,decode){if(!Array.isArray(v))throw Error('拼团列表格式不合法');return v.map(decode);}
function activity(v){obj(v);if(v.status!==0&&v.status!==1)throw Error('拼团活动状态不合法');return {id:id(v.id),name:text(v.name),spuId:id(v.spuId),spuName:v.spuName==null?'':text(v.spuName),picUrl:typeof v.picUrl==='string'&&/^https:\/\//.test(v.picUrl)?v.picUrl:'',marketPrice:v.marketPrice==null?null:money(v.marketPrice),combinationPrice:v.combinationPrice==null?null:money(v.combinationPrice),status:v.status,startTime:text(v.startTime),endTime:text(v.endTime),userSize:integer(v.userSize)};}
function detail(v){obj(v);if(v.status!==0&&v.status!==1)throw Error('拼团活动状态不合法');return {id:id(v.id),name:text(v.name),status:v.status,startTime:text(v.startTime),endTime:text(v.endTime),userSize:integer(v.userSize),successCount:integer(v.successCount),spuId:id(v.spuId),totalLimitCount:integer(v.totalLimitCount),singleLimitCount:integer(v.singleLimitCount),products:list(v.products,p=>{obj(p);return {skuId:id(p.skuId),combinationPrice:money(p.combinationPrice)};})};}
function record(v){obj(v);if(![0,1,2].includes(v.status))throw Error('拼团记录状态不合法');return {id:id(v.id),activityId:id(v.activityId),combinationPrice:money(v.combinationPrice),spuId:id(v.spuId),spuName:text(v.spuName),picUrl:typeof v.picUrl==='string'&&/^https:\/\//.test(v.picUrl)?v.picUrl:'',skuId:id(v.skuId),count:integer(v.count),userId:id(v.userId),nickname:v.nickname==null?'':text(v.nickname),avatar:typeof v.avatar==='string'&&/^https:\/\//.test(v.avatar)?v.avatar:'',headId:v.headId===0||v.headId==='0'?'0':id(v.headId),status:v.status,orderId:v.orderId==null?null:id(v.orderId),userSize:integer(v.userSize),userCount:integer(v.userCount),virtualGroup:v.virtualGroup===true,expireTime:text(v.expireTime)};}
const orderRules=require('./promotionOrder');
function createCombinationClient({request,baseUrl,getMemberToken,ready=false}){
 async function call(path,data,auth=false,method='GET'){if(!ready)throw Error('营销服务暂未启用');if(!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))throw Error('请配置合法HTTPS服务地址');const headers={};if(auth){const token=getMemberToken();if(!token)throw Error('请先完成会员登录');headers.Authorization='Bearer '+token;}const r=await request({url:baseUrl.replace(/\/$/,'')+'/mall/app/combination'+path,method,data,header:headers,timeout:15000});if(r.statusCode!==200||r.data?.code!==200)throw Error(r.data?.msg||'拼团读取失败，请重试');return r.data;}
 return {
  activities:async q=>{if(!Number.isSafeInteger(q.pageNum)||q.pageNum<1)throw Error('页码不合法');const r=await call('/activities',{pageNum:q.pageNum,pageSize:20});return {records:list(r.rows,activity),total:integer(r.total)};},
  activity:async key=>{const value=id(key);const r=await call('/activities/'+value);if(id(r.data.id)!==value)throw Error('拼团活动编号不一致');return detail(r.data);},
  heads:async(key,status=0,count=20)=>{const value=id(key);const r=await call('/activities/'+value+'/heads',{status,count});return list(r.data,record);},
  records:async(q={})=>{const r=await call('/records',{activityId:q.activityId? id(q.activityId):undefined,status:q.status,pageNum:q.pageNum||1,pageSize:20},true);return {records:list(r.rows,record),total:integer(r.total)};},
  record:async key=>{const value=id(key);const r=await call('/records/'+value,undefined,true);obj(r.data);return {head:record(r.data.headRecord),members:list(r.data.memberRecords,record),orderId:r.data.orderId==null?null:id(r.data.orderId)};},
  order:async p=>{const r=await call('/orders',{activityId:orderRules.id(p.activityId),headId:p.headId?orderRules.id(p.headId):undefined,skuId:orderRules.id(p.skuId),quantity:orderRules.quantity(p.quantity),addressId:orderRules.addressId(p.addressId),requestNo:orderRules.requestNo(p.requestNo)},true,'POST');return orderRules.decodeOrder(r.data);},
  summary:async()=>{const r=await call('/summary');obj(r.data);return {userCount:integer(r.data.userCount),avatars:list(r.data.avatars,text)};},
 };
}
module.exports={createCombinationClient};
