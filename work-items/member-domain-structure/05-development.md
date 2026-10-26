# member 结构整理与短信、微信移除交付

2026-10-26；未提交、推送或部署，未执行数据库变更。以当前 system 的分层、独立执行器、边界转换和公共分页模型为参考，保留先前的认证决策。

## 实现

- 删除短信验证码发送/验证/登录/找回密码/手机号换绑，以及微信登录、绑定/解绑、手机号授权、小程序码、订阅模板、JSAPI、OAuth state 和身份核验服务，共移除 16 个 HTTP 操作。删除对应请求、响应、命令、查询、模型、网关、Mapper、Redis 锁适配及 SDK；删除阿里云短信和 WxJava 的四项依赖与不再直接使用的 Redis 依赖。
- 小程序只保留手机号密码登录，清理验证码、微信及手机号换绑的服务方法、页面入口、事件和状态；后台重置密码及登录会员主动修改密码保留。网关只保留密码登录、刷新、退出三个会员匿名入口，旧短信/微信入口不再匿名放行。
- 删除 member 遗留登录/刷新/退出转调和核验后签发链路；移除失去消费者的 auth 内部 MemberAuthenticationClient/MemberLoginBo/令牌DTO和实现。HTTP 密码认证仍在 auth；只读 MemberSessionClient 继续供 member/trade 使用。
- 按 user、address、growth 划分领域模型、应用服务、命令/查询、转换器和持久化实现；拆除大 Account Gateway/Mapper/Service。地址、成长权益通过 user 的领域网关锁会员，保留原事务；签到奖励、经验与等级仍在一个成长权益事务中，不进行服务间循环调用。
- 合并重复的 Member/Profile 和 MemberDO/ProfileDO，会员注册与读取使用完整 Member；保留 trade 消费的 MemberClient 协议。写命令和会员查询按单一输入独立执行；分离本地密码维护 MemberPasswordService 与只读会话 MemberSessionService。
- 按域拆分 DTO/Req/Res 转换器，复用公共 PageResult；原 HTTP data.records/data.total 包装不变。兼容 /members 接口保留，Req/Res 移出 Controller，补充 Serializable、Swagger 和边界转换；修正后台下划线方法名、缩写参数、注释、导入和缩进。

后台跨资源分页仍作为应用层查询入口，使用共享只读筛选契约，分别访问 user/growth 的后台查询网关；不把跨资源报表入口建成领域聚合或混合写服务。HTTP 筛选和返回字段沿用已有契约。

数据库 V1/V2 结构不变；V3 是历史短信/社交建表记录，当前没有运行消费者，也不再作为部署前置条件。未删表或清理已有数据。原会话共用 LOGIN_TOKEN_KEY、会员/后台身份隔离、gateway 无 Dubbo、无会员全设备会话索引的规则不变。

## 验证证据

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| Java 17 离线 Maven clean verify，member/auth/gateway/trade 及依赖 | 315 项通过，失败/错误/跳过均 0 | [构建日志](evidence/backend-clean-verify.log) |
| 最新网关匿名路径专项复核 | 通过，覆盖保留认证入口、已删除旧路径及身份头清理 | [专项日志](evidence/gateway-public-routes.log) |
| 小程序 Node 测试 | 67 项通过，失败/跳过均 0 | [测试日志](evidence/miniapp-tests.log) |
| 接口静态清单比较 | member 83→67；只删除指定的 16 个短信/微信操作；没有新增路径，余下路径不变 | [核对结果](evidence/contract-audit.json) |
| SQL 静态清单比较 | 原账户与后台查询 Mapper 的 66 条注解 SQL 全部保留；注册查询补入 user Mapper | [核对结果](evidence/contract-audit.json) |
| 小程序四个会员页面 | WXML XML结构解析与事件绑定检查通过 | [事件清单](evidence/page-bindings.json) |
| 打包依赖检查 | member 无阿里云短信/WxJava或删除的身份类；gateway 无 Dubbo/auth-client | 检查最终 member-start 与 lemnt-gateway JAR 的目录项 |
| git diff --check | 通过 | 当前工作区检查 |

