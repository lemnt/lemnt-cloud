function obj(v){if(!v||typeof v!=='object'||Array.isArray(v))throw Error('秒杀响应格式不合法');return v;}
function text(v){if(typeof v!=='string')throw Error('秒杀文本格式不合法');return v;}
function id(v){const value=typeof v==='number'&&Number.isSafeInteger(v)?String(v):v;if(typeof value!=='string'||!/^[1-9]\d{0,18}$/.test(value)||BigInt(value)>9223372036854775807n)throw Error('秒杀编号不合法');return value;}
function integer(v){if(typeof v!=='number'||!Number.isSafeInteger(v)||v<0)throw Error('秒杀数值格式不合法');return v;}
function money(v){if(typeof v!=='string'||!/^\d{1,17}\.\d{2}$/.test(v))throw Error('秒杀金额格式不合法');return v;}
function list(v,decode){if(!Array.isArray(v))throw Error('秒杀列表格式不合法');return v.map(decode);}
function config(v){obj(v);return {id:id(v.id),name:text(v.name),startTime:text(v.startTime),endTime:text(v.endTime),sliderPicUrls:list(v.sliderPicUrls,text)};}
function activity(v){obj(v);if(v.status!==0&&v.status!==1)throw Error('秒杀活动状态不合法');return {id:id(v.id),name:text(v.name),spuId:id(v.spuId),spuName:v.spuName==null?'':text(v.spuName),picUrl:typeof v.picUrl==='string'&&/^https:\/\//.test(v.picUrl)?v.picUrl:'',marketPrice:v.marketPrice==null?null:money(v.marketPrice),status:v.status,stock:integer(v.stock),totalStock:integer(v.totalStock),seckillPrice:v.seckillPrice==null?null:money(v.seckillPrice)};}
function detail(v){obj(v);if(v.status!==0&&v.status!==1)throw Error('秒杀活动状态不合法');return {id:id(v.id),name:text(v.name),status:v.status,startTime:text(v.startTime),endTime:text(v.endTime),spuId:id(v.spuId),totalLimitCount:integer(v.totalLimitCount),singleLimitCount:integer(v.singleLimitCount),stock:integer(v.stock),totalStock:integer(v.totalStock),products:list(v.products,p=>{obj(p);return {skuId:id(p.skuId),seckillPrice:money(p.seckillPrice),stock:integer(p.stock)};})};}
function createSeckillClient({request,baseUrl,ready=false}){
 async function call(path,data){if(!ready)throw Error('营销服务暂未启用');if(!/^https:\/\/[^/?#]+(?:\/[^?#]*)?$/.test(baseUrl))throw Error('请配置合法HTTPS服务地址');const r=await request({url:baseUrl.replace(/\/$/,'')+'/mall/app/seckill'+path,method:'GET',data,timeout:15000});if(r.statusCode!==200||r.data?.code!==200)throw Error(r.data?.msg||'秒杀读取失败，请重试');return r.data;}
 return {
  configs:()=>call('/configs').then(r=>list(r.data,config)),
  now:()=>call('/now').then(r=>{obj(r.data);return {config:r.data.config==null?null:config(r.data.config),activities:list(r.data.activities,activity)};}),
  activities:async q=>{if(!Number.isSafeInteger(q.pageNum)||q.pageNum<1)throw Error('页码不合法');const r=await call('/activities',{configId:q.configId?id(q.configId):undefined,pageNum:q.pageNum,pageSize:20});return {records:list(r.rows,activity),total:integer(r.total)};},
  activity:async key=>{const value=id(key);const r=await call('/activities/'+value);if(id(r.data.id)!==value)throw Error('秒杀活动编号不一致');return detail(r.data);},
 };
}
module.exports={createSeckillClient};
