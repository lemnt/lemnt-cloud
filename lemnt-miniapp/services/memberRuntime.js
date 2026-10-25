const config=require('../config');
const {createMemberClient}=require('./member');
function current(){return getApp().globalData.memberSession;}
function attach(page){page.client=createMemberClient({request:options=>new Promise((resolve,reject)=>wx.request({...options,success:resolve,fail:()=>reject(Error('网络未确认，请刷新核验后再操作'))})),baseUrl:config.apiBaseUrl,ready:config.memberApiReady,getSession:current,setSession:value=>{getApp().globalData.memberSession=value;}});}
function clear(page){page.setData({profile:null,rows:[],records:[],bindings:[],summary:null,levels:[],rules:[],form:{},oldPassword:'',newPassword:'',mobile:'',code:'',oldCode:'',loggedIn:!!current()});}
async function run(page,work){if(page.data.loading)return;page.setData({loading:true,error:'',success:''});const before=current()?.accessToken;try{await work();}catch(e){if(before!==current()?.accessToken)clear(page);page.setData({error:e.message||'操作失败，请重试'});}finally{if(before!==current()?.accessToken)clear(page);page.setData({loading:false});}}
function input(event){return event.detail.value;}
function login(){wx.navigateTo({url:'/pages/member-login/index'});}
function go(event){const page=event.currentTarget.dataset.page;if(['member','member-addresses','member-benefits'].includes(page))wx.navigateTo({url:'/pages/'+page+'/index'});}
module.exports={attach,run,clear,current,input,login,go,ready:config.memberApiReady};
