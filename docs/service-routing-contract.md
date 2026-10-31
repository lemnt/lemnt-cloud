# 服务前缀与网关转发

2026-10-31。前端保留服务前缀，网关按各服务实际Controller路径处理。本批统一请求及代码路由，未修改运行中Nacos。

|外部路径|目标服务|转发路径|
|---|---|---|
|/member/member-users/**|member|/member-users/**|
|/member/groups/**、/member/tags/**|member|/groups/**、/tags/**|
|/member/growth/**、/member/growths/**|member|/growth/**、/growths/**|
|/member/app/users/user、/member/app/users/user/password|member|/app/users/user、/app/users/user/password|
|/member/app/growths/**|member|/app/growths/**|
|/member/app/addresses/**|member|/app/addresses/**|
|/member/admin/**（既有流水）|member|保持完整路径，与现有Controller一致|
|三个精确/member/app/auth入口|auth|保持完整路径，优先级最高|
|/auth/**|auth|去掉/auth|
|/system/**|system|去掉/system|
|/system/files/app/upload|system|/files/app/upload，按会员身份鉴权|
|/mall/**|mall|保持完整路径|
|/trade/**|trade|保持完整路径|

普通服务路由在ServiceRouteConfiguration，精确会员认证仍由MemberAuthRouteConfiguration提供；order分别为-8、既有会员流水-9、精确认证-10。新代码优先于默认order=0的动态路由；已有自定义更高优先级Nacos路由仍需部署核对。认证白名单策略不因转发而扩大。会员认证不调用member，gateway不依赖Dubbo。

小程序资料、密码、等级、签到、积分经验流水已按实际users/growths业务路径调整；上传使用/system服务前缀。后台member API和地区树均保留对应服务前缀。三个认证入口、mall和trade路径保持实际Controller要求，不能全局统一去一层。

路由测试实际执行谓词及StripPrefix，检查18组目标服务/路径、精确认证优先级、查询参数及相似前缀隔离。小程序使用真实客户端构造URL，接口测试保持原断言约束；这些属于代码层合同验证，未启动真实网关/Nacos/下游集群。

需部署新网关和前端/小程序版本后再做实际联调；本批不应用远程配置、不修改数据库、不提交代码。
