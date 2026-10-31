const runtime=require('../../services/memberRuntime');
Page({
 data:{ready:runtime.ready,loggedIn:false,loading:false,error:'',success:'',profile:null,form:{},oldPassword:'',newPassword:''},
 onLoad(){runtime.attach(this);},
 onShow(){runtime.clear(this);this.setData({loggedIn:!!runtime.current()});if(runtime.current())return this.reload();},
 onHide(){this.setData({oldPassword:'',newPassword:''});},
 reload(){return runtime.run(this,async()=>{const profile=await this.client.profile();this.setData({profile,form:{nickname:profile.nickname,email:profile.email||'',avatar:profile.avatar||'',sex:profile.sex,birthday:profile.birthday||''},loggedIn:true});});},
 login:runtime.login,go:runtime.go,
 input(e){const key=e.currentTarget.dataset.field;const value=runtime.input(e);if(['nickname','email','avatar'].includes(key))this.setData({form:{...this.data.form,[key]:value}});else if(['oldPassword','newPassword'].includes(key))this.setData({[key]:value});},
 birthday(e){this.setData({form:{...this.data.form,birthday:e.detail.value}});},
 sex(e){this.setData({form:{...this.data.form,sex:Number(e.detail.value)}});},
 save(){return runtime.run(this,async()=>{if(!this.data.profile)throw Error('请先读取会员资料');if(!this.data.form.nickname.trim())throw Error('昵称不能为空');await this.client.updateProfile(this.data.form);this.setData({success:'资料已保存'});try{const profile=await this.client.profile();this.setData({profile});}catch(e){this.setData({error:'保存成功，资料刷新失败：'+e.message});}});},
 password(){return runtime.run(this,async()=>{await this.client.changePassword(this.data.oldPassword,this.data.newPassword);runtime.clear(this);this.setData({success:'密码已修改，请重新登录。'});});},
 refresh(){return runtime.run(this,async()=>{await this.client.refresh();this.setData({success:'登录会话已刷新'});});},
 logout(){return runtime.run(this,async()=>{try{await this.client.logout();this.setData({success:'已退出登录'});}finally{runtime.clear(this);}});},
});
