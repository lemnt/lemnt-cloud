const runtime=require('../../services/memberRuntime');
Page({
 data:{ready:runtime.ready,loggedIn:false,loading:false,error:'',success:'',profile:null,summary:null,levels:[],rules:[],records:[],kind:'points',pageNum:1,total:0},
 onLoad(){runtime.attach(this);},
 onShow(){runtime.clear(this);this.setData({loggedIn:!!runtime.current(),pageNum:1,total:0});if(runtime.current())return this.reload();},
 login:runtime.login,
 reload(){return runtime.run(this,async()=>{const [profile,summary,levels,rules,page]=await Promise.all([this.client.profile(),this.client.summary(),this.client.levels(),this.client.signConfigs(),this.client.records(this.data.kind,1)]);this.setData({profile,summary,levels,rules,records:page.records,total:page.total,pageNum:1});});},
 sign(){return runtime.run(this,async()=>{const result=await this.client.sign();this.setData({success:`签到已确认：积分${result.point}，经验${result.experience}。`});try{const [profile,summary,page]=await Promise.all([this.client.profile(),this.client.summary(),this.client.records(this.data.kind,1)]);this.setData({profile,summary,records:page.records,total:page.total,pageNum:1});}catch(e){this.setData({error:'签到已确认，刷新失败：'+e.message});}});},
 kind(e){return runtime.run(this,async()=>{const kind=e.currentTarget.dataset.kind;if(!['points','experience','signin'].includes(kind))throw Error('记录类型不合法');this.setData({kind,records:[],total:0,pageNum:1});const page=await this.client.records(kind,1);this.setData({records:page.records,total:page.total});});},
 page(e){return runtime.run(this,async()=>{const next=this.data.pageNum+Number(e.currentTarget.dataset.delta);if(next<1||(next-1)*20>=this.data.total)return;const page=await this.client.records(this.data.kind,next);this.setData({records:page.records,total:page.total,pageNum:next});});},
});
