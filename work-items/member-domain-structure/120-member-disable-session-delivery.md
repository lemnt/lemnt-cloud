# 停用会员撤销会话交付

2026-10-31，承接119，补齐本地芋道MemberUserServiceImpl.updateUser保存停用状态后撤销会话的逻辑。

后台明确保存status=0，在会员行锁和资料事务内通过MemberAuthGateway同步调用既有auth MemberSessionClient的新撤销方法。auth调用公共TokenService遍历LOGIN_TOKEN_KEY缓存，仅删除指定ID、真实身份member的会话。保留同ID后台及其他会员；member不读写解析token，auth无DB，gateway不引入Dubbo。撤销RPC只访问缓存不回调member；远程失败回滚本次状态、普通资料及手机号。

公共Redis封装使用SCAN游标逐项处理、try-with-resources关闭，不用KEYS、不恢复Set或额外索引。会员刷新使用原子SET XX附带原TTL，只续期仍存在会话，防止删除后被读取旧对象的刷新重建。创建token继续原组件原路径。既有登录recordLogin锁定校验及失败撤销逻辑保留。

新增member两项H2验证明确停用调用、启用/App不调用、远程失败整次回滚；auth两项新增多会话/身份域隔离和旧对象刷新，并把旧的重新启用恢复缓存预期改为显式RPC撤销后不能恢复；gateway新增真实签名JWT先放行、公共会话删除后拒绝；common-security新增4项覆盖SET XX及非法编号、游标异常关闭和序列化TTL调用。Redis/Dubbo为替身，未运行真实Redis和跨进程RPC。

common+server临时Maven聚合器联合verify通过，最终common-security及gateway再verify通过：member105/auth20/mall119/trade158，共402项业务测试；gateway34项、common-security29项通过，失败/错误/跳过0。git diff --check通过。日志/tmp/member-disable-session-verify.log及/tmp/member-disable-session-final-verify.log；证据evidence/member-disable-session-verification.json；接口说明docs/member-disable-session-api.md。前端和小程序无改动，本批未重复运行其测试。

临时聚合器将现有common和server源码放入同一reactor，无install、无仓库POM结构改动。单独构建server时需按项目已有方式先更新common依赖，避免使用旧版common制品。

没有分布式原子提交：撤销后若SQL提交失败，会员资料回滚但需重新登录；没有索引时停用需遍历会话，实际耗时和Redis集群游标覆盖待部署验收。无DDL/commit/push，无真实Redis/Nacos/Dubbo/部署HTTP/浏览器E2E验收。完整用户契约仍in_progress，文章排除。

2026-10-31补充网关依赖约束：源码、Maven依赖树和BOOT-INF/lib均无Dubbo及auth/member-client。网关POM增加Enforcer BannedDependencies，禁止直接/传递引入org.apache.dubbo:*与lemnt-common-dubbo；联合verify及34项网关测试通过，临时注入依赖的反向验证确实被该规则拒绝，临时POM已删除。证据evidence/gateway-no-dubbo-verification.json；无提交。
