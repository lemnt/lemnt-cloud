# 四模块拆分设计（已确认，执行中）

未使用模版文件：项目不存在 `templates/code_design_template.md`，采用 cs-design 的完整 15 章结构。

本文件是供确认的设计草案，不代表代码、数据库或运行环境已经迁移。需求来源为本目录 `migration-plan.md`；代码基线为 `a8753a11`。用户已确认：**按新模块统一重命名表；采用 ORDER_PAID、AFTER_SALE_COMPLETED 驱动权益**。未提交的其他工作项保持原状，不 commit、不 push，不调整 system 项目及其 SQL。

## 1. 工作流

按原计划 P1 product → P2 pay → P3 promotion → P4 order → P5 清理执行。每个阶段内部依次处理数据与领域端口、Client 契约、Gateway 实现、应用编排、HTTP/MQ Adapter、启动配置和测试。

1. 固化真实表、端点、源文件与跨域依赖清单，确认本设计及逐文件预览。
2. 新建四个六层模块，分阶段复制并适配；原 mall、trade 保留至 P5。阶段之间只依赖 Client，不依赖其他服务的 Domain/Application/Infrastructure。
3. 逐上下文验证独立数据库、契约及回归；不把“编译通过”当成真实支付或部署验收。
4. 全部调用方与网关切换方案准备完成后，再清理旧模块源码、POM 和初始化 SQL。清理不执行现有数据库 DDL。
5. 真实数据库迁移、Nacos 发布、MQ Topic 创建、部署切流另按执行证据记录。未执行项不能标记为通过。

P1～P4 是代码分步迁移，不是生产环境分批切流。旧应用继续作为唯一生效入口；新旧写入方、调度器和消费者不能同时处理同一业务。隔离测试不以连接旧生产库作为“兼容”办法。

## 2. 需求背景

类型：涉及数据库、RPC、MQ 的架构重构。

| 维度 | AS-IS | TO-BE |
|---|---|---|
| 服务 | mall、trade 两个业务服务 | product、promotion、order、pay 四个服务 |
| 数据 | 两库 64 张真实表 | 四库分别拥有 10、22、21、11 张原业务表，另有可靠消息表 |
| 支付后处理 | 支付任务直接调用订单、库存、权益、分佣 | pay 发布支付事实，order 校验并发布订单事实，各权益服务本地处理 |
| 售后 | 支付退款编排直接锁定支付、订单、售后 | order 管售后冻结和数量，pay 管渠道退款及支付资金限额 |
| HTTP | `/mall/**`、`/trade/**` 两条粗路由 | 保留 313 个现有方法与路径，按端点归属路由 |

量化目标：四个独立启动模块；全部 64 张表各有唯一归属和唯一目标名；313 个 HTTP 端点的路径、方法、权限和 JSON 契约保持；旧 mall/trade 生产包及构建依赖在 P5 清零；现有有效测试全部保留或等价迁移，新增消息重试和隔离库验证。

## 3. 需求范围

- S1：按 `file-manifest.md` 迁移原有六层代码、Mapper XML、Redis Lua、测试及构建资源。
- S2：按 `table-mapping.md` 迁移并重命名全部真实表，订单优惠快照、活动订单扩展、结算编排归 order。
- S3：补齐商品、优惠计算/资源锁、购物车、支付、订单快照契约，移除跨上下文实现依赖。
- S4：采用本地 Outbox + 消费端 Inbox，增加已确认的两个订单事件，保留原六个 Topic。
- S5：member 消费订单冻结权益；仅增加本库 Inbox 及相关适配，不重构会员业务。
- S6：网关、启动配置、SQL 工具、架构规则、当前有效说明同步更新。

本次不做：不改前端页面或公共 HTTP 协议（目标是入口兼容）；不扩展营销功能（避免重构夹带业务变更）；不调整 system 项目/SQL/菜单与 `sys_config` 键（用户明确排除）；不自动向现有库执行 DDL、搬数据或发布配置（涉及真实切流，需具体环境和执行范围）；不清理无关公共 MQ 实现及历史工作项（不影响本次迁移验收）。

## 4. 需求分析

### 4.1 已核实的代码约束

以下路径均相对项目根目录，行号对应基线，不能仅依据原计划的示意表和数量搬迁：

