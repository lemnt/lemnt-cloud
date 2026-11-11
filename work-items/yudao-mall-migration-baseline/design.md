# 芋道商城迁移基线与运行验收——技术设计

> 工作项：`yudao-mall-migration-baseline`；版本：2026-11-06。
> **批准快照：设计及文件级实施计划已获用户确认（2026-11-06T09:30:37+08:00），当时未实施。当前进度：B1a/B1b已完成自动化验证，generate仍为in_progress；B2a及后续批次未实施，真实运行验收未完成。**
> 用户确认原文：“确认计划，拆解任务”。此确认保留B2b/B3/B4的前置门控，不授权跳过补充设计、直接删表或执行环境变更。
> 依据：[正式需求](requirement.md)、[已确认分析](source-analysis.md)、[已确认章节结构](design-outline.md)。
> 未使用模版文件：项目模板、skill列出的references及state-machine文件不存在，采用内置15章骨架，业务规则引用真实源码。
> 设计阶段代码基线：`3ea13ae8`；下述45处引用复核为设计阶段记录，不是最新执行快照。B1a/B1b执行HEAD为`12745a285d36869f2005d9f4ffdada93351ec1e4`，当前未提交成果及验证以[tasks](tasks.md)和evidence为准。

## 1. 工作流

### 1.1 实施流程

| 批次 | 输入 | 执行顺序与模块 | 出口 |
|---|---|---|---|
| B0 基线 | 三份本地源代码、当前项目 | 入口全量盘点 → 方法/字段/状态/前端引用映射 → 标注差异及未知 | 47个目录族有负责人、证据和后续任务；不把入口存在当完成 |
| B1 运行阻塞 | F01/F02/F03/F10 | 域协议边界 → infra转换 → application依赖收敛 → gateway/小程序地址 → 配置核对 | 新路径鉴权、评价读写及生产依赖门禁通过；不改数据库 |
| B2a 已有能力补齐 | 已证实查询/输出裁剪 | Gateway/查询契约 → 执行器 → Req/Res/Converter → 两端API/页面 → 回归 | 分类排序/ids、已有商品字段输出、多分类及已具备数据的排序 |
| B2b 源商品资料 | 字段映射、管理端与购买链源规则 | 确认字段方案 → 初始化DDL/DO/XML → 域/应用/HTTP契约 → 读写规则与两端 | 源商品配置、可见性/错误/浏览副作用闭环，不只加列 |
| B3 交易与消息收敛 | 下单/支付/关单/售后逐方法图、记录表分类 | 每事件业务结果/恢复依据 → 域/infra适配 → 业务/接口 → MQ配置及测试 | 无未说明的消息表依赖、无调用断点；不执行旧库删表 |
| B4 其余源能力 | 能力矩阵剩余项 | 逐能力补充设计、精确文件清单 → 实施/回归/前端操作 | 源能力逐项验收，内容/统计等不擅自移为二开 |
| B5 本地验收 | 确认的独立环境与运行配置 | 中间件/空库 → 服务注册/调用 → 网关 → 管理端/小程序 → 主线/异常 | 可复现实际结果，外部条件缺失显式阻塞 |

每批内部按**数据层→Client/边界契约→业务编排→HTTP/两端→配置**安排，实际依赖优先，不能为套顺序引入错误反向依赖。B1/B2a可先落地；B2b/B3/B4的门控是本范围内前置任务，不代表排除迁移责任。

### 1.2 人员/系统职责

用户确认业务取舍、独立环境可操作范围和具体实施清单；开发按证据适配现有模块；测试分别记录自动化与实际运行。源项目目录只读，目标项目system/common/auth/gateway仅做商城运行必需的适配。

## 2. 需求背景

### 2.1 AS-IS / TO-BE

| AS-IS | TO-BE |
|---|---|
| 当前项目已有业务和大量测试，但缺少完整源能力映射 | 每个源入口和能力能追溯到目标、差异及验证状态 |
| 路由已改，会员鉴权与小程序评价读URL滞后 | 实际URL、路由、身份域、权限和调用方一致 |
| Store*裁剪源商品资料，存在额外库存/事件机制 | 补齐源业务能力，必要架构适配与额外设计分开 |
| 构建通过被扩大解释为能运行 | 构建、真实基础设施、页面操作、渠道分别验收 |

