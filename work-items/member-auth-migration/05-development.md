# 会员认证迁移交付

当前交付状态为文末 v7。v1-v6 保留历史证据；其中双令牌、网关远程校验、独立会员缓存前缀和 MemberRequestSecurity 均已被后续方案替代；全设备会话索引已按 v7 删除。

需求 ID：member-auth-migration；版本 v1；更新 2026-10-26；责任 Skill：code-software-engineer。
输入：用户只迁移认证、03-development-design.md v1、f3449d3。未提交代码，未部署。

## 实现映射

- 任务1：auth 新增 lemnt-auth-client（MemberAuthenticationClient、MemberSessionClient、MemberLoginBo、MemberTokenDTO）；domain/member 定义认证存储与最小模型；application/member/service/MemberAuthenticationService 承接原认证算法；application/dubbo 提供两个独立内部 RPC；infrastructure/member 承接认证 SQL、BCrypt 校验和哈希会话存储；start 增加 MapperScan 与会员库配置。
- 任务2：member 的 MemberAuthService 保留密码本地维护并通过 MemberAuthenticationGateway 调用 auth；MemberIdentityVerificationService 承接身份核验事务，MemberIdentityService 在核验提交后请求签发。密码/手机号修改、禁用仍在本地事务中撤销同库会话。旧会员会话 RPC 提供者、契约及迁移产生的孤立会话/限流存储代码已删除。
- 任务3：gateway 的 DubboMemberSessionLookup、trade 的 MemberSessionVerifierImpl 改用 auth-client。AuthFilter 依接口路径分流，会员分支先于后台白名单；会员与后台令牌保持互斥。后台角色和菜单授权逻辑未改。
- 任务4：docs/member-api.md 更新认证归属；本设计记录共享数据库、服务同步升级及回滚边界。HTTP 路径/字段和已有前端调用不变。

## 配置与发布边界

auth 增加 MEMBER_AUTH_JDBC_URL、MEMBER_AUTH_DB_USERNAME、MEMBER_AUTH_DB_PASSWORD，连接现有会员 V1/V2/V3 数据库；dynamic primary=member，strict=true，不能被 Nacos 覆盖为系统库。配置仍需在实际环境完成，不包含真实凭证。无 DDL 或数据复制，旧令牌沿用。auth/member/gateway/trade 需同步升级，旧 MemberSessionClient 消费者需切换新契约。内部签发 RPC 延续可信内网边界，不可开放公网。

短信/社交核验事务先提交再签发，避免远程 auth 等待本地未提交的会员行锁；若签发失败，核验结果已消费，需重新核验。未引入分布式事务，也不声称跨服务原子回滚。

## 验证

- PASS：Java17、Maven3.9.12，相关模块及其依赖 clean test；317 测试，失败 0、错误 0、跳过 0。命令和逐套件结果见 evidence/tests.json，日志见 evidence/maven-tests.log。首次普通 Mockito 启动等待被终止；显式预加载 Mockito agent 后正常运行。一次编译发现 member-infrastructure 缺少 Dubbo 依赖，已修复并清理重测。
- PASS：26 个会员数据库集成用例实际使用 auth 生产 Mapper、认证服务和 Spring 事务代理，覆盖哈希会话、密码限流、刷新轮换/竞争、注销/撤销、身份核验及绑定。签发前断言会员事务已结束，独立数据库连接能读到已提交会员。
- PASS：新增 AuthFilterDomainTest 3 个用例通过真实签名 JWT 和网关总入口验证双向令牌隔离，以及各自有效会话放行；原网关伪造头清理和故障关闭测试保留。
- PASS：git diff --check；静态确认 auth 不依赖 member 实现，member 仅通过 auth-client 与领域 Gateway 调用。
- NOT RUN：真实 MySQL 方言、Nacos 配置、Dubbo 跨进程调用及序列化、真实短信/微信与小程序设备联调。H2 与本地事务证据不等于这些运行环境通过。前端无改动，未重跑前端测试。

下一接收方：代码审查/人工验收，重点核对共享数据库配置、Dubbo 可达性、两端登录和刷新/撤销。无人工验收、生产就绪或线上发布结论。

## v2 增量交付：app/web 分包（2026-10-26）