| 来源 | 发现与处理 |
|---|---|
| `lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/inventory/gateway/InventoryGatewayImpl.java:L1` | 这是订单侧商品 RPC 消费者，端口与实现留 order；库存存储和 provider 进 product。修正原计划“整个 inventory 子域进 product”。 |
| `lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/PaymentRefundService.java:L26` | 当前同一编排更新订单、售后、支付，不能原样复制进 pay；拆为 order 售后协调与 pay 渠道退款服务。 |
| `lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyDispatcher.java:L17` | 当前直接依赖订单、退款权益、分佣；pay 迁移后只发布支付事实，禁止继续注入这些应用服务。 |
| `lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/settlement/service/SettlementPreparationService.java:L46` | 结算直接依赖活动、券、购物车和商品，迁入 order 后分别由本地端口/RPC 隔离。 |
| `lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/dubbo/SettlementLifecycleClientImpl.java:L28` | 混合库存、券、积分、活动生命周期；拆为 order 编排与各服务事件处理，不用一个改名 RPC 继续跨域写。 |
| `lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/PromotionOrderPreparationService.java:L25` | 活动订单映射、结算与请求幂等属于 order；活动规则、名额和拼团/砍价记录仍归 promotion。 |
| `lemnt-server/lemnt-mall/lemnt-mall-adapter/src/main/java/com/lemnt/mall/adapter/app/promotion/AppPointController.java:L32` | 查询与下单混在同一 Controller；查询进 promotion，下单拆至 order，保持原端点。 |
| `lemnt-server/lemnt-mall/lemnt-mall-infrastructure/src/main/java/com/lemnt/mall/infrastructure/settlement/gateway/SettlementGatewayImpl.java:L24` | 结算上下文当前在 Redis，原 SQL 没有 settlement_record/order/item 表；本次不凭示意新增三张表。 |
| `lemnt-common/lemnt-common-mq/src/main/java/com/lemnt/common/mq/consumer/BaseConsumer.java:L56` | 消费异常被捕获不再抛出；新可靠消费者不能直接沿用该失败语义，也不批量修改旧广告消费者。 |

代码扫描为 1,768 个生产 Java 文件、128 个测试文件、64 张表、313 个映射端点；完整来源见 `evidence/`。跨域 import 清单是改写入口，不代表全部为违规：Client、上下文内 ACL 副本需区分，字符串全限定类型等还需编译与检查器验证。

### 4.2 目标业务流程

```text
会员 -> order结算 -> product商品快照 / promotion活动优惠及购物车 / member归属校验
     -> order保存订单、明细、优惠与活动快照
     -> promotion同步冻结券/活动资源，product同步预占库存（请求可重放）
     -> order待支付
     -> pay预支付、验签、支付成功本地提交 + PAY_SUCCESS outbox
     -> order校验原支付、金额、会员、订单状态 + ORDER_PAID outbox
     -> product确认库存 / promotion核销券与活动 / member冻结规则发放积分

order创建售后：锁订单 -> 检查唯一处理中售后 -> 按行计算并冻结金额、数量、权益
     -> 审核/收货 -> order持久化退款请求 -> pay按afterSaleNo幂等原路退款
     -> PAY_REFUND_SUCCESS -> order校验冻结请求、CAS成功数量/金额/售后 + AFTER_SALE_COMPLETED
     -> product回补库存 / promotion处理券和活动 / member按冻结权益冲正
```

零现金订单由 order 本地确认 PAID 并发布 ORDER_PAID；零现金售后由 order 本地完成并发布 AFTER_SALE_COMPLETED。两者均不伪造支付流水，不发起金额为 0 的渠道请求，不虚构 PAY_SUCCESS/PAY_REFUND_SUCCESS。

### 4.3 必须保持的业务规则