代码依据：`lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/filter/AuthFilter.java:L100-L132`；`lemnt-miniapp/services/comments.js:L55-L101`；源分页`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/mysql/spu/ProductSpuMapper.java:L69-L92`。完整绝对路径及hash见[source分析证据](evidence/source-analysis-references.json)。

### 2.2 目标

- G1：本地源product/promotion/trade/statistics/member/pay相关Controller入口100%纳入盘点（本次112个、47目录族），每个方法最终有语义/目标/证据；数量不是固定产品指标。
- G2：已知P0运行断链归零，当前真实路径通过安全及契约回归。
- G3：外部Client生产依赖在非infra层归零；不删除本服务Client提供者依赖，不将测试依赖误计。
- G4：核心闭环在真实本地隔离基础设施执行；源其余能力逐批验收，未验收项不归零作假。
- G5：已有额外机制全部有决定及理由；二次开发与迁移清单分离。

需求类型：迁移完整性修复＋必要架构适配＋运行验收，不是新商城平台设计。

## 3. 需求范围

### 3.1 本次要做

S01 全量源码/前端入口盘点；S02 修运行断链；S03 源商品查询和资料补齐；S04 交易/支付/履约/评价/售后规则核对；S05 营销/会员依赖/内容/统计等其余能力分批迁移；S06 依赖边界；S07 记录表与MQ逐项收敛；S08 本地配置和验收；S09 单独记录二开需求。

### 3.2 本次不做

- 不替换React/原生小程序、Java17/现有框架：保留项目底座。
- 不迁入芋道全部ERP/CRM/BPM/AI：超出商城需求。
- 不整批回滚/删除已实现能力：先核对调用和数据影响。
- 不重新引入product会员RPC、跨层外部Client或全局业务BOM：违反已确认边界。
- 不自动执行现有库DDL、清消息积压、真实资金操作或生产发布：没有授权。
- 不新增独立缓存/监控/分布式事务平台：不是本次必要条件。
- 不自动commit、push或修改索引：遵守项目约定。

## 4. 需求分析

### 4.1 源与目标交互

源代表链：用户选品 → 结算价格 → 创建前库存/活动Handler → 保存订单项 → 创建后券/购物车/支付处理 → 支付持久化通知任务/HTTP回调 → 交易后置Handler。

当前代表链：用户选品 → 缓存结算 → 保存订单/项 → 促销/会员资源冻结 → 商品预占 → 支付资金事件 → 订单消费/后置事件 → 各域业务确认。不能仅因当前更复杂便认定必须保留，也不能跨进程照搬源本地事务。

评价链保持：可信会员上下文 → **order资格核验** → product保存核验事实；公开查询在product。后台自动评价另有系统任务身份，不能用缺失的会员请求线程伪造昵称。

证据：源`yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/TradeOrderUpdateServiceImpl.java:L186-L205,L251-L320,L914-L966`；目标`lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/service/OrderRemoteService.java:L32-L79`；`OrderCommentService.java`完整路径及行号见[source-analysis F08](source-analysis.md)。

### 4.2 状态/业务规则

以下为当前应保留并测试的业务结果约束，不新增状态机：

| 当前状态/事实 | 事件 | 结果约束 |
|---|---|---|
| 未支付/预占中订单 | 支付未成功且满足关闭条件 | 关闭后资源恢复且不得再次成功扣同一预占；支付状态不明不能当未支付 |
| 成功支付单 | 重复相同结果 | 订单/库存/权益业务效果一次；同号不同金额/渠道事实拒绝 |
| 库存预占RESERVED | 支付确认 | CONFIRMED；不再次扣可用库存 |
| 库存RESERVED或取消先到 | 合法关单 | RELEASED；取消终态防延迟补扣 |
| 库存CONFIRMED | 原单合法售后完成 | 按原件数回补，累计不得超过原购买量 |
| 完成的本人订单项 | 用户投稿 | 保存原订单项评价并更新评价状态；他人/未完成订单拒绝 |
| 商品停用/历史未知字段 | 查询/迁移 | 按源与已确认适配规则展示；不从当前配置补历史快照 |