输入为用户接口分包要求与设计 v2。8 个小程序 Controller 已分别位于 member.adapter.app.account、mall.adapter.app.store、trade.adapter.app.checkout/store；后台 Controller 留在 adapter.web。小程序专用请求/响应随入口迁移；两端共用请求/响应及共用转换类位于 adapter.shared，保持单份映射。MemberAccountExceptionHandler 位于 shared.account，包选择器同时覆盖 app.account 和 web.account。现有 HTTP 测试包及源码引用已同步，未修改认证算法、URL、权限、字段、数据库或前端。

PASS：清理旧产物后的相关模块及依赖测试，298 项，失败 0、错误 0、跳过 0。覆盖会员、商城、交易既有 HTTP 契约和数据库集成测试；源码包路径检查通过，8 个小程序 Controller 均位于 app，43 个 Controller 路由声明与移包前完全相同。git diff --check 通过。命令、测试明细和路径迁移清单见 evidence/app-web/。本次分包复用现有测试，无新增业务行为。

NOT RUN：线上启动、真实 MySQL/Nacos/Dubbo 与小程序设备联调。小程序/后台身份隔离仍由原服务端接口路径及独立会话验证决定，Java 分包不代替权限校验。无提交、部署或用户验收结论。

## v3 增量交付：认证 Controller 归入 auth（2026-10-26）

用户要求 AppMemberAuthController 迁到 auth 服务。Controller 现位于 `lemnt-auth-adapter/src/main/java/com/lemnt/auth/adapter/app/member/`，AuthLoginReq/AuthRefreshReq 和三个认证 Cmd 随迁，auth 新增 MemberAuthCmdExe、TokenPairDTO、请求/响应转换和同字段 TokenPairRes，直接调用本地 MemberAuthenticationService。member 删除原 Controller 和专用命令/请求转换/Executor 重载；短信/社交登录仍使用自身 TokenPairRes，不依赖 auth 的实现类。

MemberAuthRouteConfiguration 在网关注册 order=-10 的三个精确 Path 路由，指向 lb://lemnt-auth，保留 /member/app/auth/login、/refresh-token、/logout 原路径，不使用 StripPrefix；原 member 常规路由继续处理其他接口。路由文档已说明代码注册，不重复配置同名路由。auth 小程序异常处理限定在 app.member 包且优先于公共全局处理器，保留401/400/409及原 R 契约，后台认证接口未改。

PASS：清理旧编译产物后 auth/member/gateway 及其依赖测试，99 项、失败 0、错误 0、跳过 0；4 项新增 auth HTTP 测试覆盖登录字段、刷新、退出、输入校验和业务异常（含全局异常处理共存）；1 项新增网关路由测试覆盖3个精确路径、路径保留、目标服务、优先级和其余会员接口不被捕获。会员既有数据库回归通过。确认 member 源码和编译输出中已无 AppMemberAuthController，auth app 不引用 member 实现，git diff --check通过。证据见 evidence/auth-controller/。

验证中修正了网关测试对 Publisher 的处理和当前 PathRoutePredicateFactory/WebFluxProperties 依赖初始化，最终 clean test 通过。NOT RUN：真实 Nacos/Dubbo/服务发现与端到端转发，未修改线上配置、未部署或提交。上线仍需按 v1 配置 auth 的会员数据库并同步发布相关服务。HTTP URL 与前端调用保持不变。

## v5 最终交付：后台令牌组件复用、网关无 Dubbo、main 风格统一（2026-10-26）

输入为用户三个连续要求：生成代码风格对齐 main、gateway不能引入Dubbo、会员登录和token校验参考后台且不要另写算法。设计以03-development-design.md v5为准，未提交代码，未部署。

### 当前实现

