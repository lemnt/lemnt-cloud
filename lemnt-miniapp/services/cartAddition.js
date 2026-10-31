const {id}=require('./storefront');
function createCartAddition({storage,currentMember,client}){
 let busy=false;const copy=v=>JSON.parse(JSON.stringify(v));
 function scope(){return 'lemnt.cart-add.v2.'+id(currentMember());}
 function validate(v){if(!v||!Number.isInteger(v.quantity)||v.quantity<1||v.quantity>999)throw Error('未确认加购记录损坏，请保留并联系支持');return {productId:id(v.productId),quantity:v.quantity};}
 return {pending(){const v=storage.get(scope());return v?copy(validate(v)):null;},async submit(input){if(busy)throw Error('正在加购，请勿重复点击');busy=true;try{const member=id(currentMember());const key=scope();const saved=storage.get(key);if(!saved&&(storage.get('lemnt.checkout.v1.'+member)||storage.get('lemnt.checkout.v2.'+member)))throw Error('请先处理购物车中的未确认结算');const intent=saved?validate(saved):validate(input);if(!saved)storage.set(key,copy(intent));const persisted=validate(storage.get(key));if(JSON.stringify(persisted)!==JSON.stringify(intent))throw Error('未能确认加购记录已保存，请重试');const before=JSON.stringify(storage.get(key));const result=await client.saveCart(intent.productId,{quantity:intent.quantity,selected:true});if(scope()!==key||JSON.stringify(storage.get(key))!==before)throw Error('会员或本机记录已变化，原加购已保留');if(!result||id(result.productId)!==intent.productId||!Number.isInteger(result.quantity)||result.quantity!==intent.quantity||result.quantity>999)throw Error('加购结果未确认，已保留原请求');storage.remove(key);if(storage.get(key))throw Error("加购已确认，但本机记录清除失败，请重试原请求");return result;}finally{busy=false;}}};
}
module.exports={createCartAddition};