源整数状态与目标字符串状态必须逐事件映射，不只按数字或枚举名称对应。源零售价、有效SKU/详情错误/访问副作用改变时，必须连同DB输入校验、库存购买及两端处理同批验收。B2a先补不改变既定行为的能力；不能因此标记最终源行为完全一致。

### 4.3 风险

| 风险→影响 | 应对 |
|---|---|
| 新路径未进入会员身份域→会员令牌被当管理员/公开读401 | 真实路径通过完整AuthFilter+Route回归，不扩大匿名通配 |
| 分/元及大ID转换错误→百倍金额/编号损坏 | BigDecimal精确转换、边界解码、请求/落库/响应三方断言 |
| 删消息表但未承接调用→支付成功不更新订单/资源不释放 | B3逐事件恢复依据和文件范围门控，禁止只删表 |
| 缺字段用当前配置回填→伪造成本/时间/会员历史事实 | NULL保持未知，空库初始化与已有库升级分开 |
| 测试替身覆盖旧路径→自动化绿但实际断链 | 分别测试GET/POST真实URL、匿名/会员/管理员域，最后本地HTTP操作 |
| 源已存在内容/统计漏盘→“全量”名不副实 | 112入口族映射到方法与页面，未核实状态保留 |
| 工作区与本地源变化→旧证据失效 | 实施前重算引用hash及HEAD；不覆盖用户未提交内容 |

## 5. 整体架构设计

### 5.1 模块职责

| 模块 | 职责与依赖 |
|---|---|
| product | 商品定义/库存/公开评价与商品互动；不核验订单资格、不查询会员昵称 |
| promotion | 促销规则、活动资源、购物车/券及源相关内容；具体源能力逐族映射 |
| order | 结算、订单事实、履约、售后、评价资格与编排；通过域Gateway使用外部服务 |
| pay | 支付渠道、资金事实、退款、钱包；渠道通知与订单业务结果分开 |
| member | 会员账户、等级/积分等商城依赖；不承接订单持久化 |
| auth/gateway/common | 复用既有认证、精确路由/安全上下文、序列化与公共组件；只必要适配 |
| React/原生小程序 | 参考源交互，继续当前技术栈，经边界转换消费目标接口 |

依赖树保持`start → adapter → application → domain`，以及`start → infrastructure → domain`；本服务Client提供者在application/dubbo，外部Client仅infra。Domain不继承BaseDO，不用Spring/Jackson/MyBatis注解。

### 5.2 外部事件隔离

每个消费上下文增加**本上下文**域端口`InboundEventGateway`和明确record事实集合`InboundEventFacts`，infra实现读取外部Client协议并映射。原JSON、eventNo/businessKey/schemaVersion保持；handler继续对合法性/状态和事务负责。没有新增业务流程、共用万能事件对象或消息表。

受影响消费者：promotion的paid/closed/after-sale/completed/stock-pre-reserved；order的pay-success/refund-success；pay的order-closed；member的order-benefits。完整文件列入实施计划。接口测试必须证明所有实际消费字段保留后，才删6处application依赖。

## 6. API设计

完整请求参数、类型、示例、响应字段、权限和错误映射见[contract-mapping.md](contract-mapping.md)，本文不另造一套接口规范。

| ID | 方法/目标地址 | 身份 | 变更 |
|---|---|---|---|
| A01 | GET /app/products | 公开 | 多分类/可选排序，默认与兼容字段保持 |
| A02 | GET /app/products/{id} | 公开 | 输出已有轮播、简介、配送和SKU重量体积；最终详情错误/副作用在B2b同批处理 |
| A03 | GET /app/categories | 公开 | sort/id稳定排序，补ids过滤 |
| A04 | GET /app/comments | 公开/product | 修小程序读URL，业务分页不重写 |
| A05 | POST /mall/app/comments | 会员/order | 保留正确归属，body不能伪造身份/展示快照 |
| A06 | /app/favorites、/app/browse-historys及当前子路径 | 会员 | 修身份分类，既有方法/数据协议不变 |

不新增后台权限码满足上述公开读；后台商品资料扩展复用`product:spu:add/edit/query/list`等真实Controller权限。新增实际路径/权限时纳入源能力和两端映射，而非统一改成另一种API规范。

## 7. 数据库设计

### 7.1 分批边界

