# 会员认证迁移

- 用户范围：只迁移认证，角色/菜单授权不动；不提交代码。
- [设计与任务](03-development-design.md)
- [交付与验证](05-development.md)


当前有效版本为 v7：两端复用 TokenService/JwtUtils + Redis 及 LOGIN_TOKEN_KEY，由 user_type 隔离身份；删除 MemberRequestSecurity，由 AuthFilter 统一鉴权；gateway无Dubbo或远程HTTP会话校验。历史双令牌、SQL会话、独立会员登录缓存前缀及全设备会话索引均已撤回。

最终证据：[全量及相关回归](evidence/backend-token-main-style/tests.json)、[样式与网关产物核对](evidence/backend-token-main-style/static-validation.json)。

[v6 复用缓存键与统一过滤器验证](evidence/shared-login-key-auth-filter/tests.json)

[v7 去除会话索引验证](evidence/no-member-session-index/tests.json)
