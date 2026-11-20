# B1a / B1b代码与验证摘要

日期：2026-11-09。B1a及B1b已完成自动化验证；generate总体仍为in_progress，后续批次和人工门控未放开。以下先保留B1a记录，B1b文件清单与结果见后半部分。

## 代码文件

| 任务 | 文件 | 操作与关键变化 |
|---|---|---|
| F001 | `lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/filter/AuthFilter.java` | 修改：`/app`及其子路径按会员域处理，不受后台宽泛白名单影响；公开规则只精确允许商品列表、详情、分类、评价GET，保留其他会员/营销/交易规则和身份头清理 |
| F002 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterAppRoutesTest.java` | 修改：真实新地址、错误方法、近似前缀、非法ID、过期旧商品地址和伪造身份头 |
| F003 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterDomainTest.java` | 修改：真实JWT覆盖收藏/足迹会员域隔离，包括后台宽泛白名单和正常会员会话 |
| F004 | `lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/ProductAppRouteContractTest.java` | 新增：实际生产YAML路由谓词和AuthFilter联合验证；公开读归product、评价投稿归order，会员私有接口不能匿名进入，可信会话展示快照替换伪造头，方法/URI不改写 |
| F005 | `lemnt-miniapp/services/comments.js` | 修改：GET走`/app/comments`，POST仍走`/mall/app/comments`，不改投稿资格、身份、换号及结果未知语义 |
| F006 | `lemnt-miniapp/tests/comments.test.cjs` | 修改：独立URL/方法断言，以及未登录公开查询和投稿拒绝 |

任务和证据文件：新增`evidence/execution-baseline.json`、`evidence/b1a-validation.json`、本摘要；更新`tasks.md`、`evidence/tasks.json`、`state.json`。没有修改路由YAML、POM、SQL、消息框架或生产配置。

## 验证

修复前新增测试已复现：商品公开GET被拒绝、新会员地址使用错误身份域并可能被后台白名单绕过、小程序GET使用投稿地址。修复后对最终代码重跑：

| 检查 | 结果 |
|---|---|
| 网关完整Maven测试 | 365项，0失败/错误/跳过，退出码0 |
| 小程序评价定向测试 | 11项，0失败/跳过，退出码0 |
| 小程序全部Node测试 | 182项，0失败/跳过，退出码0 |
| 项目架构检查 | 0违规，退出码0 |
| 两个JS/CJS文件Prettier检查 | 通过，退出码0 |
| `git diff --check` | 通过 |

实际命令、前后结果、文件指纹及边界记录在[evidence/b1a-validation.json](evidence/b1a-validation.json)。

## 未验证与边界

- 固定google-java-format 1.25.2 JAR已核验SHA256；执行被自动权限策略拒绝，未绕过限制，不声明Java自动格式检查通过。
- 本轮验证为离线路由/鉴权和原生小程序服务契约测试，没有启动真实Nacos、Redis、MySQL、RocketMQ；未做微信开发者工具、真机或支付渠道验收。没有启动不能证明这些行为的浏览器预览。
- B1a完成时8个范围内任务已完成，其余64个pending、34个blocked；后续B1b结果见下节，整体迁移尚未完成。
- 无commit、push、索引修改或DDL操作。

## B1b文件清单

用户确认：“确认并执行”。原F007–F037加4个必要配套文件，共35个代码/测试/POM/脚本文件（16新增、19修改）；另更新本工作项状态、任务和证据。