**B1/B2a无DDL变更**：已有资料可以先透出，分类排序与ids不需要新表。B2b才补商品真实缺字段；以下为设计DDL提案，尚未写入生产初始化SQL、未执行。已有表/索引在`lemnt-server/lemnt-product/sql/lemnt_product.sql:L82-L144`。

| 表 | 候选补齐字段 | 含义/类型 |
|---|---|---|
| product_spu | spec_type、give_integral、sub_commission_type | Boolean单/多规格、非负赠积分、独立佣金配置；历史未知NULL |
| product_spu | virtual_sales_count、browse_count | 展示虚拟销量/浏览量；新隔离空库新记录从0开始 |
| product_spu | create_time | 真正创建时间；旧资料未知不得填当前时间或推ID |
| product_sku | market_price、cost_price | DECIMAL(19,2)元，历史NULL；不能用售价补造 |
| product_sku | bar_code | 商品条码，与当前业务product_code区分 |
| product_sku | first_brokerage_price、second_brokerage_price | DECIMAL(19,2)元，需与分佣源规则及订单快照联动 |
| product_sku | sales_count | 源字段候选；在B2b与SKU支付累计/查询映射一起确认，不仅加列 |

源模型依据：`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/spu/ProductSpuDO.java:L93-L170`；`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/sku/ProductSkuDO.java:L48-L95`的完整路径见[source-analysis](source-analysis.md)。

### 7.2 DDL提案（仅用于评审）

```sql
-- 下列ALTER仅表达增量；正式实施先整合所属服务最终CREATE TABLE。
-- 不执行于已有数据库；历史库升级需要独立清单、备份与审批。
ALTER TABLE product_spu
    ADD COLUMN spec_type TINYINT(1) NULL COMMENT '规格类型：0单规格、1多规格；历史未知NULL',
    ADD COLUMN give_integral INT NULL COMMENT '下单赠送积分配置；历史未知NULL',
    ADD COLUMN sub_commission_type TINYINT(1) NULL COMMENT '佣金配置：0默认、1单品配置；历史未知NULL',
    ADD COLUMN virtual_sales_count INT NOT NULL DEFAULT 0 COMMENT '展示虚拟销量；仅新隔离空库按0初始化',
    ADD COLUMN browse_count INT NOT NULL DEFAULT 0 COMMENT '商品浏览次数；仅新隔离空库按0初始化',
    ADD COLUMN create_time DATETIME NULL COMMENT '商品创建时间；历史未知NULL',
    ADD CONSTRAINT chk_spu_virtual_sales CHECK (virtual_sales_count >= 0),
    ADD CONSTRAINT chk_spu_browse_count CHECK (browse_count >= 0),
    ADD CONSTRAINT chk_spu_give_integral CHECK (give_integral IS NULL OR give_integral >= 0);

ALTER TABLE product_sku
    ADD COLUMN market_price DECIMAL(19,2) NULL COMMENT '市场价，单位元；历史未知NULL',
    ADD COLUMN cost_price DECIMAL(19,2) NULL COMMENT '成本价，单位元；历史未知NULL',
    ADD COLUMN bar_code VARCHAR(64) NULL COMMENT '商品条码；与业务编码区分',
    ADD COLUMN first_brokerage_price DECIMAL(19,2) NULL COMMENT '一级佣金，单位元；历史未知NULL',
    ADD COLUMN second_brokerage_price DECIMAL(19,2) NULL COMMENT '二级佣金，单位元；历史未知NULL',
    ADD COLUMN sales_count INT NOT NULL DEFAULT 0 COMMENT 'SKU支付确认累计销量；仅新隔离空库按0初始化',
    ADD CONSTRAINT chk_sku_market_price CHECK (market_price IS NULL OR market_price >= 0),
    ADD CONSTRAINT chk_sku_cost_price CHECK (cost_price IS NULL OR cost_price >= 0);
```

原主键、业务编码唯一索引、SKU规格组合唯一约束和SPU/分类/品牌外键保留；本批不无证据新增排序索引（动态排序/小数据不代表新索引必然有效）。B2b对正式DDL重跑、原约束和新字段读写进行MySQL验证，按实际执行计划再决定索引。

“默认0”仅对新隔离空库计数初始化成立；**禁止将该ALTER直接用于旧库而让未知历史被写成真实0**。本设计不含旧库回填脚本。