集成测试使用实际 H2/MyBatis SQL 和 Spring 事务，涵盖默认地址并发、地址数量/归属、积分幂等及回滚、等级调整、签到并发与双资产奖励回滚、分组标签引用、资料校验、密码与 JWT/Redis 会话边界。删除的测试仅覆盖已明确取消的短信/微信功能；相关保留业务测试继续通过。

验证边界：未做部署环境 MySQL/Redis/Nacos/Dubbo 联调、微信开发者工具编译或真机验收。本次未修改 React 管理后台页面；不以先前 UI 测试结果充当本次验证。

## adapter 入口层级修正（2026-10-26）

按用户补充约束，adapter 顶层只有 app、web，下面再分 user、address、growth。删除顶层 shared 及 web/shared，将 HTTP 请求、响应、转换器按入口分别组织；app/web 没有互相引用的适配层类型。异常处理分别限定在各自入口，避免相同异常被重复处理。后台分组、标签分页控制器归 web/user，其余成长记录归 web/growth。

本次修正重新运行 member-start 及依赖的 Java 17 离线 clean verify，共 54 项测试通过，失败、错误、跳过均为 0；其中会员服务 22 项。静态核对保留全部 67 个会员 HTTP 操作，无路径或方法变化。git diff --check 通过。证据：[构建日志](evidence/adapter-boundary-clean-verify.log)、[包和接口核对](evidence/adapter-boundary-audit.json)。前述 315 项为上一次包含 auth/gateway/trade 的整体构建结果，本次未重新执行全部下游模块。

## 统一异常处理（2026-10-26）

按用户要求删除 AppMemberExceptionHandler 和 WebMemberExceptionHandler，使用已有自动配置的 GlobalExceptionHandler。member 的业务校验、会话拒绝和状态冲突改用公共 ServiceException，分别保留 400/401/409；未知故障仍由全局处理器兜底。错误响应采用项目既有 application/problem+json，成功响应仍为 R；未修改公共异常处理器。小程序会员请求读取统一错误体的 detail，同时兼容既有 msg。

member-start 及依赖 clean verify 共 55 项通过（其中会员服务 23 项），失败/错误/跳过为 0；小程序 68 项测试通过。HTTP 测试验证全局处理器的 400/401/409、问题响应格式及身份来源；业务集成测试继续验证幂等、回滚与并发，并断言统一异常的状态码。证据：[后端构建](evidence/unified-errors-clean-verify.log)、[小程序测试](evidence/unified-errors-miniapp.log)。未提交、部署或执行数据库变更。

## 令牌认证统一在网关（2026-10-26）

按用户要求删除 MemberSessionService、MemberSessionGateway 及其实现，移除 member-infrastructure 的 auth-client 依赖。三个会员 app Controller 不再接收 Authorization、解析token或调用认证RPC，改用公共 SecurityUtils.getUserId()。

AuthFilter 校验会员令牌后，先清除外部提供的用户/角色身份头，再写入可信会员编号。会员请求不写后台 userKey，公共 HeaderInterceptor 只装载用户上下文，不触发后台会话查询。公共接口继续清除身份头；后台认证与会员认证的路径隔离规则保持。业务服务依赖网关的内部访问边界，领域业务状态检查仍保留。trade 当前仍使用的独立会话协议没有作为孤立代码删除。

member-start、gateway、trade-start 及依赖 clean verify 共 312 项通过，失败/错误/跳过均为 0。网关测试覆盖伪造用户头覆盖、过期会话拒绝、后台/会员令牌隔离；会员 HTTP 测试通过真实 HeaderInterceptor 读取网关用户头，无 Authorization 仍能取得可信上下文，并断言请求后清理。最终 member JAR 无 auth-client 或会员会话服务/网关类，gateway JAR 无 Dubbo。证据：[构建日志](evidence/gateway-auth-only-clean-verify.log)、[测试和包核对](evidence/gateway-auth-only-tests.json)。未提交或部署。

