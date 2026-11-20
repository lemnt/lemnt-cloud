# 会员登录连接IP设计

2026-10-29，承接72。缺可信代理链，故只记录网关观察到的socket连接IP，不将客户端X-Forwarded-For或任意输入头当终端IP。网关清除X-Lemnt-Login-Ip并从实际连接重新写入，未知时不写。auth仅消费该内部头，服务须仅内网可达；代理在前时值是代理IP，真实终端IP解析仍未完成。

auth请求Cmd附加IP，不开放JSON参数；密码/会话成功后通过现有recordLogin写动作传member。member按源login_ip varchar(50)落库，与login_date同条Lambda更新；未知IP写NULL避免把上次IP当本次。后台分页详情可读，App/认证DTO不增加。旧私有方法保留仅时间兼容，转发新方法null。IP仅接受数值IP，不进行任意域名查询。

验证伪造头覆盖、未知连接不保留、HTTP转换与成功/失败审计、H2同时存储、后台展示及回归。不执行DDL/发布路由/commit/push；真实代理链和端到端另验收。