已核实源`yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/admin/spu/vo/ProductSkuSaveReqVO.java:L19-L22`允许售价为0。B2b需要把目标输入下限、数据库`price>0`约束、公开展示、结算与零金额订单路径作为同批适配，全部回归后才能交付零售价能力；B2a不单独放开约束。条码VARCHAR(64)是目标长度提案，实施前核对源DDL/实际输入边界，超限明确拒绝或经确认扩大，不静默截断；佣金/赠积分条件也须联动源Service规则。

## 8. 枚举与常量

| 对象 | 设计 |
|---|---|
| 商品排序 | 仅price/salesCount/createTime白名单；空值默认sort DESC,id DESC，不动态拼SQL字段 |
| 商品生命周期 | 保留ON_SHELF/OFF_SHELF/RECYCLED/DELETED，映射源状态语义；不改公开字段含义 |
| 预占状态 | RESERVED/CONFIRMED/RELEASED/RETURNED暂按当前业务记录核对，去留属B3，不因名称直接删除 |
| 会员/管理员身份域 | 复用现有SecurityConstants，用户路径精确匹配，不以请求体决定身份 |
| 权限 | 复用当前product:*与其他域实际权限，前后端及sys_menu同步验证 |
| 事件类型/版本 | 保持现有8类Topic、schemaVersion=1等已存在协议；无新增事件常量 |

新增枚举类不是必须；可在当前查询模型用固定白名单保持简洁。createTime/展示销量排序只在对应数据方案就绪后开放验收。

## 9. 缓存设计

本次无新增缓存体系/商品缓存：先核对复用，不为迁移增加双写和失效问题。

| 已有对象 | Key/类型 | TTL | 处理 |
|---|---|---|---|
| 会话 | `lemnt:global:login_tokens:{token}` / 序列化SecurityUser | 30天，距过期120分钟内按既有流程续期 | 复用；会员/管理员域校验，不写明文令牌到资料 |
| 结算快照 | `mall:{settlement}:v2:`既有族 / 序列化快照 | expiresAt，当前准备流程900秒 | 不改Key；不缓存重算出的新价格替代原确认快照 |
| 请求/绑定/恢复 | 同族request/binding/submission/cart/abandon | 当前恢复30天 | 保留原恢复键及订单业务号，收敛前验证 |
| 会员操作锁 | `lemnt:mall:member:operation:{id}` | 既有Redisson执行器/事务释放；等待参数3000ms | 这是锁不是业务缓存，不误写成永久缓存或擅改租约 |

证据：`lemnt-common/lemnt-common-core/src/main/java/com/lemnt/common/core/constant/CacheConstants.java:L22-L44`；`lemnt-server/lemnt-order/lemnt-order-infrastructure/src/main/java/com/lemnt/order/infrastructure/settlement/gateway/SettlementGatewayImpl.java:L30-L55`；`lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/settlement/service/SettlementPreparationService.java:L402`。

## 10. MQ设计

### 10.1 当前消息拓扑（不是新建协议）

| Topic | 当前生产者 | 当前承接方 | B3需验证 |
|---|---|---|---|
| PAY_SUCCESS | pay | order | 原支付成功事实/金额/渠道、重复/迟到支付 |
| PAY_REFUND_SUCCESS | pay | order | 原支付退款事实、售后状态不可提前成功 |
| ORDER_PAID | order | product/promotion/member | 库存确认、权益效果及既定销量一次 |
| ORDER_CLOSED | order | pay/product/promotion/member | 支付关闭、库存/券/积分恢复 |
| AFTER_SALE_COMPLETED | order | product/promotion/member | 按原单件数/冻结权益处理，不重算历史配置 |
| ORDER_SHIPPED | order | member等实际消费者 | 规则与发货事实、不可被提前赠送 |
| ORDER_COMPLETED | order | promotion | 拼团等完成事实 |
| STOCK_PRE_RESERVED | product | promotion | 当前仅事实核对，不再次扣活动库存 |

现有部分消费者配置最多16次重消费，耗尽转broker死信；不能把该数字自动视为所有消息/事务回查的统一策略。设计无新增Topic、消费者组、通用重试平台。

### 10.2 去本地消息表的门控

