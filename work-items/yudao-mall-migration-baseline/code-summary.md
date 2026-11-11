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