1. 同订单通过订单行锁和处理中售后查询控制最多一笔处理中的售后；不新增“退款中数量”。明细只累计成功已退数量，成功状态与数量更新 CAS 保证一次。
2. 退款金额在**生成售后**时冻结。普通份额为 `floor(原明细实付/原购买数量, 2) × 本次数量`；最后剩余数量使用 `原明细实付 - 历次成功现金退款`。审核、渠道重试和 MQ 不重新计算。
3. 示例：10 元/6 件，退 2、3、1 件分别 3.32、4.98、1.70 元；0.01 元/3 件分别 0、0、0.01 元。整单 REFUNDED 同时满足现金与全部商品数量守恒。
4. 积分/赠送权益按冻结方案及原记录冲正，整数运算不溢出；保留历史 NULL 未知边界，缺失快照阻止新售后并要求对账，不按当前配置补造历史。
5. 商品、订单明细、售后继续使用 specs/specId/specName/specValueId/specValueName；既有旧 JSON 别名读取能力保留。
6. pay 不读取 order 表，order 不读取 pay 表。order 校验原订单、售后与冻结退款计划；pay 锁原支付并验证支付归属、成功状态、唯一售后号及待处理+成功退款金额限额，失败/取消记录按既有规则释放额度。
7. 同步 RPC 超时属于结果未知：使用原 requestNo/orderNo/afterSaleNo 查询或重试，不能换编号创建第二笔。RPC 不放在跨服务共享事务中；本地状态先持久化，再调用/补偿。
8. 付款与关单竞争按订单 CAS 处理；关闭后收到成功支付事实不可重新开单、发权益，保留异常支付任务告警并进入对账处理，不能静默丢弃资金事实。
9. 库存确认 marker 不能随 ORDER_PAID 发布直接置为成功。order 通过 ProductCatalogClient 查询原预占确认结果后更新本地 marker；确认未完成继续阻止发货及退款。查询失败可重试，禁止“未知即成功”。

| 当前状态 | 事件/条件 | 下一状态/效果 |
|---|---|---|
| PENDING_PAYMENT | 有效现金支付事实或合法零元确认 | PAID；同事务产生一次 ORDER_PAID |
| PENDING_PAYMENT | 合法超时/取消 | CLOSED；同事务 ORDER_CLOSED |
| PAID/SHIPPED/COMPLETED | 同一成功支付重投 | 状态不倒退、不重复发权益 |
| WAIT_REFUND | order已冻结退款请求；pay接受非零退款 | REFUNDING；未知结果保留原请求查询 |
| WAIT_REFUND | 冻结现金=0且可退款 | REFUNDED；同事务更新数量并产生售后事件 |
| REFUNDING | 匹配原冻结计划的成功支付退款事实 | REFUNDED；同事务 CAS 数量/金额并产生售后事件 |
| REFUNDED | 任意成功重投 | 不重复增加数量、现金、库存、积分 |

### 4.4 风险与取舍

- 跨库事务原子性消失 → 可能出现渠道已成功、订单未确认 → outbox/inbox、持久化请求、对账与原编号重试，禁止分布式事务伪装本地原子性。
- 消息重复/跨 Topic 乱序 → 重复或提前发权益 → 消费端双重幂等、冻结载荷校验和前置事件检查，缺前置抛错重试。
- 统一改表名 → XML、测试、外键和工具可能遗漏 → 真实 64 表映射、逐文件检索、隔离数据库测试。采用用户选择的统一新名，代价高于保留旧名。
- 同一 Controller 混合归属 → 粗路由会把部分接口送错服务 → 按方法和路径建立路由契约测试。
- 历史任务仍有未完成记录 → 删除旧执行器会失去补偿 → 切流前盘点并排空；不能排空则审计迁移为新任务，无法核实的阻止切流，不删除记录。
- 缓存/接口类名改变 → 已提交结算或旧 RPC 失败 → 保留已提交 Redis key、原编号及兼容 JSON 读取；新报价使用新版本；协调发布 Client/Provider，旧协议不能悄悄丢弃冻结字段。
- 新旧消费者/调度器同时工作 → 重复资金及库存副作用 → 切流清单要求单写入方与消费者组启停顺序。

## 5. 整体架构设计

| 模块 | 职责 | 允许的业务依赖 |
|---|---|---|
| product | 分类/品牌/SPU/SKU/spec、评价互动、库存预占/确认/回补 | member-client；order-client 的只读快照/验证契约 |
| promotion | 活动及参与记录、券、购物车、DIY/客服、资源锁 | product-client、member-client；订单事件契约 |
| order | 结算、订单/明细、售后、履约、物流、自提、分佣、优惠及活动快照 | product/promotion/pay/member/system 的 Client |
| pay | 支付应用/渠道、支付与原路退款、钱包、充值与内部转账 | member-client；只读订单快照契约及订单事件 |
| member | 原有会员/积分/账户能力，增加冻结权益事件入口 | order-client 事件契约 |

