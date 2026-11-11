# auth 无数据库边界修正

> 后续按用户指定改为 member_user.password，并将登录资料接口合入 MemberUserClient，当前实现见 [会员密码与 RPC 契约修正](08-member-user-password-client.md)。本节保留当时修正记录。

2026-10-27。用户明确 auth 服务没有数据库，且不得随意新增表。本节替代此前将会员表 Mapper 放入 auth 的设计。

- auth 删除会员 Mapper、DO、数据库转换器、MapperScan、数据源配置以及 datasource/MyBatis/JDBC/H2 相关直接依赖和数据库测试夹具。
- 新增 member-client 的 MemberLoginClient 读取契约，由 member/application/dubbo 实现，读取仓库既有会员资料与密码映射；同一查询事务在 member 内完成，不跨 RPC 持有数据库事务。
- auth 通过 MemberAccountGateway 的 Dubbo 实现读取登录资料，使用现有默认 PasswordEncoderGateway 校验密码，使用现有 LoginSecurityGateway 的 Redis 重试策略，会员重试键加 member: 前缀与后台分开。
- TokenService、LOGIN_TOKEN_KEY、会员/后台令牌类型隔离保持原有行为。刷新时通过会员 RPC 检查当前状态；登出只删除当前会员会话。
- 登录资料 DTO 和领域模型的 toString 不打印密码哈希；登录资料不作为 HTTP 返回值。

## 表结构边界

member_credential 是仓库此前已提交的 V2__member_account.sql 中的定义，字段为 member_id、password_hash；脚本存在不代表实际数据库已建表，也不作为用户已确认结构的依据。本轮没有新增表、没有更改会员 SQL 或表结构、没有执行建表或迁移。原 member_login_attempt 数据库计数逻辑从 auth 移除，改为复用后台已有 Redis 组件；历史 SQL 文件保持。

## 验证

Java 17 离线 auth-start、member-start、gateway 及依赖 clean verify 成功，68 项测试通过，失败/错误/跳过均为 0。

- auth 不启动数据库的测试覆盖密码、Redis 重试、JWT/缓存、刷新、并发续期、登出和禁用状态。
- auth 的类路径回归断言 JDBC、MyBatis 运行类、MySQL 驱动、Hikari、H2 不存在；打包依赖检查通过。共享依赖中保留的 mybatis-plus-annotation 仅为注解包。
- member 的真实 MyBatis/H2 事务测试验证新 RPC 提供者读取既有密码、当前状态和缺失会员，并检查密码诊断脱敏。
- auth 的 RPC 适配器委托与 DTO 映射测试通过。网关现有令牌/身份测试继续通过。
- 会员三个 SQL 文件与本轮开始时完全一致。git diff --check 通过。

未做真实 Redis/Nacos/Dubbo 或实际数据库联调，未提交、部署或执行迁移。

证据：[清理构建](evidence/auth-no-database-clean-verify.log)、[边界核对](evidence/auth-no-database-audit.json)。
