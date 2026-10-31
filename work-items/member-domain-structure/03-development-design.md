# member 结构分析与开发设计

需求ID member-domain-structure；版本v1；2026-10-26；责任Skill code-software-engineer。
输入：用户本轮指令、现有工作区、member-auth-migration v7及当前system模块。参考工程Skill的references/development-design.md与java.md，沿用现有Maven/Java17/Spring/MyBatis/Dubbo，不新增框架或微服务。已有未提交改动保留，不提交、部署或执行DDL。

## 已确认问题

1. account横跨会员资料、地址、资产、等级、分类和签到，MemberAccountGateway/Service/Mapper与MemberAccountCmdExe/QueryExe混合多种职责，无法从包或执行器识别业务。
2. root Member与account Profile、MemberDO与ProfileDO重复映射member_user；旧注册入口在Controller内定义Req/Res并组装对象，缺少system式边界转换。
3. MemberAuthService仍混合本地密码维护和已迁往auth的登录/刷新/退出；这些转调除测试外已无业务消费者。短信/微信删除后内部核验签发协议也将无消费者。
4. MemberDTOConverter/ReqConverter/ResConverter跨域，AccountPageDTO与已有PageResult重复；后台跨资源分页需区分应用层查询入口和领域边界。
5. 参数缩写、机械注释、异常处理选择器与包边界、单行语句、HTTP模型Swagger/Serializable和POM外部SDK依赖需要按system逐项核对。

## 业务域与依赖

- user：会员档案、注册、密码维护、分组与标签及关联；canonical Member覆盖完整会员快照，淘汰Profile重复模型。通过SecurityUtils读取网关传递的会员身份，不解析或校验token，也不签发令牌。
- address：收货地址和默认地址规则，写入时锁定user会员以保持归属/地址数量/默认值并发约束。
- growth：积分/经验流水、等级和等级记录、签到及奖励、积分交易配置。签到奖励与经验升级共用原事务和会员行锁，放在同一成长权益域避免服务间互调循环。
- shared：放跨域的编号/文本校验、分页HTTP边界、通用查询筛选和后台跨资源查询入口；查询入口分别依赖各域只读网关，不承载混合业务写入。

application/address和growth可依赖user的领域Gateway获取会员/锁/数据库时间，领域Gateway本身不互相继承，不使用一个大接口拼装各域。identity因用户要求删除短信/微信，不建立空身份域。

## 实施任务

1. 删除短信、微信/社交绑定、验证码、二维码、JSAPI接口及其Cmd/Qry/DTO/模型/Gateway/SDK配置、依赖和测试；小程序只保留密码登录、资料、修改密码、地址和成长权益入口。删除网关对应公开规则，避免残留入口。历史数据库脚本不执行破坏性清理。
2. 删除member的无业务消费者认证转调和核验后签发模型；本地密码与只读会话职责分离。同步清理auth内部无消费者签发RPC，保留正常密码登录；member删除只读会话RPC调用，trade仍使用的独立协议不在本轮机械删除。
3. 按user/address/growth同步移动各层模型和实现；拆分Account Gateway/Mapper/Service，合并Member/Profile及持久化重复；公共分页输出复用PageResult。写命令与查询执行器按system单一类型组织，HTTP字段和URL保持（已要求删除的短信/微信接口除外）。
4. 旧/members注册/详情虽为兼容入口，仍在已有接口文档中，本次不直接删除URL；提取独立Req/Res/Converter并补充公开契约说明。MemberClient被trade使用，保留现有RPC协议名，不机械改名造成额外发布耦合。
5. adapter顶层只保留app/web，下一层为user/address/growth；请求、响应、转换器按入口归属；异常复用项目GlobalExceptionHandler和ServiceException，不建立adapter.shared，也不互相依赖另一个入口。后台分组/标签分页归web/user，成长记录归web/growth；修正注释、导入、命名、注解和依赖。

## 验证与回滚