所有模块保留 client/domain/application/infrastructure/adapter/start 六层；HTTP Req/Res 与 Converter、应用 Cmd/Qry/DTO、纯 Java Domain、持久化 DO/Mapper/XML 的项目约定不变。不采用 skill 中与本项目六层结构不符的通用 CRUD 示例。注解使用显式 import 与短名 `@Schema`，复杂规则中文注释。

`CheckoutLifecycleClient` 不等价于 `PromotionLockClient`：同步资源冻结由 promotion 负责；订单合法状态、事件产生由 order 负责；库存和积分只在各自上下文处理。迁移中兼容桥只转发到明确的本地编排/持久化事件，不能继续在一个类里直接更新多库。

评价存在双向业务 RPC：product 的公开评价入口调用 order 验证订单，order 再调用 product 的 `saveVerified`；后者禁止反查 order，避免递归及订单锁内回调。两个独立 Client 不依赖实现模块，不形成 Maven 环。

## 6. API 设计

### 6.1 HTTP 兼容契约

本次无新增公开业务 API，不修改现有接口入参字段、响应字段、错误码、权限、登录规则。完整 313 条方法/路径、原 Controller 路径/行号及目标服务在 `evidence/http-endpoints.json`；具体参数类型/必填/校验、请求示例与响应结构以同一源 Controller 引用的 Req/Res 为基线，迁移后用契约测试逐一核对，不能重写为通用 CRUD 包装。

特殊路由：`/mall/app/point-mall/orders`、`/orders/batch` 及其他活动的下单端点进入 order；活动查询仍进 promotion。`/trade/app/orders/{orderNo}/payment` 的 GET/POST 进入 pay；其他同前缀订单请求进入 order。新路由保留 `/mall`、`/trade` 前缀，不 StripPrefix。现有 member/auth/system 路由及优先级不调整。

### 6.2 RPC 契约增量

| Client | 操作与必要输入 | 结果/规则 |
|---|---|---|
| ProductCatalogClient | 原商品批查/库存预占、确认、释放；增加按 reservationNo+skuId 查询原预占状态 | 保留商品快照完整 specs、价格及原 ID；查询不能默认确认 |
| ProductCommentClient | 原 saveVerified/saveSystem | 已验证入口不反查订单，保持原幂等 |
| PromotionCalcClient | calculate(memberId, requestNo, 商品行快照、活动/券选择) | 含原价、优惠、分摊、活动及券原记录 ID、ruleVersion 的不可变报价 |
| PromotionLockClient | lock/confirm/rollback(orderNo, quoteId, 固定资源ID及数量) | 同订单资源内容必须一致；同编号不同内容报冲突，不覆盖原冻结 |
| CartClient | 按会员批查选中行，按 orderNo+原购物车行及版本 consume | 不越权；不删除用户后续改动的新版本 |
| PaymentClient | prepay(orderNo, memberId, requestNo)；refund(afterSaleNo, paymentNo, orderNo, memberId, 固定amount, ruleVersion) | 非零请求；pay反查可信快照校验；按业务编号幂等 |
| PaymentQueryClient | 按订单/支付/售后编号查询可信结果 | 仅资金事实，不返回完整会员/订单敏感信息 |
| WalletClient | 原钱包、充值与内部转账所需能力 | 继续有序锁钱包与原 requestNo 账本幂等 |
| OrderSnapshotClient | 可信内部订单/退款请求查询 | 只读已保存的原单和冻结请求，不按当前配置重新计算 |

Bo/DTO 只放 Client 模块；新增纯数据对象优先 record，实现 Serializable。金额保持 BigDecimal 和明确的元单位，积分/ID 用整数，不转浮点。未实现字段不可用默认值伪造。

RPC 冲突沿用现有业务异常/响应映射；超时不能映射成业务失败。HTTP 仍由原异常处理器返回原错误码及信封，不凭设计示例新增 500/409 等新协议。

## 7. 数据库设计

### 7.1 业务表拆分及命名

逐表的原库、原表、目标库、新表、SQL 来源行和跨库外键处理见 `table-mapping.md`，机器清单见 `evidence/table-ownership.json`。所有 64 张原业务表一对一迁移，无合并、无丢失；只按本设计修改表名、同库约束名及跨库物理外键。

