# 会员登录连接IP交付

2026-10-29，承接73。网关清除外部X-Lemnt-Login-Ip后用remoteAddress重写，缺连接时不保留伪造头。auth通过独立Cmd上下文和ReqConverter传递IP，不接受JSON设置；现有recordLogin RPC写member login_ip与login_date，后台分页/详情和详情页面展示。源字段varchar(50)，历史NULL不回填，未知连接清除上次IP。App/认证响应不增加，资料更新不覆盖。

旧recordLogin(id)兼容并保留provider事务；新增方法锁会员核验启用、验证数值IP。会话/审计失败逻辑沿72，auth仍无数据库，gateway未引入Dubbo。测试发现的时间断言按DATETIME秒精度校正，未放宽业务失败校验。

相关verify通过：auth18、member31、gateway33、mall118、trade158，共358项失败/错误/跳过0。事务兼容补齐后auth/member/gateway再回归通过；ReqConverter收敛后auth再次verify通过。新增测试覆盖网关覆盖伪造头与未知连接、HTTP上下文、成功登录IP写动作及H2保存/非法IP/清除。React191项及类型/代码/格式和构建通过。

重要边界：记录网关观察到的连接对端，代理在前时可能是代理IP；没有实现可信代理链，也不能宣称真实终端IP。部署若使用转发地址变换器，必须明确信任策略；auth必须只允许内部网关访问。本批未发布代理/访问策略，未真实网络或浏览器验收，升级脚本仅生成未执行，未commit/push。注册来源与终端仍待迁移，文章排除。

接口docs/member-profile-mark-api.md；证据evidence/member-login-ip-verification.json。