| 任务 | 路径 | 操作 |
|---|---|---|
| F007 | `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/model/InboundEventFacts.java` | 新增 |
| F008 | `lemnt-server/lemnt-promotion/lemnt-promotion-domain/src/main/java/com/lemnt/promotion/domain/event/gateway/InboundEventGateway.java` | 新增 |
| F009 | `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/main/java/com/lemnt/promotion/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 |
| F010 | `lemnt-server/lemnt-promotion/lemnt-promotion-infrastructure/src/test/java/com/lemnt/promotion/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 |
| F011 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/AfterSaleCompletedEventHandler.java` | 修改 |
| F012 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderClosedEventHandler.java` | 修改 |
| F013 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderCompletedEventHandler.java` | 修改 |
| F014 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/OrderPaidEventHandler.java` | 修改 |
| F015 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/src/main/java/com/lemnt/promotion/application/event/service/StockPreReservedEventHandler.java` | 修改 |
| F016 | `lemnt-server/lemnt-promotion/lemnt-promotion-application/pom.xml` | 修改 |
| F017 | `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/model/InboundEventFacts.java` | 新增 |
| F018 | `lemnt-server/lemnt-order/lemnt-order-domain/src/main/java/com/lemnt/order/domain/event/gateway/InboundEventGateway.java` | 新增 |
| F019 | `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/main/java/com/lemnt/order/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 |
| F020 | `lemnt-server/lemnt-order/lemnt-order-infrastructure/src/test/java/com/lemnt/order/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 |
| F021 | `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PayRefundSuccessEventHandler.java` | 修改 |
| F022 | `lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/event/service/PaySuccessEventHandler.java` | 修改 |
| F023 | `lemnt-server/lemnt-order/lemnt-order-application/pom.xml` | 修改 |
| F024 | `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/model/InboundEventFacts.java` | 新增 |
| F025 | `lemnt-server/lemnt-pay/lemnt-pay-domain/src/main/java/com/lemnt/pay/domain/event/gateway/InboundEventGateway.java` | 新增 |
| F026 | `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/main/java/com/lemnt/pay/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 |
| F027 | `lemnt-server/lemnt-pay/lemnt-pay-infrastructure/src/test/java/com/lemnt/pay/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 |
| F028 | `lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/event/service/OrderClosedEventHandler.java` | 修改 |
| F029 | `lemnt-server/lemnt-pay/lemnt-pay-application/pom.xml` | 修改 |
| F030 | `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/model/InboundEventFacts.java` | 新增 |
| F031 | `lemnt-server/lemnt-member/lemnt-member-domain/src/main/java/com/lemnt/member/domain/event/gateway/InboundEventGateway.java` | 新增 |
| F032 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/main/java/com/lemnt/member/infrastructure/event/gateway/InboundEventGatewayImpl.java` | 新增 |
| F033 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/src/test/java/com/lemnt/member/infrastructure/event/InboundEventGatewayImplTest.java` | 新增 |
| F034 | `lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/event/service/OrderBenefitsEventHandler.java` | 修改 |
| F035 | `lemnt-server/lemnt-member/lemnt-member-application/pom.xml` | 修改 |
| F036 | `scripts/check_architecture.py` | 修改 |
| F037 | `scripts/test_architecture.py` | 修改 |
| B1b-T01 | `lemnt-server/lemnt-order/lemnt-order-application/src/test/java/com/lemnt/order/application/event/PaySuccessEventHandlerTest.java` | 修改 |
| B1b-T02 | `lemnt-server/lemnt-pay/lemnt-pay-start/src/test/java/com/lemnt/pay/start/event/CashPaymentPersistenceTest.java` | 修改 |
| B1b-T03 | `lemnt-server/lemnt-member/lemnt-member-start/src/test/java/com/lemnt/member/start/MemberAccountIntegrationTest.java` | 修改 |
| B1b-T04 | `lemnt-server/lemnt-member/lemnt-member-infrastructure/pom.xml` | 修改 |

### 关键变化

- 每个上下文通过本域record和Gateway消费外部事实，仅infra解析外部Client协议；保留金额、long积分、订单项、库存行和赠券数量，null集合不伪装为空。
- 基本类型缺失/null在协议边界拒绝，不能被解析器默认成0/false；原合法性、冻结事实、状态和数值规则仍留在原业务链。
- 9个Handler保留事务注解、信封requireType、原payload和inbox消费键；没有新增消息表、Topic或可靠性框架。
- 4个application POM移除共6处外部Client生产声明；本服务Client与合法test依赖保留，member infra仅补test依赖。
- 架构检查同时检查生产源码和POM，支持通配、静态、嵌套及全限定类型引用，区分本服务/common Client、infra与测试scope。新增负例先复现原漏检，再修复检查器。

### B1b验证与状态

| 检查 | 结果 |
|---|---|
| 四域定向映射测试 | 26项通过，全部真实Client事件字段参与映射 |
| 全服务Reactor `clean test` | 1338项，0失败/错误/跳过，退出码0 |
| 全部脚本自测 | 33项通过，其中架构门禁13项 |
| 生产源码与POM扫描 | 0违规，退出码0 |
| 小程序全量Node测试 | 182项通过，B1a成果未回退 |
| 差异空白、HEAD、索引、B1a文件指纹 | 检查通过 |

新增支付Handler版本/类型测试证明未知版本或错误类型在payload解析和消费登记之前拒绝；原事务回滚、重复、乱序、金额/渠道/会员和冻结业务事实回归均保留并通过全量测试。

执行基线：[evidence/b1b-execution-baseline.json](evidence/b1b-execution-baseline.json)。实际命令、退出码、232个测试类报告汇总及35文件指纹：[evidence/b1b-validation.json](evidence/b1b-validation.json)。

本批36个任务完成后，总计110个文件任务为44 completed、32 pending、34 blocked；6个人工门控状态不变，generate仍为in_progress。B2a及后续批次、真实基础设施/微信工具/渠道验收未执行；没有DDL、生产配置、消息表、commit、push或索引操作。固定Java格式工具此前执行被权限策略拒绝，本批未绕过或重试，不声明自动AOSP格式检查通过。

## 基线对齐及只读预检（2026-11-09）

本轮未修改业务代码。B1a/B1b共42条文件指纹复核一致，纠正文档当前态与历史设计快照的歧义；E001/E002文档任务完成后为46 completed、30 pending、34 blocked。6个人工门控不变，generate继续in_progress。

本轮实际复跑：后端1338项、小程序182项、管理端320项全部通过；管理端typecheck/build通过，架构/模块拆分门禁零违规，脚本33项通过。管理端构建仍有大于500kB chunk警告，非构建失败。原始日志与命令退出码见[evidence/alignment-20261109-113811/commands.json](evidence/alignment-20261109-113811/commands.json)，静态检查见[evidence/alignment-static-validation-20261109.json](evidence/alignment-static-validation-20261109.json)。本轮未重跑common组件测试或自动AOSP格式检查。

运行条件见[只读预检](local-environment-preflight.md)及[基线复核证据](evidence/alignment-baseline-20261109.json)：环境在两次探测间变化，Docker/MySQL/Redis/Nacos后续可见，但资源归属/隔离与操作授权未确认，RocketMQ和业务服务未就绪。本次没有连接业务数据、启动服务或执行DDL；真实运行、微信和渠道验收仍未完成。


## B2a商品查询与已有字段透出（2026-11-18）

用户指令：“按照缺的补齐”。本批完成F038–F063与V-B2a，新增应用回归节点B2a-T01。当前111个文件任务为74 completed、3 pending、34 blocked；整体generate仍in_progress，未完成全量迁移。

- 查询链：categoryId与categoryIds去重合并，一次批量展开启用直接子分类；price与既定salesCount升降序，false不被默认值覆盖；SQL固定分支排序、相同值用sort/id稳定排序，列表/count共用筛选。
- 分类链：现有公开GET /app/categories增加optional ids，不新增公开路由；只返回启用分类，sort/id升序；空ids与未传ids有不同含义。
- 详情链：已有简介、轮播、配送方式/模板和SKU重量/体积贯通DTO/Res/小程序。复用既有ProductSpuResult字段映射，补齐公开DTO/Res与页面透出；历史未知字段不补零或当前配置。
- 小程序：商城页加入价格与销量排序，切换重置页码；详情展示轮播、简介、配送方式和选中SKU配送参数。复用既有product页面整体承接对象逻辑；所有原有页面测试保留并追加回归。
- 计划调整：原F056所列start测试源码在当前HEAD不存在，改在infrastructure的StoreCatalogQueryTest使用真实Mapper XML和H2；F041的筛选透传、F061的页面对象承接已满足要求，验证复用而不做无效改动。新增StoreQueryTest覆盖应用分类展开/拒绝非法输入/原值投影。

验证：Java17全服务Reactor `mvn -o -f lemnt-server/pom.xml clean test`通过，224个测试类报告共1273项，零失败/错误/跳过；小程序187项通过。架构和模块拆分零违规、脚本33项通过。本批Java固定版本/校验和AOSP格式和显式导入、JS/CJS Prettier及修改页WXML缩进检查通过。首次受限沙箱内Mockito agent attachment失败，经正式执行审批在沙箱外重跑成功；没有删除测试或放宽断言。

本批维持SPU/SKU正价可见性、隐藏详情返回null、人民币元协议和支付确认累计销量口径；createTime与虚拟销量不在B2a伪造交付。源原有会员/营销/履约/退款分支尚需继续逐方法核对。

后续仍需B2b补充设计与贯通、B3逐事件消息机制收敛、B4内容/统计及其余能力补迁；真实MySQL业务写入/MQ/RPC/HTTP/页面/微信及支付退款渠道未执行。此次未执行DDL、发布Nacos、启动服务、中间件或提交代码。完整证据：[B2a验证](evidence/b2a-validation.json)。

## 2026-11-18 继续补齐迁移差异

用户要求直接补齐已识别的缺口。本轮把能确定语义的商品、文章和支付统计链接入现有代码，同时修正库存事务消息回查。全量能力与运行验收继续跟踪。

| 实施内容 | 行为与兼容处理 |
|---|---|
| 商品创建时间 | 使用 SPU 真实创建时间排序，两种方向下历史未知均置底。升级脚本先增加可空列，再设置新记录默认值 |
| 商品资料 | 管理端可设置规格模式、虚拟销量、SKU 划线价、成本价和条码。单规格沿用一个无规格值的 SKU。成本价只由后台输出 |
| 销量 | 保留原 SPU 支付确认累计。公开接口独立输出展示销量，虚拟配置未知时返回空。销量排序与源项目一致，按实际销量加虚拟销量排序，虚拟配置缺失时以实际销量作为排序值。SKU 累计从切换后发生的支付事实计算，不重算历史 |
| 零元订单 | 普通报价、营销计价、订单快照均接受零现金。仍先预占库存，再进入自动支付，保留逐行金额校验、优惠券消费、原请求号幂等 |
| 商品详情 | 不可见时返回业务码 404。公开访问累计浏览量，已验证的会员身份同时用于写入本人足迹 |
| 文章 | 促销服务承接文章与分类管理、分类筛选、热门和轮播推荐筛选、按 ID 或标题读已启用文章、浏览计数。管理端使用现有促销权限，小程序入口位于商城页 |
| 支付统计 | 在充值单页显示累计实付充值毛额，已退款充值仍计入历史成功充值，不包含赠送部分 |
| 会员统计 | 会员管理页显示每日新增、当前性别和省级地区分布。起止日均计入，注册趋势补齐无新增的日期，地区按系统字典解析，空档案与无法识别的编号计入未知 |
| 订单汇总 | 订单履约页依现有汇总权限读取订单数、应付金额、成功支付退款笔数和金额。既有接口支持精确订单编号；状态筛选仅作用于订单列表 |
| 库存预占回查 | 用业务状态区分取消先到和实际扣减后释放。新消息核对数量，旧 RELEASED 缺少依据时返回 UNKNOWN |
| 启动配置 | 商品补齐 `commerce-mq.yaml` 导入。促销、订单、支付明确沿用同一 Nacos 地址作为 discovery 地址，满足公共下线组件的路径要求 |

存量库升级文件位于 `lemnt-server/sql/upgrade`，文章菜单脚本位于 `lemnt-server/sql/V20261118__mall_article_menu.sql`。这些脚本已进入工作区，实际部署和数据库执行另有验收记录。库存状态语义变更需要商品服务的写入方和回查 listener 同步切换，避免旧实例把新取消标记当成预占成功。历史 RELEASED 需要原业务证据才能追认。

全量后端回归通过 1294 项；随后会员请求绑定、权限映射、数据库边界的 5 项定向复跑通过。管理端 321 项、类型检查、Lint 和构建通过，小程序 188 项通过。真实 MySQL 8.0.46 的一次隔离复跑通过商品升级、文章表和菜单重复执行、会员 Mapper 统计。首次执行发现设置创建时间动态默认值的 ALTER 语法在实际 MySQL 被拒绝，改用保留可空性的 MODIFY 并验证旧行仍为空、新行取得默认值。见[隔离库执行证据](evidence/mysql-gap-upgrade-20261118.json)。

商品、促销和会员都用实际启动包连接隔离 MySQL 和临时 Redis，使用回环 HTTP 验证。商品通过零价展示、详情浏览入库和下架详情业务码 404；促销通过热门列表、中文文章详情和浏览入库；会员通过统计接口拒绝匿名访问。会员授权后的统计数据由 MockMvc 和 MySQL 分别验证。直连过程关闭 Nacos 注册及下线钩子，外部 Dubbo 引用用本机占位地址，关闭 MQ 并排除公共 MQ 生产者装配。早期一次启动尝试中的会员下线钩子曾对未注册的临时端口 0 发出摘除调用，最终复跑已排除该组件。见[服务直连证据](evidence/service-gap-http-20261118.json)。

本阶段原剩余差异包括商品单品赠积分、独立佣金、SPU 价格聚合、支付与订单 Outbox/各消费域 Inbox、会员访问/端来源/地区交易、商品和交易时序统计，以及其余能力族的逐方法对照。历史 PV/UV 需要相应的访问事实，历史注册终端及按日商品事实也需要原始记录。完整运行验收仍需要带网关、管理登录、RocketMQ 与微信渠道的业务流验证。

## 2026-11-18 商品赠积分与 SKU 聚合

- 复核源项目以选中订单行领取一次 SPU 配置的赠积分，同一行买多件仍赠一次。商城商品管理支持配置和读取，留空保留历史值，填 0 关闭。促销将每个 SKU 行的商品赠积分独立冻结，普通、活动报价都走这一规则。
- 结算报价绑定各行赠积分配置。订单保存商品赠积分来源及明细，配送重算和人工调价只调整按实付赠送的部分。退款按原订单行的积分和件数扣回，最后一批退余数。按真实原订单数据重放退款，商品配置变化不影响旧订单。
- 管理端按页聚合未删除的 SKU，展示最低售价与可用库存。接口同时返回最低划线价和成本价，任一 SKU 对应历史金额缺失时保留空值。商品详情可查看各 SKU 的划线价、成本价和条码。
- MySQL 8.0.46 隔离库实测升级、非负约束和聚合口径，验证历史未配置保持空值、删除 SKU 排除在统计外。见[数据库验证证据](evidence/product-gift-mysql-20261118.json)。
- 商品、促销、订单及其 Reactor 依赖通过 562 项测试，含 107 项结算集成测试。随后自提定向 4 项复跑通过，覆盖同一行的商品赠积分和实付赠积分只计一次。退款回归的事件重放使用测试 Broker。管理端 321 项、类型检查、Lint 与构建通过。实际支付、退款渠道及 RocketMQ 待验收。

## 2026-11-18 订单行独立佣金

- 商品后台支持 SPU 固定分佣开关及 SKU 一级、二级每件金额，单位人民币元。开启后缺失金额按零处理，显式零不会转用全局比例。旧商品未配置字段保持空值。固定金额仅由管理接口和建单所用的商品内部接口读取。
- 首次建单通过一次商品批量读取冻结配置，包含固定分佣的订单在 `order_brokerage_line` 保存全部行。支付确认后，固定行按件数发放，混合订单比例行按各行实付金额和当时的比例计算。每级实发金额与订单级佣金流水及余额同事务写入。零现金订单可发已配置的固定佣金。
- 部分退款按原订单行扣回，固定行按累计退件数，比例行按该行累计实退款。重试只补扣回差额。已部分扣回的流水按剩余佣金解冻，整单退完扣除剩余金额。旧订单和纯全局比例订单继续按原整单金额比例处理。管理端和小程序均可读取负数扣回流水。
- 商品和订单两份升级脚本在隔离 MySQL 8.0.46 实测。核对旧字段保留空值、旧订单无回填、独立零金额、唯一键及外键。部署先执行对应库升级并部署商品端，再部署订单端，保证建单可读取新 Dubbo 方法。见[数据库验证证据](evidence/order-commission-mysql-20261118.json)。
- 相关 Reactor 回归 564 项通过。随后佣金与结算定向 7 项通过，验证重放支付、部分退款、部分退款后解冻、整单结清和零现金固定佣金。管理端 321 项、类型检查、Lint 和构建通过，小程序 188 项通过。

剩余范围包括推广端商品佣金预估、订单与支付消息表收敛、未覆盖的时序和访问统计，及网关、真实 Dubbo、RocketMQ、微信支付退款渠道验收。

## 2026-11-19 推广商品佣金预估、支付确认与到账趋势

- 小程序在商品详情提供一级单件佣金预估。订单分销域核对会员资格，商品域仅返回当前上架且未删除的 SKU 的计算基础。固定分佣与全局比例分别计算，同一个商品的固定零佣金规格纳入区间。部署时先发布支持新 Dubbo 查询的商品服务，再发布订单服务及小程序。
- 新订单在渠道支付准备时冻结应付金额，核验资金后在订单内记录支付单号。确认支付、订单日志和下游事件保持同一事务。退款仍核对支付库资金事实，并通过售后终态处理重复通知。支付准备和确认兼容旧 Inbox，支付与退款的正常确认停止写入新的订单 Inbox。
- 开启订单恢复调度后，分页回查已冻结金额的待支付订单。仅在支付服务有一笔成功资金且金额一致时补记，不恢复已经关闭的订单。上线前需由发布流程执行订单升级脚本，再部署新读写方。历史字段保留空值，原表继续服务存量事件和剩余消费链。见[订单与商品隔离 MySQL 验证](evidence/preview-payment-facts-mysql-20261118.json)。
- 管理端支付单提供到账趋势，复用支付及充值列表权限。按原到账日返回成功支付笔数、订单支付毛额、充值实付毛额。赠送金额排除，后续退款的充值保留原到账日实付金额。无到账时间的历史交易排除，查询范围最长 366 天。钱包余额未接入订单支付，历史订单退款成功时间缺失的记录未纳入此趋势。见[到账趋势隔离 MySQL 验证](evidence/payment-trend-mysql-20261119.json)。
- 网关、订单、支付、促销及其依赖的 Reactor 通过 998 项测试。管理端 323 项及类型检查、Lint、构建通过，小程序 190 项通过。架构与拆分检查均为零违规。隔离 MySQL 8.0.46 分别核对订单字段升级、佣金查询和到账趋势 SQL。

本轮证据覆盖应用及持久化回归、网关路由与权限契约和隔离库 SQL。真实网关登录、Dubbo、RocketMQ、微信支付退款渠道仍需联合运行验收。支付和订单出站 Outbox、其他消费端 Inbox 以及其余访问和时序指标继续跟踪。

## 2026-11-19 交易待办

- 管理端订单页新增交易待办，按当前状态展示待发货、待自提、售后待审核和提现待审核。待发货和待自提复用当前履约的已支付语义，历史未填配送方式沿用履约列表的快递默认。售后排除已逻辑删除记录，提现只统计待审核状态。计数与列表的订单号筛选独立。
- 接口复用履约、售后和分销三项列表权限，网关路由验证 GET、HEAD、OPTIONS。订单实际 Mapper 通过持久化回归，覆盖空库、待支付、发货和核销后状态及审核数据。
- 网关、订单、支付、促销及其依赖的 Reactor 共 1000 项测试通过，启动包构建成功。管理端 324 项及类型检查、Lint、构建通过。架构与拆分检查零违规。
- 启动包验收发现结算和会员订单的两个 AppOrderController 使用同一个默认 Bean 名。为结算 Controller 指定独立 Bean 名，原 URL 与权限契约保持一致。组件扫描和订单持久化定向复跑共 21 项通过。
- 联合运行范围为本地网关、认证、系统、订单和支付，使用专用 MySQL 和临时 Redis。最后复跑在 Nacos 127.0.0.1:8848 拒绝连接时中止，尚未验证管理员经网关读取业务结果。见[联合运行尝试](evidence/commerce-admin-runtime-attempt-20261119.json)。

## 2026-11-19 商品销量确认与消费回执收敛

- 商品支付消费按原预占号和 SKU 校验购买数量。销量入账标记、库存确认、SKU 销量和 SPU 汇总在商品库同一事务提交。直接确认库存的 RPC 继续只确认库存，销售事实由支付消费写入。重复支付按预占行的销量事实识别，历史事件仍按原 Inbox 载荷核验，历史预占列保留空值。
- 商品售后消费等待该 SKU 的销量入账或原历史支付凭证，再按原售后回补规则执行。任一商品行处理失败时整笔支付消费回滚，原事件可以重试。
- 营销的库存预占和订单完成消费复核原订单资源状态，关单消费复用原资源围栏与释放终态。会员发货复核原订单状态，关单复用积分终态及退回流水。上述消费路径停止写自身消息回执，营销资源围栏与已有的业务结果继续参与恢复。
- 商品发布先在目标商品库执行 `migrate_product_reservation_sales.sql`，然后协调切换所有旧商品支付消费者。旧消费者只认识历史 Inbox 的成功记录，新旧商品支付消费者交叉处理同一事件会重复累计销量。新版本已消费的事件应继续由识别销量标记的版本处理重放。
- 商品、营销、会员、订单及其依赖的 Reactor 回归 713 项通过。实际 Mapper 回归覆盖并发投递、同单号重新投递、第二行失败后的整单回滚、售后先到后重试、历史回执和直接库存确认。架构与模块拆分检查零违规。
- 本机 Docker 套接字不存在，Nacos 8848 端口拒绝连接。本轮的商品库升级脚本仍待 MySQL 实测，RocketMQ 与跨服务运行验收继续跟踪。

支付关单的原回执仍是支付建单的终态围栏。商品售后、会员支付与售后、营销支付与售后以及订单和支付出站 Outbox 继续纳入 B3。

## 2026-11-19 支付与商品关单消费

- 支付建单仍与支付关单共用同一订单的本库数据库锁。持锁后向订单服务重查待支付状态、会员及冻结应付金额，以拦截较早取得待支付快照的建单请求。关单只处理仍待支付的尝试，渠道关闭失败时可重试，已成功的资金状态保持可对账。
- 新支付关单以订单终态和支付尝试状态判断重放，继续兼容旧关单回执。支付建单和消费需协调切换，先结束仍依赖旧关单成功回执的在途建单请求，再启用新版关单消费。
- 商品关单通过既有订单快照 RPC 核验已关闭状态、归属和完整 SKU 预占数量。已释放的预占无重复归还，尚未预占写业务取消标记。历史关单回执按原载荷校验。
- 支付、商品、订单、营销及依赖回归 625 项通过，其中结算集成 108 项。支付持久化定向复跑 14 项通过，覆盖锁内重查拦截延迟建单、渠道关闭失败重试和旧载荷不一致。商品集成覆盖关单和优惠券补偿失败后的消息重放。架构与拆分检查零违规。
- 本机 Docker 套接字仍不可用，Nacos 8848 端口拒绝连接；真实 RocketMQ、Dubbo 和支付渠道的运行验收继续跟踪。
- 营销退款的成功查询仍依赖售后回执。其余消费、支付本地订单锁及订单和支付出站 Outbox 继续纳入 B3。

## 2026-11-19 按芋道业务动作简化交易链路

- 芋道 `TradeProductSkuOrderHandler` 建单时同步扣减库存，整单取消和售后按订单行恢复。目标服务改为在商品库独立短事务中执行扣减和登记预占，按原预占号及 SKU 处理重试。订单补偿继续处理跨服务调用失败。
- 删除会员发货、营销订单完成和营销库存预占的纯校验消费，并清理对应的生产调用、消息 DTO、适配器、处理器和库存事务消息回查。发货、收货、自提核销直接保存订单状态、运单及原操作日志。芋道的微信物流同步是独立业务动作，按实际渠道能力继续对照。
- 正式消息从 8 类收敛到 5 类，保留支付确认、退款确认、订单支付、关单及售后的实际跨服务效果。历史发货和完成消息记录留供核对，新版订单中继仅领取有消费者的事件。
- 删除无接口调用的商品库存调整 DTO、响应转换及商品基础设施的出站 MQ 依赖。删除的预占事务消息专项测试由真实库存 Mapper 回归承接，覆盖重复预占、提前取消、写台账异常及调用方回滚。
- 公共事件契约编译通过，网关及交易相关服务干净构建通过 1151 项测试，含 108 项结算集成测试。架构和拆分检查零违规，已删除的类无编译产物残留。本机 Docker 套接字与 Nacos 仍未就绪，真实 Broker 和渠道验收继续跟踪。

## 2026-11-20 支付与退款通知

- 参照芋道的支付通知时序，在支付成功和现金退款成功的资金事务中登记 `pay_notify_task`，提交后异步派发，到期重试，订单确认提交后才标记任务成功。通知沿用本项目内部 Dubbo，订单依据原资金记录、订单状态和冻结退款方案核验。零现金退款在订单本地完成。
- 移除支付服务 Outbox 的生产、存储、中继和空库建表。存量待投递记录使用 [支付库迁移脚本](../../lemnt-server/sql/upgrade/migrate_pay_outbox_to_notify_task.sql) 登记通知任务，保留历史记录。已投递的支付及退款消息由订单兼容消费者处理。订单支付、关单、售后完成仍使用订单服务的 Outbox 派发库存、会员和营销业务动作。
- 支付、订单及其依赖的 Reactor 通过 541 项测试，其中结算集成 108 项、支付库持久化 12 项。覆盖资金回滚无通知、订单写入失败后重试和重放幂等。架构与模块拆分检查零违规。Java 仅格式化变更区域，全库格式检查仍报告既有差异。
- 使用 H2 的 MySQL 模式核对迁移的两条写入语句，覆盖已有任务重置、退款任务新增、已发送消息排除和原记录保留。Docker 和 Nacos 当前不可用，实际 MySQL 升级、跨进程 Dubbo 与 Broker 切流待验收。

## 2026-11-20 微信公钥模式与渠道查单

- 商户 API 私钥用于请求签名，微信支付公钥用于 API 应答和支付、退款回调验签。`publicKey` 和 `publicKeyId` 写入原微信渠道 JSON。APIv3 密钥用于回调解密。
- 支付与退款定时查原单，回调和补查共用资金结果事务及提交后通知。退款查无记录时保留待确认状态。退款回调核对原应用、支付单号、渠道及金额，资金事务提交后返回 HTTP 204。
- 网关、支付、订单及依赖共 933 项测试通过，架构与模块拆分检查零违规。本地用独立的商户和渠道 RSA 测试密钥验签解密支付、退款报文，覆盖错误公钥 ID。支付库回归覆盖补查与回调重放、错误应用拒绝和未受理原退款不重提。真实商户、网关与 Nacos 联调待验收。

## 2026-11-20 钱包余额支付

- 启用商城应用的 `WALLET` 渠道后，小程序使用原支付入口扣款，支付服务使用原订单的会员和金额。扣款、支付事实与通知任务同事务提交。钱包原路退款在退款资金事务中返还原钱包。
- 订单与支付使用同一 Redis 锁协调本地扣款和各种待支付关单入口。外部渠道尝试未关闭时，钱包扣款拒绝创建并行支付。商城后台配置与正式收银台提供微信和钱包余额。
- 网关、订单、支付及依赖通过 935 项。定向回归覆盖余额不足回滚、重复支付与退款、渠道互斥和支付与关单并发。小程序通过 191 项，管理端支付模块通过 22 项，TypeScript 检查通过。真实 Redis 和 Nacos 联调待验收。

## 2026-11-20 钱包微信充值与原路退款

- 小程序为本人套餐充值单发起微信 JSAPI 支付。渠道成功回调和主动查单通过原单金额完成一次入账，新微信充值单走渠道确认。原人工确认接口兼容历史记录。
- 微信充值退款冻结实付与赠送余额，原路退款金额使用实付快照。退款成功扣回冻结余额，失败释放冻结；回调、查单和后台重试使用同一退款编号。充值实付与商城支付分别统计。
- 网关、订单、支付及依赖通过 938 项，小程序通过 192 项，管理端支付模块通过 22 项，TypeScript 检查通过。覆盖原单和金额校验、重复入账、原路退款、失败解冻及统计避免重复计入。真实微信商户及 Nacos 联调待验收。

## 2026-11-20 自定义充值与手续费快照

- 小程序支持按套餐或自定义金额发起微信充值。自定义金额精确到分，实付即到账金额。相同请求编号重放核对会员与原输入，并发命中既有记录时同样核验原输入。
- 第一次确认支付成功时保存原应用渠道费率与按费率计算的手续费，按元四舍五入到分。钱包支付记录零费率。历史与费率未知的交易留空，后台支付详情可查看快照。
- 网关、订单、支付及依赖通过 941 项，小程序通过 193 项，管理端支付模块通过 22 项，TypeScript 检查通过。覆盖快照精度、配置变更后重放、历史字段兼容与自定义金额幂等。H2 用等价语句核对字段升级及历史数据保留，存量 MySQL 和真实微信商户待验收。

## 2026-11-20 微信支付尝试到期

- 商城与充值新建微信支付尝试使用支付库时钟登记 30 分钟有效期，JSAPI 和 Native 均传原 `time_expire`。预支付与本地渠道关单使用同一支付尝试行锁。查询异常或渠道未确认关闭时保留原尝试。
- 定时任务到期先查询资金，渠道已收款时走原支付成功流程。确认关闭后，原业务单仍待支付时可生成新尝试，退款绑定实际收款的支付单。历史尝试保留原编号和状态，新有效期字段留空。
- 网关、订单、支付及依赖通过 943 项，架构与模块拆分检查零违规。定向回归覆盖查单超时、关单确认、已收款补记、再次预支付和充值退款关联。H2 使用等价语句验证历史记录与新尝试共存，实际 MySQL 升级与微信商户联调待验收。

## 2026-11-20 独立支付主单

- 商城订单和钱包充值按原业务单号各关联一个支付主单。开单、确认资金、关闭业务在支付库以主单行锁协调。渠道尝试过期仅关闭该尝试，后续重试沿用主单号。管理端支付详情展示主单号与状态。
- 主单识别历史金额、归属及多笔真收款冲突，保留原尝试与资金事实。待对账时停止自动订单确认、充值入账及新发起充值退款。已关业务的真实晚到资金仍保留。
- 迁移原脚本在 H2 MySQL 模式验证，九笔原尝试完整保留，金额冲突、会员冲突和多笔收款分别标记待对账。Docker 和 Nacos 仍不可用，真实 MySQL 升级和微信商户联调待验收。
- JDK 17 下支付、订单、网关及依赖模块共 944 项测试通过。管理端支付模块 22 项测试和 TypeScript 类型检查通过。架构与模块拆分检查零违规。

## 2026-11-20 支付应用结果通知

- 参照芋道为支付和退款保存创建时的应用通知地址。支付成功、退款成功和渠道明确失败分别进入现有通知任务，资金事务提交后投递。应用 HTTP 任务可独立重试，商城原有业务确认仍走内部 RPC。
- 管理端通知任务页支持筛选和重发应用通知。回调接收端通过 Nacos 配置授权 Origin，接收端以 HTTP 2xx 和 `{"code":200}` 确认处理。
- 本机 MySQL 8.0.46 的独立验收库 `lemnt_pay_notify_accept_20261120_1703` 执行通过，已有支付与退款记录完整保留，两类新记录均能保存回调地址。默认账号回读 Nacos 的 `dev/DEFAULT_GROUP/lemnt-pay.yaml` 成功，当前配置尚无接收端授权项。真实商户和全服务联合验收待完成。
- 支付、订单、网关及依赖的回归覆盖 946 项，包含真实回环 HTTP 投递。全量运行遇到支付配置测试的重复 Bean，修正测试依赖后定向重跑 34 项全部通过。管理端支付测试 22 项、TypeScript 类型检查、架构和模块拆分检查均通过。