核心命名：`trade_order → order_info`，`trade_order_item → order_item`；三张优惠快照为 `order_promotion`、`order_item_promotion`、`order_promotion_refund`。原 `mall_promotion_order/item/refund` 为另一组活动订单扩展，改名 `order_activity/item/refund`；不与优惠快照合并。商品使用 product_、营销使用 promotion_、支付使用 pay_ 前缀。

拆除的三条跨库外键：售后→支付、支付退款→售后、支付退款→订单。字段、业务唯一索引保留，并由可信 RPC 与原业务编号验证；其余同库外键保留。现有已迁入 sys_config 的配置不新建 `trade_config`。原计划中的 `product_inventory`、`settlement_record` 等示意表不作为真实存在依据。

每服务一个 SQL：`lemnt-server/lemnt-{product|promotion|order|pay}/sql/lemnt_{product|promotion|order|pay}.sql`，只含建表与必要基础数据。无业务演示数据，基础菜单/字典/系统配置留在原 system SQL。member 只在既有单一初始化文件增加消费幂等表。

### 7.2 可靠消息表

product、pay、order 各建本库 `{context}_message_outbox`、`{context}_message_inbox`；promotion 只消费本设计事件，因此仅建 `promotion_message_inbox`，不生成无用途的 Outbox 和 Relay。member 只新增 `member_message_inbox`。共新增 8 张可靠消息表。以下为模板，生成时必须将 `{context}` 替换为真实前缀，不能直接执行模板。

```sql
CREATE TABLE {context}_message_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    event_no VARCHAR(64) NOT NULL COMMENT '稳定事件编号，重投不变',
    event_type VARCHAR(40) NOT NULL COMMENT '事件类型',
    business_key VARCHAR(128) NOT NULL COMMENT '本事件的业务幂等键',
    aggregate_no VARCHAR(64) NOT NULL COMMENT '所属订单或支付业务编号',
    schema_version INT NOT NULL COMMENT '事件载荷版本',
    payload LONGTEXT NOT NULL COMMENT '冻结事件JSON，不保存凭据或完整个人资料',
    payload_hash CHAR(64) NOT NULL COMMENT '规范化载荷SHA256，识别同键不同内容',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENDING/SENT/FAILED',
    attempts INT NOT NULL DEFAULT 0 COMMENT '已尝试次数',
    execute_version BIGINT NOT NULL DEFAULT 0 COMMENT '执行版本，隔离过期工作线程',
    next_retry_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次允许领取时间',
    lease_until DATETIME DEFAULT NULL COMMENT '数据库时钟计算的执行租约',
    last_error VARCHAR(500) DEFAULT NULL COMMENT '脱敏错误摘要',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_no (event_no),
    UNIQUE KEY uk_event_business (event_type, business_key),
    KEY idx_event_retry (status, next_retry_at, lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地事务消息待投递记录';

CREATE TABLE {context}_message_inbox (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    consumer_key VARCHAR(80) NOT NULL COMMENT '稳定消费处理器标识',
    event_no VARCHAR(64) NOT NULL COMMENT '已处理事件编号',
    event_type VARCHAR(40) NOT NULL COMMENT '事件类型',
    business_key VARCHAR(128) NOT NULL COMMENT '本处理器业务幂等键',
    payload_hash CHAR(64) NOT NULL COMMENT '载荷SHA256，同键不同内容拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费成功时间，与业务同事务',
    PRIMARY KEY (id),
    UNIQUE KEY uk_consumer_event (consumer_key, event_no),
    UNIQUE KEY uk_consumer_business (consumer_key, event_type, business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地事务消息消费幂等记录';
```

财务幂等记录不设置会自动失效的 TTL。outbox/inbox 的归档必须继续保留原编号去重能力；本次不实现删除策略。SQL XML 的 FOR UPDATE 显式 `useCache=false`、`flushCache=true`，不使用 Java 注解 SQL。

### 7.3 存量数据与切流

交付只包含初始化 SQL 和只读核对/切流说明，不把升级 ALTER/RENAME 混进初始化文件。真实迁移需停写或经确认的增量同步方案，按映射保留所有 ID、编号、金额、NULL、日志和索引，先父后子装载。逐表行数、关键唯一键、资金/积分/数量合计及外键孤儿数核对通过才可切流。

