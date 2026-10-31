# 后台会员资料保存设计

承接115/116，参考本地芋道 MemberUserServiceImpl.updateUser、MemberUserBaseVO，补后台手机号维护及手机号/邮箱唯一性校验。源普通编辑同时声明标签、分组、等级；本项目已有独立操作，本批保留这些入口及等级记录流程。

1. 后台独立MemberUserUpdateCmd/Exe，App保留原Cmd；后台mobile省略或null保留原值，非空按现有MemberRules.mobile校验；空串错误，不清空登录凭据。
2. 复用锁定会员资料的事务及局部字段更新，预检手机号/邮箱唯一；数据库唯一约束兜底409，任何失败回滚整次资料更新。只写资料，密码、资产、等级分组标签、登录审计不变。App不接收mobile，不能借附加JSON修改手机号。
3. React编辑支持手机号，API白名单继续隔离资产和关系字段；无DDL、无SMS微信、无认证迁入member。

验证：H2覆盖改号后新旧手机号查询、自有值、他人占用及失败无部分更新，资产/密码/关联保持；MockMvc覆盖App与后台Cmd隔离、非法长度；React请求白名单、省略兼容。四服务verify及前端检查，小程序回归；真实MySQL/Redis/网关/浏览器未验收。