目标是满足用户不默认保留本地消息表的要求，**不是写一句“使用RocketMQ”就删除Outbox**。逐条说明不可变业务键、业务结果原子提交点、正/负/未决事实、回查/去重、异常恢复及旧调用替换，再给出代码/DDL变更。

禁止仅用“业务行存在”作为事务消息COMMIT：取消先行的终态、成功后释放的记录、重复请求和并发赢家必须区分。DB暂不可查应UNKNOWN，而不是误判成功/回滚；最终回查次数与停机时长在真实broker验证。没有确定事实依据的事件是B3设计阻塞，不新增一张“事务状态表”绕过约束。

Outbox/Inbox、源支付通知任务/尝试日志、钱包流水、预占/确认/活动/退款台账分别在[decision-log](decision-log.md)列出。业务台账不默认不可删，通知任务不冒充资金流水，不把停用Relay开关当作最终收敛。

## 11. 安全设计

- 公开GET仅真实商品列表/详情、分类、评价查询等已存在公开能力；POST评论仍走order会员路径。
- `/app/favorites`、`/app/browse-historys`及其合法子路径按会员令牌域；管理员令牌不能进入会员业务，会员令牌不能进入管理端。
- 客户端X-User-Id/X-User-Nick-Name/X-User-Avatar等先清理，再由可信会话注入；body会员、昵称及可见性字段不能扩大权限。
- API权限逐条见contract-mapping；后台字段扩展复用实际Controller权限，检查sys_menu和页面按钮，不凭管理员角色名称跳鉴权。
- 幂等复用原业务号/订单项/请求号的既有规则，结果不明先查原记录；不无理由给所有接口新增Idempotency-Key或另建表。
- 本次无新增限流阈值（原因：迁移非防刷策略重做）；沿用已存在登录保护、接口/网关约束，实施时记录生效配置和边界测试。不得无证据声称具备某固定用户/IP限流值。
- 本地环境使用独立库/Redis实例或明确隔离；不得因方便运行全局放开CORS、关闭权限、共用生产密钥。

## 12. 日志与监控

本次无新增监控平台和性能SLA。复用当前TraceId、服务日志与现有监控；原样记录环境/服务启动、注册、外部调用、回调/消息处理结果及耗时。

| 关注点 | 证据/告警口径 |
|---|---|
| 启动及配置 | 记录服务名、端口、profile/namespace/group、失败原因；不打印数据库密码/令牌 |
| 核心请求 | 按traceId+订单号+请求号定位，不记录完整敏感载荷 |
| 消息/支付异常 | 业务号、事件号、类型、结果/错误摘要；重试耗尽/状态不明在本地验收报告列失败或阻塞 |
| 迁移完成率 | 源能力总数、已核实/已实施/自动化/本地验收/阻塞各列，不混成一个“完成” |
| 发布结果 | 实际命令/时间/退出码和服务结果关联，不把stdout中的无ERROR当成功 |

正式通知渠道和性能阈值不在本批凭空新定；如运行平台已有告警，记录其实际配置，不称已接入某通知服务。

## 13. 测试方案

### 13.1 单元（至少3项）

| 测试 | 入参 | 预期 |
|---|---|---|
| U01身份域 | /app/favorites +会员/管理员/空token | 会员可进入正确域，管理员/空token拒绝；不依赖body身份 |
| U02读写URL | comments.page/create | GET /app/comments；POST /mall/app/comments；GET无写令牌要求 |
| U03查询转换 | categoryId+categoryIds重复、非法sortField | 分类去重/正数/上限；非法排序拒绝，SQL无动态字段插值 |
| U04协议隔离 | 每类原JSON、缺字段/未知版本 | 本域record保留金额/积分/业务号；非法不能成为成功消费 |
| U05输出 | null轮播/成本、大Long、元字符串 | 不补造历史值、不失精度 |

### 13.2 集成（至少3项）

| 测试 | 前置 | 预期 |
|---|---|---|
| I01类目/排序 | 父类目、启用/停用直接子类、排序值相同 | 与源扩展口径一致；sort/id稳定，不取任意深度 |
| I02字段读写 | 源字段映射、生产Mapper/XML、独立库 | 资料写入、查询及管理更新不丢字段；未知NULL保留 |
| I03支付与关单 | 原支付/库存/订单及重复/乱序事实 | 金额/库存/权益结果一次，未决支付不当未支付关闭 |
| I04销量与回滚 | 多SKU同SPU、重复事件、DB写失败、售后 | 已确认销量口径保持；失败原子回滚；同锁序不引入SKU/SPU锁反转 |
| I05消息表收敛 | 每事件已批准替代链及真实broker | 成功/回滚/UNKNOWN、进程中断/重复/取消先行按业务事实判定，不靠新通用消息表 |