旧 PayNotifyTask 含 PAY、REFUND、AFTER_SALE，不能整表复制后假定 pay 能执行全部任务。切流前先排空；无法排空时逐条审核业务快照并映射到目标 outbox，日志和原幂等键可追溯，未知记录阻止切流。钱包和支付写入必须单写。出现新库写入后不能简单把网关切回旧库；应停写、对账及经审核反向同步后恢复。

## 8. 枚举与常量

| 项目 | 值 | 说明 |
|---|---|---|
| Topic | PAY_SUCCESS / PAY_REFUND_SUCCESS | 仅 pay 可发布的现金资金事实 |
| Topic | ORDER_PAID / AFTER_SALE_COMPLETED | order 校验/冻结后的订单与售后事实，含零现金 |
| Topic | ORDER_CLOSED / ORDER_SHIPPED / ORDER_COMPLETED | 原订单事件 |
| Topic | STOCK_PRE_RESERVED | product 库存预占事实 |
| OutboxStatus | PENDING/SENDING/SENT/FAILED | 保留失败记录，人工重试仍用原事件号 |
| schemaVersion | 1 | 新协议起始版本；未知版本失败，不降级丢字段 |

现有订单、售后、支付及预占状态编码、`trade.config` 等 sys_config 键、权限码不因模块或表名改变。

## 9. 缓存设计

| 缓存 | Key | 类型/TTL | 策略 |
|---|---|---|---|
| 结算预览 | 原 `mall:settlement:v5:{id}` 只读兼容；新 `order:settlement:v1:{id}` | JSON；原过期时刻、15分钟报价有效期 | 不延长原报价；新键写入，旧报价按原字段读取并验证 |
| 已提交结算/请求/购物车指针 | 原 `mall:{settlement}:v2:` 各子键 | JSON/String；原30天恢复保留期 | 原提交原编号重试，保持Lua多键hash tag和CAS；不能只等待15分钟就删除恢复数据 |
| 其他商品/活动缓存 | 逐文件迁移原策略 | 原类型与TTL | 不批量清Redis；仅按实际归属变更写入方 |

JSON 是否携带类名由现有序列化实现核实，兼容测试必须使用旧 JSON 样例，不假设包名改动必定安全。旧键读取失败保留明确错误，不用当前价格重建已提交订单。

## 10. MQ设计

| Topic | 生产者 | 消费者及本地效果 |
|---|---|---|
| PAY_SUCCESS | pay | order：核对现金支付事实，更新订单并产生 ORDER_PAID |
| PAY_REFUND_SUCCESS | pay | order：核对原售后冻结请求，完成数量与金额并产生 AFTER_SALE_COMPLETED |
| ORDER_PAID | order | product确认库存；promotion核券/活动；member按冻结规则记积分与成长 |
| AFTER_SALE_COMPLETED | order | product按售后行回补；promotion处理原券/活动；member按冻结数量冲正 |
| ORDER_CLOSED | order | pay关闭未支付尝试；promotion释放原冻结；product释放原预占；member撤销创建阶段资源（如有） |
| ORDER_SHIPPED | order | member既有通知适配；确认收货倒计时仍由order拥有，不能迁给member |
| ORDER_COMPLETED | order | promotion完成活动奖励；order内分佣解冻处理（同库事务或独立幂等处理器） |
| STOCK_PRE_RESERVED | product | promotion更新对应活动资源状态，不再次扣商品库存 |

原计划 PAY_SUCCESS/REFUND_SUCCESS 直接驱动权益的消费者划分按用户确认修订；只在 order 接受了资金事实后产生权益事件。若一次 HTTP 请求后新状态尚未同步，现有查询返回真实处理中状态，不能提前返回完成。

事件信封：eventNo、eventType、schemaVersion、businessKey、aggregateNo、occurredAt、traceId、payload。PAY 载荷含原 paymentNo、orderNo、memberId、amount、channel，REFUND 含原 refundNo、afterSaleNo、paymentNo、orderNo、memberId、amount。order 事件只使用已保存快照，售后载荷含原 orderItemId、skuId、quantity、冻结现金、returnUsePoint、deductGivePoint、ruleVersion、是否全退及原促销关联键。只包含该消费动作必需字段，禁止地址、手机号、token、渠道私钥等。

可靠性协议：

