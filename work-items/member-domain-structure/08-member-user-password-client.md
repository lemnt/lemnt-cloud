# 会员密码与 RPC 契约修正

2026-10-27。按用户指定，密码存 member_user；MemberLoginClient 合并到 MemberUserClient。本节是当前实现，替代前文的独立凭证表和独立登录资料 RPC 设计。

- MemberDO 增加 password 映射，写入默认密码组件生成的哈希，并排除密码的 toString 输出。会员资料投影和 HTTP 响应不包含密码。
- 密码重置、原密码校验、密码修改、内部登录资料查询统一使用 MemberUserGateway/MemberUserMapper，读写 member_user.password。密码校验与更新保持会员行锁和本地事务。
- 删除 MemberCredentialGateway、实现、Mapper、DO、Converter、Credential 模型。初始化 SQL 在 member_user 定义 password 列，并移除 member_credential 的建表定义。
- MemberUserClient 保留 getByMemberId，合并 getByMobile，状态判断复用 getByMemberId 的查询结果；认证资料仍使用独立的 MemberLoginInfoDTO，并对诊断输出脱敏。删除独立 MemberLoginClient 和实现。
- auth 的 Dubbo 适配器改为调用 MemberUserClient；auth 仍没有数据库连接和持久化代码，继续复用默认密码、Redis 重试、TokenService 与 LOGIN_TOKEN_KEY。

验证：auth、member、trade、gateway 及依赖 Java 17 离线 clean verify 成功，共 278 项测试通过，失败/错误/跳过均为 0。覆盖密码改写、原密码拒绝、资料脱敏、真实会员表 SQL/事务、合并后的 RPC 查询、auth 无 JDBC/MyBatis 运行类、令牌与业务消费者回归。源码不再引用 member_credential 或独立 MemberLoginClient，client JAR 无旧接口类。git diff --check 通过。

仅修改仓库代码和初始化脚本，未执行实际数据库建表、改表、搬迁或删表，未提交、部署或进行真实 Dubbo/数据库联调。

证据：[清理构建](evidence/member-user-password-client-clean-verify.log)、[边界核对](evidence/member-user-password-client-audit.json)。

## 移除重复状态 RPC

按用户要求删除 MemberUserClient.isActive 和提供者实现。auth 的 RPC 适配器复用 getByMemberId 读取状态，认证服务自行判断会员存在且 status=1。缺失、启用、停用状态的回归继续通过；auth/member/trade/gateway 及依赖 verify 共 278 项通过。证据：[回归日志](evidence/member-active-query-verify.log)。
