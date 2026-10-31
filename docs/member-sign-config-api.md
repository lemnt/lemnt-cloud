# 签到配置按ID管理

2026-10-30，对照源MemberSignInConfigDO/Service。规则ID为独立主键，day为唯一业务属性，不能按day覆盖已有奖励。

|方法及服务内路径|权限|入参/返回|
|---|---|---|
|GET /growths/signin-configs|member:sign-in-config:query|按day升序，独立列表Res含id/day/point/experience/status|
|POST /growths/signin-configs|member:sign-in-config:create|独立CreateReq四项规则内容；返回生成的ID|
|PUT /growths/signin-configs/{id}|member:sign-in-config:update|独立UpdateReq四项内容；允许修改day，返回Boolean|
|GET /growths/signin-configs/{id}|member:sign-in-config:query|独立详情Res，按ID查询，不扫描全部规则|
|DELETE /growths/signin-configs/{id}|member:sign-in-config:delete|按ID删除，返回Boolean，保留历史奖励|

day继续1..366，奖励非负且至少一项为正，status按本项目1启用0停用。重复天数新增/修改返回409，不覆盖；修改或删除不存在的ID返回404，非法编号400。查询不存在ID按现有详情约定返回data=null。路径ID用于更新，客户端不能通过新增body指定生成ID。原集合PUT覆盖接口已移除。

后台页面用记录ID作rowKey，编辑允许调整day，删除按ID，创建成功解码实际ID；未知ID不借day补造。请求路径已从旧/member/admin/signin-configs修正为服务真实/growths/signin-configs。新增权限已加入V20261025__member_menu.sql（可重跑不重复定义），角色是否授权仍需部署时配置。App启用规则响应不增加ID字段。

数据库新表member_sign_in_config，id自增主键、day唯一、审计复用本地BaseDO。原member_sign_config旧库执行sql/upgrade/sign_config_id.sql：改表/列、把day主键转唯一键并生成定位ID，保留奖励/状态，未知审计NULL。新生成ID只代表本次升级后的定位键，不是芋道历史ID。必须停写备份并确认结构后执行一次，不能与新库V2重复运行；真实MySQL尚未执行。签到记录旧表另有sign_record_time.sql，按实际库结构分别升级。

网关路由示例docs/config/member-gateway-route.yaml补/growths/**，不StripPrefix；尚未应用到Nacos。H2、MockMvc、React/Node验证分别见交付证据；没有部署HTTP、浏览器E2E或微信验收。源deleted和最终统一路径等仍待收敛。