1. 业务提交与 outbox 插入使用同一个本地数据库事务；唯一 `(event_type,business_key)`。重试不生成新编号，相同业务键但不同载荷报错。
2. Relay 以数据库时钟领取120秒租约并增加 execute_version；事务外同步发送，版本匹配才可完成。发送成功但标记前崩溃允许重投。
3. 发布重试起始5秒、上限10分钟；连续20次失败转 FAILED 并保留人工重试入口/告警，不能丢弃。重试沿用原事件号；重置增加执行版本，旧工作线程不能覆盖。
4. 消费者群组按服务与业务处理器隔离；Inbox 唯一键插入、业务效果在同一本地事务。失败全部回滚并向 RocketMQ 抛异常；异常不能被 BaseConsumer 吞掉。
5. 同一原事件的重复直接返回；同业务键不同 eventNo 也需去重并比较载荷。资金、库存、券、积分还保留原业务唯一键/CAS，不只依赖消息去重。
6. 不依赖不同 Topic 的投递顺序。退款权益必须等该订单原支付权益已记账，旧 PAID 不能覆盖已 CLOSED/REFUNDED；缺前置事实先重试/查询可信快照，不静默跳过。
7. 消费重试次数在源码模板明确为16次后死信，死信可审计重放且沿用原信封。未知版本、归属不符或金额不符不标为消费成功；错误分类与告警可见。

## 11. 安全设计

- 公共端点沿用原 Controller 的权限注解与登录校验；权限名称中的 mall/trade 不因新模块名重写。完整权限核对以原 Controller 为源。
- memberId/operatorId 从认证上下文获取，不能相信 HTTP 自报。内部 RPC 仅允许可信服务，支付回调继续原验签、防重放和渠道关联校验。
- 同订单、同支付、同售后都校验原会员、金额、状态和原业务号；客户端或 MQ 的数值不能覆盖持久化冻结值。
- 保留现有限流阈值与窗口，不无依据新增统一阈值。重构不能绕过网关现有鉴权/限流。
- Nacos、数据库、MQ、渠道凭据仅使用环境占位符，不复制已有配置中的默认秘密，也不保存令牌到文档/测试证据。

## 12. 日志与监控

新增链路只记录 eventNo、orderNo、afterSaleNo、状态、次数、耗时和脱敏错误摘要，使用占位符日志。继承当前 traceId 透传；异步信封携带 traceId，不记录完整事件 payload。

| 指标 | 口径与建议告警阈值（待部署校准） |
|---|---|
| commerce.outbox.pending.age | 最老可发送消息年龄；连续5分钟超过60秒告警 |
| commerce.outbox.failed | FAILED记录数；大于0告警 |
| commerce.inbox.failure | 消费失败次数，按事件/处理器分组，不以订单号作为指标标签 |
| commerce.mq.deadletter | 死信数大于0告警 |
| commerce.payment.reconcile.mismatch | 支付/退款归属、金额或状态不一致数大于0告警 |
| commerce.inventory.confirm.pending | 已支付但未确认库存的数量与最老年龄 |

沿用当前 metrics 基础组件，不引入新监控平台。告警渠道复用部署环境现有通知方式；未配置通知时如实记录为待部署验证，不宣称告警已送达。

## 13. 测试方案

所有现有测试保留有效断言；原跨模块同库集成测试拆成契约、隔离数据库和事件组合测试，不能删除测试换取通过。

| 单元测试 | 入参 | 预期 |
|---|---|---|
| 退款尾差 | 10/6，依次2/3/1件 | 3.32/4.98/1.70；创建时冻结，重试不变 |
| 零现金份额 | 0.01/3，依次1/1/1件 | 0/0/0.01；前两次无渠道资金流水 |
| 历史未知 | 已退数量/权益快照NULL | 明确阻止新售后，不补造快照 |
| 付款/关单竞争 | CLOSED订单收到成功资金事实 | 保留异常对账，不发ORDER_PAID权益 |

| 集成测试 | 前置条件/输入 | 预期 |
|---|---|---|
| 事务消息原子性 | 业务事务在outbox插入后抛错 | 业务与outbox都回滚 |
| 发送后崩溃 | MQ已接受，发送状态未写入 | 原eventNo重投，目标只处理一次 |
| 消费失败 | Inbox插入后库存/积分失败 | Inbox与业务都回滚，异常返回broker |
| 多消费者/租约 | 两个worker及过期旧worker | 同版本CAS领取；旧worker不能完成新任务 |
| 独立数据库 | 四套仅本服务表的H2/测试库 | 无隐式跨库JOIN，支付退款仅访问pay表 |
| 并发售后 | 同订单两线程不同退单 | 只允许一个处理中；成功数量不越界 |
| 路由/JSON | 原313端点清单及Req/Res样例 | 方法/路径/字段/权限不变，全部映射到唯一服务 |

