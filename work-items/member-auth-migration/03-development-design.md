# 会员认证迁移开发设计

当前有效方案为文末 v7。v1-v6 为历史设计记录；双令牌、SQL会话、网关远程校验、会员独立登录缓存前缀及专用会员请求过滤器均不再代表当前实现；会员全设备会话索引也已按 v7 删除。

需求 ID：member-auth-migration；版本 v1；日期 2026-10-26；责任 Skill：code-software-engineer。
基线 f3449d3；输入为用户“只迁移认证”、现有 MemberAuthService、会员 V2/V3 SQL、后端 AGENTS.md。
参考：code-software-engineer/references/development-design.md 与 references/java.md（插件 0.13.2）。

## 边界与取舍

Java 17 / Maven，多模块 Spring Boot + MyBatis + Dubbo（版本以 lemnt-dependencies/pom.xml 为准）。
认证中心负责密码登录校验、失败计数、令牌签发、刷新轮换、退出、访问令牌校验，以及对应 SQL 与事务。
会员中心保留密码设置/修改、资料、身份核验和绑定、短信及第三方提供商集成。现有 HTTP 路径和字段不变，会员 HTTP 入口通过领域 Gateway 调用 auth-client，不依赖 auth 的实现模块。网关和交易直接调用 auth 的会话接口。
本次不改变角色菜单授权，也不迁移数据库表。为保留会员锁、刷新轮换、修改密码/手机号/禁用撤销会话的原子性，auth 的认证数据库连接必须指向现有会员数据库；不得指向系统库。auth infrastructure 独立维护认证所需 Mapper，不依赖 member 实现。会员仅保留账户更新所需的凭证和会话撤销 SQL。跨库拆分不在本次范围。
短信/第三方核验和注册先在会员本地事务提交，然后调用 auth 签发；不得持有会员行锁调用远程 auth。签发失败时身份核验已消费，客户端需重新核验；不会返回虚假登录成功。签发再次锁定并检查会员状态，避免禁用后签发。

## 契约、安全与迁移

保持 mbr_a_/mbr_r_ 随机令牌、SHA-256 哈希存储、2小时访问/30天刷新、5次/15分钟密码失败限制。刷新一次性轮换，禁用和账户安全修改撤销会话。内部签发 RPC 仅供已完成核验的会员服务使用，不新增公开按会员 ID 签发接口。Dubbo 依现有内网可信服务边界；不得对公网暴露。
新增 auth-client 独立 RPC 契约；MemberSessionClient 旧契约移除，所有仓内调用切换。无 HTTP 请求、响应、错误语义和前端变更。上线需同步发布 auth/member/gateway/trade，配置 auth 的会员库数据源（MEMBER_AUTH_JDBC_URL、MEMBER_AUTH_DB_USERNAME、MEMBER_AUTH_DB_PASSWORD，dynamic primary=member 且 strict=true；Nacos 不得覆盖为其他库）与已有 RPC 注册中心；不执行线上配置或 DDL。回滚须同步回滚服务版本，数据库表和存量令牌可沿用。

## 任务与验收

1. 新增 auth-client、auth domain 认证 Gateway/模型、application 认证执行和 Dubbo 实现、infrastructure 认证 Mapper/Gateway。依赖：现有认证规则；验收：生产 SQL 下登录/限流/刷新并发/撤销测试。
2. member 增加远程认证 Gateway，将 MemberAuthService 变为调用编排，保留本地密码修改；身份核验拆分事务后签发。依赖 1；验收：短信/社交注册可见性、重复核验拒绝和原有账户集成测试。
3. gateway/trade 改用 auth 会话契约，删除 member 会话提供者和不再使用的认证存储入口。依赖 1/2；验收：依赖编译、HTTP 契约和网关失败拒绝测试。
4. 更新启动配置说明与文档、运行相关 Maven 测试。生产 MySQL/Nacos/Dubbo 实测另列 NOT RUN，H2 与本地代理验证不冒充跨进程验收。

风险：共用会员数据库是本次保留现有事务语义的过渡边界；数据库权限与服务同步发布需运维配置。无部署、提交或人工验收结论。

## v2 增量：小程序与后台接口分包（2026-10-26）

输入：用户“小程序的接口放在app 包下 后台放在web下”。沿用本文件 v1 及工程 Skill 既读模板，仅变更 Adapter Java 包结构，不改变认证归属、URL、权限码、字段或业务实现。

