const runtime=require('../../services/memberRuntime');
const blank=()=>({name:'',mobile:'',areaId:'',detailAddress:'',defaultStatus:false});
Page({
 data:{ready:runtime.ready,loggedIn:false,loading:false,error:'',success:'',rows:[],editing:false,editId:null,form:blank()},
 onLoad(){runtime.attach(this);},
 onShow(){runtime.clear(this);this.setData({loggedIn:!!runtime.current(),editing:false,editId:null,form:blank()});if(runtime.current())return this.reload();},
 login:runtime.login,
 reload(){return runtime.run(this,async()=>{this.setData({rows:await this.client.addresses()});});},
 add(){if(this.data.loading)return;this.setData({editing:true,editId:null,form:blank()});},
 edit(e){if(this.data.loading)return;const row=this.data.rows.find(r=>r.id===e.currentTarget.dataset.id);if(!row)return;this.setData({editing:true,editId:row.id,form:{name:row.name,mobile:row.mobile,areaId:row.areaId,detailAddress:row.detailAddress,defaultStatus:row.defaultStatus}});},
 input(e){const key=e.currentTarget.dataset.field;if(['name','mobile','areaId','detailAddress'].includes(key))this.setData({form:{...this.data.form,[key]:runtime.input(e)}});},
 toggle(e){this.setData({form:{...this.data.form,defaultStatus:e.detail.value}});},
 cancel(){if(!this.data.loading)this.setData({editing:false,editId:null,form:blank()});},
 save(){return runtime.run(this,async()=>{const form=this.data.form;if(!form.name?.trim()||!form.detailAddress?.trim())throw Error('请填写收件人和详细地址');await this.client.saveAddress(this.data.editId,form);this.setData({editing:false,editId:null,form:blank(),success:'地址已保存'});try{this.setData({rows:await this.client.addresses()});}catch(e){this.setData({error:'保存成功，刷新失败：'+e.message});}});},
 remove(e){return runtime.run(this,async()=>{const key=e.currentTarget.dataset.id;if(!this.data.rows.some(r=>r.id===key))throw Error('请刷新地址簿');const answer=await new Promise((resolve,reject)=>wx.showModal({title:'删除地址',content:'确认删除此收货地址？',success:resolve,fail:reject}));if(!answer.confirm)return;await this.client.deleteAddress(key);this.setData({success:'地址已删除',rows:this.data.rows.filter(r=>r.id!==key)});try{this.setData({rows:await this.client.addresses()});}catch(e){this.setData({error:'删除成功，刷新失败：'+e.message});}});},
});