| E2E/真实环境场景 | 步骤 | 验证点 |
|---|---|---|
| 普通现金订单 | 购物车→报价→下单→支付回调→发货→收货 | 券、库存、积分、分佣无重复且可追溯 |
| 部分退款 | 同一行多次分批退款并重投所有事件 | 现金、数量、积分、优惠守恒 |
| 多SKU积分订单 | 零元下单→按行分批退→最后一行退完 | 无伪支付，最终全退状态准确 |
| 故障恢复 | 停MQ/停消费者后恢复，回放死信 | 无丢消息，无重复资金/权益 |

真实环境未执行前以上 E2E 标记待验收。核心业务测试采用生产建表语句，隔离库而不是把四服务所有表混装到同一个测试库来掩盖依赖。

## 14. 验收标准

- A1：Given 全部新模块接入 reactor，When 从 server 聚合构建并执行目标测试，Then 四个start及依赖通过，原有效测试无删除/无弱化。
- A2：Given 64张原业务表，When 检查映射与新SQL，Then 64个唯一归属/唯一新名，字段/原数据语义保留，三条跨库外键被可信校验替代，同库约束有效。
- A3：Given 每个服务只有自己的数据库，When 执行下单、支付、退款隔离集成测试，Then 没有跨上下文表查询及业务实现依赖。
- A4：Given 同一事件重复/乱序/发送后崩溃，When 恢复处理，Then 现金、库存、券、积分效果各一次；不匹配载荷与未知版本不能成功确认。
- A5：Given 10/6及0.01/3部分退款，When 多批申请及重试，Then 冻结金额、尾差、已退数量与积分守恒，无零额渠道请求。
- A6：Given 原313端点，When 执行路由与HTTP模型对比，Then 方法、路径、权限与响应信封保持，member/auth/system路由不受影响。
- A7：Given 历史任务、结算恢复键、未知快照，When 做切流预检，Then 所有未决项可追溯，未知项阻止切流，不能静默丢弃。
- A8：Given 全部代码阶段通过，When 清理旧模块，Then 生效源码/构建中旧mall/trade业务包依赖为0，系统项目及SQL未被修改；真实环境验收另列结果。

## 15. 输出要求与执行约束

正式设计、任务和代码交付均放本工作项。`file-manifest.md` 为逐文件新增/适配/删除、复杂度、前置依赖与工时预估；`implementation-plan.md` 为阶段依赖与验证；`generation-preview.md` 为审批摘要；`evidence/` 保存真实表、端点、依赖和文件哈希基线。

本文件等待确认，`design` 保持 in_progress，`task/generate` 不能伪标 completed。用户确认设计和逐文件清单后，完成正式 tasks.md 并按 cs-generate 写入代码。实现中若发现未列入清单且影响 API、字段、业务流程的变更，先核实并更新设计；常规编译修复不另起审批。

每阶段输出实际变更、运行命令及结果。仅文档/静态检查、聚合测试、真实数据库执行、Nacos/MQ/Dubbo/HTTP验收分层记载；不得把某一层成功扩写为全部完成。


执行记录：用户在本预览后再次明确调用 cs-generate；方案与文件范围已确认，开始按 tasks.md 实施。以上“待确认/尚未生成”段落为确认前基线，实际进度以 tasks.md 与 code-summary.md 为准。


实施细化：资源RPC的锚点、锁定、核销、回退凭证复用promotion_message_inbox，同本地券/活动修改事务提交；关单先到时留下终止凭证，防止延迟冻结。折扣SKU互斥同样使用本库稳定锚点，并使用READ_COMMITTED保证串行后读取最新活动，不共享远程商品锁。订单权益完成状态将通过原业务号查询各消费方凭证，不增加第9个Topic。

用户补充要求：全部Java类型统一短名及显式import，覆盖泛型、implements、参数/返回值、静态调用和跨行引用。因此system Java只同步类型引用风格；原排除的system业务及SQL仍不调整。