- 任务 P1：会员 account 的 App*Controller 迁往 adapter.app.account；商城整个小程序 store 迁往 adapter.app.store；交易原 adapter.web.app 迁往 adapter.app.checkout，AppPromotionController 迁往 adapter.app.store。后台 Controller 留在 adapter.web。
- 任务 P2：按两端实际 Controller 类型引用确定请求/响应专用性；专用模型放 app/web，共用模型与已有共用转换器放 adapter.shared，保留单份映射实现，不复制字段或业务规则。会员共用异常处理放 shared.account，显式覆盖 app.account 与 web.account。
- 任务 P3：同步包声明、源码/测试引用，保留既有 HTTP 契约测试；清理旧编译产物后运行 member/mall/trade 相关模块及依赖测试，检查移包前后 Controller 路径映射完全相同、所有 App Controller 不再位于 web。
- 范围边界：仅移动实际存在的小程序接口及直接适配依赖。后台认证继续位于 auth.adapter.web。业务应用层、Dubbo 接口、数据库和前端不因分包修改。

验收：包路径与源码目录一致；后台 Controller 均留 web；小程序 Controller 均在 app；HTTP 路径/方法不变，错误处理保持覆盖；原有 HTTP 测试通过。不新造与实现镜像的业务测试。

## v3 增量：认证 HTTP 入口归入 auth（2026-10-26）

输入：用户“AppMemberAuthController 挪到auth服务”。仅迁移该 Controller 的密码登录、刷新、退出三个入口，短信/社交身份核验入口继续在 member。

1. 将 Controller 及 AuthLoginReq/AuthRefreshReq、三个认证 Cmd 迁至 auth 的 app/member 与 application/member；新增对应 Executor 和边界 Converter，直接调用 auth 本地认证服务。TokenPairRes 在 member 仍被短信/社交登录使用，auth 保留独立同字段响应，服务实现层互不依赖。
2. 删除 member 中仅用于这三个 HTTP 入口的 Cmd、ReqConverter 方法及 Executor 重载；保留会员密码维护和身份核验。
3. 新增 auth 小程序认证异常处理，保持原401/400/409和 R 响应语义；网关通过独立高优先级路由把三个原 URL 转至 lb://lemnt-auth，不使用 StripPrefix，也不捕获短信/社交入口。同步路由示例。Java路由不依赖外部配置中心手工增加这三条映射。
4. 验证 auth HTTP 成功/校验/错误契约，验证路由仅选择三个精确路径并保留原路径，清理旧产物后运行 auth/member/gateway 测试。真实注册中心服务发现另列未运行。

契约：URL /member/app/auth/login、/refresh-token、/logout 与请求/响应字段不变；auth 数据库配置沿用 v1。后台 AuthController 仍在 adapter.web，会员入口在 adapter.app。

## v4 增量：网关不依赖 Dubbo（2026-10-26）

用户约束：gateway 不能引入 Dubbo。移除网关 auth-client/common-dubbo、EnableDubbo、Dubbo 配置与客户端；MemberSessionLookup 改为 Mono<Long>，使用负载均衡 WebClient 经服务发现直接访问 auth 的 GET /internal/member-sessions/current。该内部接口由 auth 本地执行访问令牌校验，200 返回会员编号，401 表示无效会话；不接受客户端会员编号，不开放按 ID 签发。内部端点不注册额外网关路由。

网关只携带原会员 Bearer 令牌，查询结果为空按401拒绝；HTTP错误/网络异常/4秒超时按503拒绝。移除阻塞 RPC 工作线程切换，不增加网关缓存或后台令牌回退。member/trade 的 RPC 保留。验收包括HTTP客户端请求/响应测试、auth内部端点测试、原身份隔离/故障拒绝测试以及最终网关依赖检查。

同时继续用户提出的 main 风格统一工作：范围为当前功能分支新增后端源文件、相关 POM；参照 main 的 system 模块显式 import、普通注解引用、四空格缩进、多行声明/分支与中文说明。仅进行可核对的样式整理，不改变业务语义和已确认 app/web/auth 边界。下载工具未获允许，使用本地 JDK 和已有工具验证。

## v5 当前有效方案：复用后台公共令牌组件与 main 风格（2026-10-26）

本节取代 v1-v4 中的自定义双令牌、SQL会话和网关远程校验方案。当前用户要求优先：gateway 无 Dubbo；会员登录和校验参考后台，不另写令牌算法；新增后端代码与 main 同类代码格式一致。

### 认证与隔离

会员认证由 auth.adapter.app.member 提供，仍为原三个 URL。MemberAuthenticationService 经领域 MemberTokenGateway 调用已有 TokenService；签名/解析由已有 JwtUtils 完成，缓存和30天会话续期复用后台机制。仅增加签名 user_type=member 与缓存前缀隔离，未携带 user_type 的历史JWT继续按后台 admin 处理。默认 TokenService.getSecurityUser(token)只接受后台身份，会员调用显式指定 member；AuthFilter 后台分支拒绝 member 类型。会员不注入后台角色与权限。

网关 RedisMemberSessionLookup 直接复用 TokenService 读会员缓存，阻塞 Redis 访问在工作线程执行；无 Dubbo、auth-client、HTTP远程校验或自建验签逻辑。服务端接口路径继续决定所需身份域，客户端传的身份和角色头仍被清理。trade/member 的已有内部 RPC 不在“网关禁用 Dubbo”的范围内。

