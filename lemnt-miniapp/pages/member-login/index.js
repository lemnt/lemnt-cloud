const runtime=require('../../services/memberRuntime');
Page({
 data:{ready:runtime.ready,loading:false,error:'',success:'',mode:'password',mobile:'',password:'',code:'',cooldown:0},
 onLoad(){runtime.attach(this);},
 onUnload(){if(this.timer)clearInterval(this.timer);this.setData({password:'',code:''});},
 changeMode(e){if(this.data.loading)return;this.setData({mode:e.currentTarget.dataset.mode,password:'',code:'',error:'',success:''});},
 input(e){const key=e.currentTarget.dataset.field;if(['mobile','password','code'].includes(key))this.setData({[key]:runtime.input(e)});},
 sendCode(){return runtime.run(this,async()=>{if(this.data.cooldown)throw Error('请稍后再发送');await this.client.sendCode(this.data.mobile,this.data.mode==='reset'?'RESET_PASSWORD':'LOGIN');this.setData({cooldown:60,success:'验证码发送请求已确认，请查收短信。'});this.timer=setInterval(()=>{const n=this.data.cooldown-1;this.setData({cooldown:Math.max(0,n)});if(n<=0)clearInterval(this.timer);},1000);});},
 submit(){return runtime.run(this,async()=>{const {mode,mobile,password,code}=this.data;if(mode==='reset'){await this.client.resetPassword(mobile,code,password);this.setData({password:'',code:'',mode:'password',success:'密码已重置，请重新登录。'});return;}if(mode==='password')await this.client.passwordLogin(mobile,password);else await this.client.smsLogin(mobile,code);this.setData({password:'',code:'',success:'登录成功'});wx.redirectTo({url:'/pages/member/index'});});},
 wechat(e){return runtime.run(this,async()=>{if(!e.detail.code)throw Error('微信手机号授权未完成');const state=await this.client.state('LOGIN');const login=await new Promise((resolve,reject)=>wx.login({success:resolve,fail:()=>reject(Error('微信登录失败'))}));if(!login.code)throw Error('未取得微信登录凭证');await this.client.wechatLogin(login.code,e.detail.code,state);wx.redirectTo({url:'/pages/member/index'});});},
});