- auth 的 AppMemberAuthController 保留在 adapter.app.member，密码登录/续期/退出 URL 保持原样；直接调用本地应用执行器。短信/社交身份核验及账户维护仍在 member，核验事务提交后由 auth 签发。
- MemberTokenGatewayImpl 复用后台 TokenService、JwtUtils 和 Redis 会话；密码编解码也使用后台 SecurityUtils。不再生成 mbr_a_/mbr_r_、不计算令牌摘要、不使用SQL member_session存储、不提供独立刷新令牌。手机号限流摘要仅用于原有登录失败计数，不是令牌算法。
- 公共 SecurityUser/JWT 增加 user_type，会员为 member，后台默认 admin；无该声明的历史后台JWT保持兼容。公共组件根据所需身份域同时校验签名类型和缓存对象类型，并使用独立会员缓存前缀。后台 AuthFilter 显式拒绝 member JWT；会员空角色、空权限，不复用后台角色上下文。
- gateway 删除 Dubbo 启动注解、配置、common-dubbo/auth-client 依赖和客户端，亦删除中途提出的远程HTTP校验。RedisMemberSessionLookup 直接复用公共 TokenService，在工作线程读取缓存；没有业务远程调用。最终网关jar检查无Dubbo库、auth-client、旧Dubbo/HTTP实现类残留。
- 登录/短信/微信响应统一为 MemberTokenRes(memberId,accessToken,expiresIn)，会话时长2592000秒。refresh-token 使用 Authorization 请求头携带当前访问JWT，同后台一样只续期会话，返回原JWT。小程序已同步删除refreshToken字段及旧不透明令牌正则，保留会员身份一致性和换号/过期请求保护。
- 退出撤销当前会话；改密、重置、换手机号、停用通过公共 TokenService 的会员会话索引撤销全部会员缓存。使用现有 RedisSetService，无全库KEYS扫描。索引TTL比会话长现有120分钟续期阈值，避免顺序写入使索引先于有效会话失效。账户安全变更、签发、显式续期与退出仍遵循会员行锁顺序。Redis与SQL不是统一事务，失败后可能提前撤销部分会话，未虚构跨资源原子提交。

### main 样式对齐

基准为本轮读取的 main 分支 system 模块 ConfigController、ConfigAddReq、ConfigAddCmdExe、ConfigMapper，实际SHA见 evidence/backend-token-main-style/static-validation.json。检查当前分支新增的698个Java源文件：展开通配符导入和注解全限定名、统一四空格与声明/record/分支排版；使用本地JDK语法树定位479个无大括号分支，补齐块结构；补充中文类型/方法说明，依赖字段按具体协作者命名；原单行POM展开为多行（21份）。已确认 app/web 分包与 auth 归属不回退。

格式整理使用本地JDK/已有工具，未安装额外格式化依赖。格式化阶段检查词法序列，初次样式处理后核对字符串字面量无差异；XML比较元素/属性/非空值保持一致。依赖字段重命名曾误及包路径，编译发现后已修正为作用域内引用，后续全量验证通过。最终检查：新增源文件无通配符导入、无包路径不匹配、无超过120字符的方法声明，git diff --check通过。未修改 main 分支现有系统模块来迁就生成风格。

### 验证证据

- PASS：根目录离线 Maven clean verify，492项测试，失败/错误/跳过均0。覆盖所有模块构建及现有文件上传本地HTTP测试。文件上传用例的临时端口绑定首次受运行权限限制，获允许后完整重跑通过。
- PASS：补充会员会话索引TTL保护后，相关模块及其依赖 verify 再次通过340项，失败/错误/跳过均0。
- PASS：原有TokenService测试保留，新增历史后台JWT兼容、会员签名/缓存隔离、会员撤销不影响后台和索引TTL断言。会员H2集成使用真实JWT/TokenService与受控Redis测试替身，覆盖密码/短信/社交身份核验、续期并发、退出与全部会话撤销；测试替身不冒充真实Redis运行证据。
- PASS：小程序69项测试、修改JS语法检查；格式整理使用已有本地Prettier处理两份修改的会员JS/CJS。
- PASS：实际网关jar的依赖和类清单检查，无Dubbo、auth-client或已撤回的远程校验实现；新旧两端JWT混用通过网关总入口测试拒绝，历史后台JWT仍可访问后台。
- 证据目录：evidence/backend-token-main-style/，含root-clean-verify.log、token-regression.log、miniapp-tests.log、tests.json和static-validation.json。

### 迁移与未运行项

必须同步更新auth/common-security/member/gateway/trade和小程序，auth与gateway保持同一JWT签名密钥。旧会员不透明令牌失效，需要重新登录；历史后台令牌和原后台缓存键保持兼容。JWT无固定exp，过期由Redis TTL控制；expiresIn为本次会话时长，不应解释成JWT固定到期时间。SQL旧member_session表保留历史DDL，无破坏性操作，但当前运行代码不再访问它。