### 会话生命周期与契约

登录/短信登录/微信登录响应改为 MemberTokenRes：memberId、accessToken、expiresIn（2592000秒）；删除 refreshToken。POST /member/app/auth/refresh-token 通过 Authorization: Bearer accessToken 续期，返回同一JWT及会话时长，不再生成第二种令牌或一次性轮换。注销删除当前会话。旧 mbr_a_/mbr_r_ 令牌不再接受，需要重新登录。小程序同步移除双令牌格式校验和请求体字段，仅保存当前访问JWT。

会员改密、换手机号、停用需要撤销全部会话：公共 TokenService 使用 Redis Set 记录会员已签发会话键；维护索引与会话TTL，按会员索引删除会员缓存，避免 Redis KEYS 和误删后台缓存。账户更新、显式续期、退出和签发继续使用会员数据库行锁序列化；Redis故障使账户事务失败，不宣称 SQL/Redis 为统一事务，部分已撤销会话在回滚后也不会恢复。Gateway 复用后台 TokenService 的错误语义（缓存读取异常或缺失拒绝身份）。SQL member_session 旧表保留历史结构但当前代码不再读写，无破坏性DDL。

### 风格整理及验证

main 的 system Controller/Req/Executor/Mapper 为样式基准。当前分支新增后端源文件限定范围内：显式导入、注解与类型使用普通引用、四空格缩进、声明/分支展开、按语法树定位补充分支大括号、中文类型与方法说明；依赖字段采用具体协作者命名。新增 POM 展开成多行。API、包划分和业务规则不因样式整理改变。

使用本地 JDK 语法树定位结构，字符串/注释独立分词；格式化过程核对可执行词法序列，XML前后比较元素/属性/非空值；回归覆盖行为，样式工具下载未获允许且未安装新依赖。验收包括公共令牌组件历史后台兼容、JWT类型与缓存双重隔离、会员撤销范围、网关无Dubbo产物、全根Maven clean verify、小程序测试、源文件包路径与路由保持检查。真实MySQL/Redis/Nacos/Dubbo/小程序设备端到端另列未运行。

## v6 当前有效增量：统一 LOGIN_TOKEN_KEY 和 AuthFilter（2026-10-26）

用户要求复用 LOGIN_TOKEN_KEY，并删除 MemberRequestSecurity。会员与后台缓存键统一为 LOGIN_TOKEN_KEY + 公共组件生成的唯一 userKey，删除 MEMBER_LOGIN_TOKEN_KEY 与按身份分支的拼键方法。JWT 和缓存对象 user_type 仍需同时匹配；后台按 userKey 获取上下文时也拒绝会员缓存对象。会员会话索引继续按会员维护，只负责定位应撤销的会话键。

AuthFilter 直接调用公共 TokenService.getSecurityUser(token,expectedType)，统一完成两端身份校验；删除 MemberRequestSecurity、MemberSessionLookup、RedisMemberSessionLookup。保留进入网关时的原始路径识别、原小程序精确公开接口、身份请求头清理；会员不写后台身份头，后台继续写入已校验的会话信息。Redis/JWT无效沿用公共组件返回未认证的语义。不存在网关Dubbo或HTTP远程会话校验。

验证：公共键下两端签名/缓存类型不匹配拒绝、后台按userKey读取时拒绝会员、会员索引撤销不误删其他后台会话、AuthFilter总入口的公开规则/头清理/路径边界与正常两端访问；清理网关旧产物确认删类不残留。v5的独立缓存前缀描述由本节替代；测试证据按本次增量保存。

## v7 当前有效增量：删除会员会话索引（2026-10-26）

用户明确要求去掉 TokenService 中的 redisSetService。删除 RedisSetService 注入、会员索引维护和撤销全部会话方法、MEMBER_LOGIN_INDEX_KEY；TokenService恢复RedisStringService/SnowflakeUtils/JwtUtils三个依赖。登录/校验/续期/当前会话注销继续复用LOGIN_TOKEN_KEY，身份类型校验不变。

会员Gateway删除sessionsRevoke接口及实现，账户资料/密码/手机号变更不再调用全部会话撤销，避免残留空实现或隐式Redis依赖。当前退出只删当前userKey；改密、重置和换手机号不删除其他会话；停用仍保留业务层会员状态校验，启用恢复后未到期会话可能再次有效。后台页面删除“所有会话失效”的过期提示，不添加实现细节提示。接口文档和测试同步，不承诺全设备下线。

本次不删除公共RedisSetService（其他业务可继续使用），不清理线上缓存，也不引入替代索引、版本号或其他全部下线机制。验证公共组件三依赖构造、当前会话注销、缓存续期、身份隔离、修改密码/手机号保持现有会话及停用状态校验；相关后端clean verify和前端规定检查。旧索引如果曾生成可按原TTL自然过期，未执行线上操作。