## 去除 Admin 命名、独立接口对象、统一分页（2026-10-26）

按后续用户指令，重命名 19 个含 Admin 的类型（包含 Controller、请求/查询、执行器、查询网关与实现、Mapper、筛选模型），同步标识符和引用。后台更新资料请求命名 MemberProfileUpdateReq，避免与小程序 ProfileUpdateReq 混淆。路由中的 /admin、权限码和已持久化业务来源 ADMIN/ADMIN_LEVEL 不变。

三个小程序记录分页使用 SignRecordPageReq、PointRecordPageReq、ExperienceRecordPageReq，仅包含分页参数，资产类型由转换方法确定。member 其他被多个接口使用的业务 Req/Res 也分别拆开，包括创建/更新、列表/详情、全局和单会员记录查询。静态核对 67 个 HTTP 操作，没有路径或方法变化，也没有跨接口共用的业务 Req/Res；标量与项目统一 R 继续复用。

分页使用 system 的 R.page：data=null，rows=记录数组，total=总数。删除两份 AccountPageRes 及只用于嵌套分页包装的 MemberResConverter，Controller 直接转换独立 Res 后返回 R.page。后台会员 API 解码和小程序会员客户端同步切换到 rows/total，页面内部数据结构保持。接口文档与分页示例已同步，模型表按当前独立 Req/Res 重建。本次同时补齐 member-adapter 的 member-application 依赖版本，解决当前 POM 缺失版本导致无法构建的问题。

验证：member-start 及依赖 Java17 离线 clean verify 共 58 项通过，失败/错误/跳过为0；后台 typecheck、lint、format:check 和 169 项测试通过；小程序 69 项通过。HTTP 回归覆盖三种分页默认值/显式页码、类型隔离、越界校验及统一 rows 返回；静态核对无 Admin 类型、自定义分页类或共用业务模型。证据：[后端构建](evidence/independent-contracts-clean-verify.log)、[接口核对](evidence/independent-contracts-audit.json)、[UI测试](evidence/pagination-ui-test.log)、[小程序测试](evidence/pagination-miniapp-test.log)。未部署或提交。

## 编译修复与默认密码处理（2026-10-26）

用户提供的 testCompile 错误指向 MemberAccountIntegrationTest 对 auth.application/domain/infrastructure 实现类的直接引用。将 6 项 JWT/共享缓存键、刷新与并发、退出、停用状态、失败次数的认证集成测试迁入 auth-start 的 MemberAuthenticationIntegrationTest，auth 使用本服务真实 Mapper 和事务，凭证/会员状态由数据库测试夹具准备。member 移除对 auth-application/auth-infrastructure 的测试依赖及所有 auth 内部引用，保留会员业务测试，并补充密码凭证维护检查；没有通过删除认证覆盖掩盖错误。

根据当前 Controller 的 /app/addresses、/app/growths 映射修正 HTTP 测试旧路径，仅修改测试，不修改生产路由。删除 MemberPasswordGateway、实现和 MemberPasswordService；重置、修改密码分别由 MemberPasswordResetCmdExe、MemberPasswordChangeCmdExe 直接调用 MemberUserGateway 和公共 SecurityUtils.encryptPassword/matchesPassword。事务仍覆盖会员行锁、原密码验证和凭证写入，缺少凭证/原密码错误不会覆盖旧密码。

member-start、auth-start 及依赖 Java17 离线 clean verify 共 63 项通过，失败/错误/跳过均为0，其中 auth 独立认证集成测试6项、member业务集成测试15项。证据：[干净构建](evidence/password-executors-clean-verify.log)、[测试统计](evidence/password-executors-tests.json)。没有引入新的密码抽象，没有提交代码。

额外复核：仅选择 member-start 及其自身依赖执行离线 clean test-compile，BUILD SUCCESS，构建依赖中不再包含 auth。见[独立测试编译](evidence/member-independent-test-compile.log)。