### 13.3 E2E（至少3项）

| 场景 | 步骤 | 核验 |
|---|---|---|
| E01核心购买 | 管理员发布商品→会员浏览→选购→结算→下单→支付结果→履约→收货→投稿 | 网关/服务/DB/页面一致，评价公开查询可读，源业务规则满足 |
| E02未支付关闭 | 建单→会员取消或超时关闭→重复关闭 | 商品及活动/券等资源恢复，迟到回调明确处理，不双扣/双退 |
| E03售后 | 已付订单→按项申请→审核→退款沙箱结果→完成 | 原冻结金额/件数、库存/权益与订单状态一致，重复结果幂等 |
| E04安全 | 匿名商品查询、他人订单、伪造头/昵称、错身份token | 公开可读、写操作越权拒绝、敏感字段不泄露 |

自动化替身、H2模式、真实MySQL/MQ/Redis、浏览器和微信工具/真机分别记录。缺外部支付沙箱时E01支付段/E03不能标真实通过；替身结果仍可计自动化，但不计外部验收。

## 14. 验收标准

- AC01 Given本地源与当前源码，When盘点，Then112入口对应方法/能力均有去向及证据，所有未知/阻塞可见，未分类项为0；源变化重建基线。
- AC02 Given当前真实新路径，When匿名GET/会员写/错身份token请求，Then路由、公开规则、身份域正确；不扩大通配匿名白名单。
- AC03 Given评价分页/投稿，When小程序发请求，Then分别命中product GET与order POST，原匿名脱敏及订单资格不弱化。
- AC04 Given业务Client使用与POM，When门禁和编译，Then非infra的外部Client生产依赖/引用为0，本服务提供者和测试scope正确区分。
- AC05 GivenB2已批准字段/查询，When保存和查询两端，Then资料/金额/编号/排序符合映射，不把已存字段未输出误当DB缺失。
- AC06 Given支付/关单/售后重复、失败和乱序，When恢复，Then业务结果可核验一次，消息表去留与替代链均有批准记录；不能仅停开关宣布无消息表依赖。
- AC07 Given隔离库、中间件及配置，When启动真实服务并操作页面，Then注册、HTTP、RPC、必要消息和核心闭环均有实际证据，而非仅测试通过。
- AC08 Given外部渠道/证书/微信工具缺失，When收口，Then相应验收明确阻塞或未完成，不能伪称全量已跑通。
- AC09 Given完整能力矩阵，When全量验收，Then每项状态闭环（通过或用户明确排除），不把范围内的内容/统计静默转二开。
- AC10 Given任何批次实施，When交付，Then保留用户改动，不执行未授权旧库DDL/提交/push，新增断言不通过时不得删除以过构建。

## 15. 输出要求

本设计及配套文件：

1. `design.md`：本15章正文。
2. `capability-matrix.md`＋`evidence/capability-inventory.json`：112个源Controller/47目录族首轮索引与状态；仍需方法语义核对。
3. `contract-mapping.md`：首批完整API与数据映射，B2b门控。
4. `decision-log.md`：既定约束与待决定的具体收敛，不变相保留消息表。
5. `implementation-plan.md`：精确文件清单、顺序、复杂度、前置及阻塞；可用于后续cs-task，阻塞项不可直接cs-generate。
6. `local-acceptance.md`：环境/操作/核验和证据分层。

代码引用以[source-analysis](source-analysis.md)和45处引用指纹为基础，设计阶段机械清单增加独立hash。设计阶段没有运行构建、数据库或服务；该历史事实不替代后续B1a/B1b自动化执行记录，文档校验不等于业务测试。

**后置门控记录**：用户已确认文件级实施计划，design阶段已完成；当时进入cs-task，现已完成任务拆解并进入generate。具体B2b/B3/B4补充设计仍是显式前置任务；此次计划确认不允许生成器猜测填空、跳过清单补齐或宣称全范围已具备可执行细节。
