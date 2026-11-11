# 前端前缀与网关链路交付

2026-10-31，承接用户确认的前端服务前缀规则和127中路径缺口。按整条请求链路修改，不再分拆单点设计/交付文档。

新增ServiceRouteConfiguration：member/system/auth按实际Controller去服务前缀，mall/trade保留完整路径；member/admin现有流水保留完整路径，三个精确会员认证入口仍优先去auth并保留路径。网关只用RouteLocator和HTTP负载均衡，没有业务client或Dubbo依赖。查询参数保留，相似前缀不匹配。

React全部member用户/元数据/配置请求补/member服务前缀，既有/member/admin流水保持；地区树补/system。小程序修正资料/密码users/user、等级/签到/积分经验growths路径，仍保留/member。会员图片上传改/system/files/app/upload，AuthFilter按会员身份鉴权，后台身份不能进入该上传接口。

35项网关测试通过，新增1项执行真实谓词及StripPrefix覆盖18组服务与路径、精确认证优先、查询参数、相似前缀；原身份隔离测试增加新上传路径。223项前端测试通过；125项小程序测试通过，新增真实客户端构造URL合同验证；既有请求断言同步正确路径，没有弱化业务断言。回归捕获并修正tags相对路径误加前缀的错误。前端build/typecheck/lint/format:check通过，仅既有大chunk提示；git diff --check通过；jar中Dubbo依赖为空，Enforcer规则继续通过。

本批只改请求和转发，没有修改业务服务实现/数据库，所以只运行受影响gateway及其common依赖、UI和miniapp验证，没有重复四业务全量构建。接口docs/service-routing-contract.md；证据evidence/service-prefix-verification.json及/tmp/service-prefix-*.log。

代码路由已提供，无需重复录入Nacos；已有自定义高优先级路由仍需部署核对。本批未启动真实网关/下游集群、未修改运行中Nacos、未执行部署HTTP或浏览器/微信验收。未commit/push。127中的路径缺口已在仓库合同层修正，42.3%保留为该轮审计快照；整体评分在下一轮功能审计再更新，不因单项修正直接宣称全链路验收完成。
