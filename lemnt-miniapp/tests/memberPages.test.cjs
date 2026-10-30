const {test}=require('node:test');const assert=require('node:assert/strict');
function definition(name){let value;global.Page=p=>value=p;delete require.cache[require.resolve('../pages/'+name+'/index')];require('../pages/'+name+'/index');return value;}
function setup(name){let session={memberId:'1',accessToken:'one'};global.getApp=()=>({globalData:{get memberSession(){return session;},set memberSession(v){session=v;}}});global.wx={showModal:o=>o.success({confirm:true}),navigateTo:()=>{},redirectTo:()=>{}};const d=definition(name);const p={...d,data:JSON.parse(JSON.stringify(d.data)),setData(v){Object.assign(this.data,v);}};return {p,switch:()=>session={memberId:'2',accessToken:'two'},get:()=>session};}
test('个人资料保存成功但刷新失败时保持成功信息，不诱导重复保存',async()=>{const {p}=setup('member');p.data.profile={id:'1'};p.data.form={nickname:'n'};p.client={updateProfile:async()=>true,profile:async()=>{throw Error('network');}};await p.save();assert.match(p.data.success,/已保存/);assert.match(p.data.error,/刷新失败/);});
test('请求期间切换账号，旧会员资料不会留在页面',async()=>{const x=setup('member');x.p.client={profile:async()=>{x.switch();return {id:'1'};}};await x.p.reload();assert.equal(x.p.data.profile,null);});
test('地址删除必须确认，取消和false都不能显示成功',async()=>{const {p}=setup('member-addresses');p.data.rows=[{id:'7'}];let calls=0;p.client={deleteAddress:async()=>{calls++;throw Error('未确认');}};global.wx.showModal=o=>o.success({confirm:false});const event={currentTarget:{dataset:{id:'7'}}};await p.remove(event);assert.equal(calls,0);global.wx.showModal=o=>o.success({confirm:true});await p.remove(event);assert.equal(calls,1);assert.equal(p.data.rows.length,1);assert.equal(p.data.success,'');});
test('地址保存成功后刷新失败保留成功且关闭编辑，不重复创建',async()=>{const {p}=setup('member-addresses');p.data.editing=true;p.data.form={name:'n',detailAddress:'d'};let calls=0;p.client={saveAddress:async()=>{calls++;},addresses:async()=>{throw Error('network');}};await p.save();assert.equal(calls,1);assert.equal(p.data.editing,false);assert.match(p.data.success,/已保存/);assert.match(p.data.error,/刷新失败/);});
test('签到未确认不伪造奖励，确认成功但刷新失败保留明确结果',async()=>{const {p}=setup('member-benefits');p.client={sign:async()=>{throw Error('unknown');}};await p.sign();assert.equal(p.data.summary,null);assert.equal(p.data.success,'');p.client={sign:async()=>({point:5,experience:2}),profile:async()=>{throw Error('network');},summary:async()=>null,records:async()=>null};await p.sign();assert.match(p.data.success,/积分5/);assert.match(p.data.error,/已确认，刷新失败/);});
test('分页失败保持当前页，重复点击被加载锁阻止',async()=>{const {p}=setup('member-benefits');p.data.total=40;let resolve;p.client={records:()=>new Promise(r=>resolve=r)};const event={currentTarget:{dataset:{delta:'1'}}};const pending=p.page(event);assert.equal(p.data.loading,true);await p.page(event);resolve({records:[],total:40});await pending;assert.equal(p.data.pageNum,2);});

test('密码登录成功清空密码并进入会员中心',async()=>{const {p}=setup('member-login');p.data.mobile='13800000001';p.data.password='Example-password-2026';let credentials;let url;global.wx.redirectTo=o=>url=o.url;p.client={passwordLogin:async(...v)=>credentials=v};await p.submit();assert.deepEqual(credentials,['13800000001','Example-password-2026']);assert.equal(p.data.password,'');assert.equal(url,'/pages/member/index');});

test('积分支出与日期筛选翻页保留已应用条件，修改草稿不影响当前分页',async()=>{
 const {p}=setup('member-benefits');const calls=[];p.client={records:async(...v)=>{calls.push(v);return {records:[],total:40};}};
 p.data.pointStartDate='2026-10-01';p.data.pointEndDate='2026-10-30';
 await p.pointStatus({currentTarget:{dataset:{status:'false'}}});
 const filters={addStatus:false,startTime:'2026-10-01 00:00:00',endTime:'2026-10-30 23:59:59'};
 assert.deepEqual(calls[0],['points',1,filters]);
 p.pointDate({currentTarget:{dataset:{field:'pointStartDate'}},detail:{value:'2026-10-02'}});
 await p.page({currentTarget:{dataset:{delta:'1'}}});
 assert.deepEqual(calls[1],['points',2,filters]);assert.equal(p.data.pageNum,2);
 await p.resetPoints();assert.deepEqual(calls[2],['points',1,{}]);assert.equal(p.data.pageNum,1);
});

test('切换经验记录清空积分筛选与日期',async()=>{
 const {p}=setup('member-benefits');p.data.pointAddStatus='false';p.data.pointStartDate='2026-10-01';p.data.pointFilters={addStatus:false};
 let args;p.client={records:async(...v)=>{args=v;return {records:[],total:0};}};
 await p.kind({currentTarget:{dataset:{kind:'experience'}}});
 assert.deepEqual(args,['experience',1]);assert.deepEqual(p.data.pointFilters,{});assert.equal(p.data.pointStartDate,'');assert.equal(p.data.pointAddStatus,'all');
});