NOT RUN：目标MySQL/真实Redis/Nacos/Dubbo跨进程、真实短信/微信、原生开发者工具/设备端到端。没有修改线上配置、部署、提交、合并或宣称人工验收/生产就绪。下一接收方为代码审查和环境联调，重点复核真实缓存序列化/TTL、签名配置、服务同步升级与会员安全变更后的会话撤销。

## v6 增量交付：复用 LOGIN_TOKEN_KEY，删除 MemberRequestSecurity（2026-10-26）

用户要求“调整为复用”并“MemberRequestSecurity也去掉”。TokenService统一使用LOGIN_TOKEN_KEY + userKey，删除MEMBER_LOGIN_TOKEN_KEY及按身份拼接不同缓存前缀的重载方法；新增/续期/读取/退出/按会员撤销全部走公共键。会员索引仍只记录该会员已签发的会话键，不索引后台会话。

读取会话仍同时校验签名身份与缓存对象user_type。后台按userKey获取上下文也拒绝会员缓存对象，避免共享前缀后混用；历史未标记user_type的后台会话保持兼容。退出与撤销的键来自已验证JWT/会员索引，测试确认会员撤销只删除索引内会员会话，不删除其他后台会话。

AuthFilter统一调用公共TokenService.getSecurityUser(token,expectedType)。已删除MemberRequestSecurity、MemberSessionLookup、RedisMemberSessionLookup及旧专用过滤器测试。公开接口/身份头清理/原始路径匹配改由AuthFilter总入口测试覆盖；会员不写后台身份头，后台从校验后的SecurityUser写可信头。网关没有重新引入Dubbo或远程HTTP校验。

PASS：相关模块及依赖clean verify，325项测试，失败0、错误0、跳过0；包含公共键下双向身份隔离、缓存身份类型不一致拒绝、后台按userKey拒绝会员、会员撤销范围、公开规则精确匹配、原始路径与伪造头清理、正常两端访问及会员数据库回归。实际网关jar确认不含已删类、Dubbo、auth-client残留，git diff --check通过。证据见evidence/shared-login-key-auth-filter/。

未执行线上配置、缓存迁移、部署或提交。若此前运行过旧独立前缀版本，其会员会话需重新登录；后台原缓存键不变。真实Redis/Nacos/设备端到端未运行，测试替身不冒充真实Redis证据。本次前端无改动，未重跑前端测试。

## v7 增量交付：移除会员会话索引（2026-10-26）

用户明确要求去掉 TokenService 的 redisSetService。已删除该构造依赖、索引写入/过期/移除、revokeMemberSessions和MEMBER_LOGIN_INDEX_KEY。TokenService恢复RedisStringService、SnowflakeUtils、JwtUtils三个依赖；公共LOGIN_TOKEN_KEY、user_type隔离、当前会话退出及续期保留。公共RedisSetService类未删除，其他业务不受影响。

会员领域Gateway及实现删除sessionsRevoke与TokenService注入，密码/资料/手机号应用服务不再触发全设备撤销。相关Swagger描述、接口文档与后台密码/编辑弹窗中的“所有登录会话失效”提示同步移除。没有保留空撤销方法或添加替代索引。改密/重置/换手机号后其他现有会话仍有效；停用继续由会员业务状态校验拒绝访问，恢复启用后未到期会话可能恢复使用。小程序原先清除当前设备本地会话的行为未修改。

PASS：相关后端及依赖clean verify，325项，失败0、错误0、跳过0。测试更新为当前会话单独退出、改密凭证更新但保留其他会话、短信重置及手机号更新保留会话、停用与恢复的状态校验；公共签名/缓存身份隔离仍通过。前端按lemnt-ui/AGENTS.md执行typecheck、lint、format:check与test，169项测试通过；页面只删除过期提示，无新增UI行为或测试。git diff --check通过。证据见evidence/no-member-session-index/。

首次前端检查因当前目录的PATH未包含Node而未能启动，使用已有本地Node22.23.3补齐运行路径后四项全部通过，未安装依赖。未运行前端构建（仅提示删除）、浏览器、真实Redis/Nacos或设备端到端。未清理线上旧索引、未部署、未提交；旧索引若曾产生可按已有TTL自然过期。