迁移前记录Controller路径/方法与所有SQL语句。允许删除清单仅限短信/微信接口和明确失去消费者的内部实现；其余URL、权限、字段、SQL表和事务语义不因结构整理改变。运行现有H2/MyBatis真实SQL、回滚/幂等/并发、HTTP身份与校验、密码、网关令牌校验和身份头传递、trade消费者编译、网关公开规则和小程序测试。需要时补充能暴露问题的回归，不能只测试目录形状。

不执行MySQL/Redis/Nacos/微信/短信或设备联调，不声称生产验收。旧短信/社交表作为历史迁移记录保留但当前代码不再访问，不删线上数据；移除SDK后需同步服务发布，不提供保留旧身份入口的假实现。外部未证实消费者的兼容HTTP/RPC协议记录为保留项。

## 用户补充约束：命名、接口模型与分页

member 各层类型和变量不带 Admin，原 /member/admin 路径、权限和持久化业务类型 ADMIN/ADMIN_LEVEL 保持，避免改变路由与幂等键。每个带业务对象入参/返回项的接口使用独立 Req/Res；仅 R、标量结果等基础契约复用。签到、积分、经验记录分页入参分别定义，去除原小程序通用分页里无用的 mobile。创建/更新、全局记录分页及按会员查询等其他复用对象一并拆分。

分页按照 system 使用已有 R.page(rows,total)，删除自定义 AccountPageRes 和泛化分页响应转换器。应用层继续复用 PageResult；Controller 完成独立 Res 转换后直接 R.page。分页 HTTP 由原 data.records/data.total 改为顶层 rows/total，data=null，后台和小程序解码同步调整，页面内部状态 records/total 保持。本项是用户明确要求的契约调整，替代前文分页包装保持不变的初始约束。

## application/domain/infrastructure/client 规范续修

1. 拆除 MemberQueryExe 和 shared 的通用分页/详情 Qry、MemberFilter：user 的分组/标签，growth 的资产/等级/签到记录各自使用独立 Qry、execute 执行器及只含真实存储筛选条件的领域 Filter。SQL、HTTP路径、请求与返回字段及事务保持。
2. 对齐 system 的单数聚合 + List/Page/Detail 命名，统一 application Cmd/Qry/DTO 序列化、DTO与领域/DO转换、接口实现@Override和中文注释；不把没有审计列的表强行套入BaseDO。
3. client 保留现有 MemberUserClient/getByMemberId 和 DTO 字段，不变更 RPC 协议；补齐契约、序列化和独立转换。移除 domain 对 client 的无用依赖，由 application 直接依赖 client，保证领域不耦合远程契约。
4. 用会员真实MyBatis/H2筛选、详情类型隔离、边界和事务测试，RPC消费者trade编译与测试验证；追加源码依赖/命名检查和接口/SQL快照核对。修改后不提交。


## 按表存储边界与按业务 Controller 拆分

- Gateway、GatewayImpl、Mapper 按 12 张当前使用的会员表一一对应。同表读写归并，跨表编排继续保留在 application service/executor；会员余额、等级、分组字段写入 member_user 对应组件，凭证写入 member_credential 对应组件。
- 删除混合的 MemberGrowthGateway/Mapper、MemberUserQueryGateway/Mapper、MemberGrowthQueryGateway/Mapper；各表分页和普通列表 Mapper 使用不同方法名，避免 MyBatis 按方法名注册语句时发生重载冲突。
- adapter 顶层保留 app/web，下面按 user/address/growth 放置接口。小程序成长接口按等级、签到配置、签到、积分流水、经验流水拆分；会员档案与密码分开。后台会员资料与密码、等级分配、分组分配、标签关联、地址、积分、经验及各类记录分开，配置与全局流水也按业务拆开。
- 以本轮开始时工作区的 67 个 HTTP 操作为基准，保留路径、HTTP 方法、权限、独立 Req/Res、统一 R.page；保留 MemberUserClient 的 RPC 契约。本轮不修订原有路径前缀。
- 用全部 Controller 同时注册的 Spring MVC 测试检测重复映射，并比对拆分前的路由/权限快照；用真实 MyBatis/H2 与事务测试验证存储拆分。SQL 注解逐条快照比对，SQL 内容不变。
