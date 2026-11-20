# 源码分析发现——芋道商城迁移基线与运行验收

> 工作项：`yudao-mall-migration-baseline`
> 阶段：cs-design Step 3 源码分析检查点，**用户已确认分析结果**。
> 确认记录：2026-11-05T15:27:27+08:00，用户原文“确认分析，继续设计”。本确认不等于批准具体删表、数据变更或业务实施方案。
> 核对时间：2026-11-05T15:19:46+08:00。
> 本文不是完整技术设计、全量迁移完成清单或运行验收报告。

## 1. 本阶段范围与限制

- 已读取用户确认的 `requirement.md`；需求阶段 completed，设计阶段 in_progress。
- 参考本机 yudao-cloud、yudao-mall-uniapp、yudao-ui-admin-vue3。三个入口元数据文件指纹仍与需求文档一致；源目录没有 Git 元数据，不能标注上游 commit。
- 当前项目代码基线为 `cab9b5e1` 加已有工作区改动；分析没有回退、暂存、修改这些业务改动。
- 深入对照了商品分页/详情、分类、商品字段，以及下单、支付/关单、评价三条代表链；另核对网关、前端请求、Maven 声明与配置入口。
- **未做全量能力逐接口审计、未运行构建、未连接数据库或 MQ、未启动服务、未发布 Nacos。** 环境可用性和真实业务结果仍未验证。
- 项目 `templates/code_design_template.md`、skill 所列三份 references、`docs/state-machine.md` 均不存在。后续设计采用 skill 内置的15章结构，标记“未使用模版文件”；不据不存在的资料编造状态机。

## 2. 已核实发现

### F01｜优先级P0：商品新路径与网关会员鉴权未同步

**事实**：路由契约中的商品公开读与会员交互已使用 `/app/products`、`/app/categories`、`/app/comments`、`/app/favorites`。但 `AuthFilter.isAppPath` 只将 member/mall/trade 的旧 App 前缀归入会员身份域，没有 `/app/**`；公开读白名单也仍列旧商品路径。

**影响**：匿名浏览可能被要求登录；会员收藏等请求会被按管理员令牌域校验，而不是进入会员身份注入分支。全局白名单若额外放行也不能证明会员身份被正确注入。路由存在及 Controller standalone 测试通过，不能覆盖这个问题。

证据：
- [当前鉴权判断 AuthFilter.java:L47-L64、L100-L132](../../lemnt-server/lemnt-gateway/src/main/java/com/lemnt/gateway/filter/AuthFilter.java:100)。
- [当前商品路由 commerce-gateway-routes.yaml:L375-L382](../../docs/config/commerce-gateway-routes.yaml:375)。
- [现有鉴权测试仍测旧路径 AuthFilterAppRoutesTest.java:L55-L72](../../lemnt-server/lemnt-gateway/src/test/java/com/lemnt/gateway/filter/AuthFilterAppRoutesTest.java:55)。

**建议**：保留用户已调整的业务路径，精确同步公开读规则与受保护会员规则，补真实路径的匿名/会员/管理员令牌隔离和伪造头清理测试。不以扩大通配白名单作为修复。

### F02｜优先级P0：小程序评价读写共用地址，与已确认服务归属冲突

**事实**：契约约定 `GET /app/comments → product`，`POST /mall/app/comments → order`。小程序 `call(method, data)` 的地址却固定为 `/mall/app/comments`，分页和投稿共用该方法。

**影响**：投稿链路与目标归属一致，但公开查询会请求错误地址，且该请求不带会员令牌。即使解除鉴权，查询路径也没有对应的商品路由。

证据：
- [comments.js:L55-L68、L86-L101、L127](../../lemnt-miniapp/services/comments.js:55)。
- [已确认端点契约 gateway-endpoint-contract.json](../module-split-migration/evidence/gateway-endpoint-contract.json)。
- [源商品评价查询 comment.js:L4-L19](../../../yudao-mall-uniapp/sheep/api/product/comment.js:4)。

**建议**：只拆分请求地址选择，保留评价查询属于商品、投稿属于订单的边界，并分别验证两个方法的实际 URL、匿名/登录行为和响应读取。

### F03｜优先级P1：外部Client仍进入4个Application模块

直接解析生产 `dependencies`，发现6处外部 Client 声明：

| Application模块 | 外部Client | 源码 |
|---|---|---|
| promotion | product-client、order-client | [pom.xml:L13-L21](../../lemnt-server/lemnt-promotion/lemnt-promotion-application/pom.xml:13) |
| order | pay-client、promotion-client | [pom.xml:L24-L32](../../lemnt-server/lemnt-order/lemnt-order-application/pom.xml:24) |
| pay | order-client | [pom.xml:L13-L16](../../lemnt-server/lemnt-pay/lemnt-pay-application/pom.xml:13) |
| member | order-client | [pom.xml:L15-L18](../../lemnt-server/lemnt-member/lemnt-member-application/pom.xml:15) |

这些声明没有 test scope，与“外部 Client 只在 infra”的已确认边界冲突。自己服务的 Client 提供者依赖、测试依赖不能混在此计数中。

**建议**：逐个追溯实际类型使用，外部协议解析/调用留在 infra，经域端口返回本服务模型。先转换调用点再移除依赖；不重新把各服务 Client 汇总进全局 BOM 来掩盖边界问题。门禁应能区分生产与测试 scope。

### F04｜优先级P1：商品入口数量接近，但查询与字段迁移不完整

两端商品 Controller 集合均为13个（源 admin8/app5，当前 web8/app5），这只能证明入口族存在，不能证明业务等价。

已核实的代表差异：

| 能力 | 本地芋道源码 | 当前项目 | 影响 |
|---|---|---|---|
| App商品分页 | 多分类；合并启用的直接子分类；价格/销量/创建时间可选升降序 | 单分类，固定 sort/id 倒序 | 分类选品、上新和价格/销量排序不等价 |
| App详情 | 校验商品及状态，增加浏览量并调用浏览记录 | 隐藏/不存在返回成功null；详情链不自动增加浏览量/浏览历史 | 错误行为与访问副作用不同；当前另有显式浏览记录入口，不称全系统无历史能力 |
| 可见性 | 源App分页SQL实际只约束SPU上架，没有库存>0条件 | 还要求存在上架且价格>0的SKU，详情过滤SKU | 零售价/无有效SKU时结果不同；两端都不能据此断言零库存商品一定隐藏 |
| 分类列表 | 启用列表按sort升序；另有按ids列表 | 只有全量启用列表，Gateway未指定排序 | 运营排序和按ID取分类能力缺失 |
| 已有字段输出 | 源列表/详情包含简介、轮播、规格模式、价格/库存/配送等 | Store*是裁剪后的视图；当前已持久化的简介、轮播未完整返回 | 前端所需信息不能仅靠恢复字段名解决 |
| 商品配置字段 | 源SPU有marketPrice、costPrice、specType、giveIntegral、subCommissionType、virtualSalesCount、browseCount；SKU有划线/成本价及单品佣金字段 | 当前商品DO未完整承载这些字段 | 对应运营资料和配置能力需盘点补齐；不等同于全平台不存在积分/分佣 |

证据：
- [源分页/排序及库存更新 ProductSpuMapper.java:L69-L116](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/mysql/spu/ProductSpuMapper.java:69)。
- [源详情与访问副作用 AppProductSpuController.java:L82-L107](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/spu/AppProductSpuController.java:82)。
- [源分类入口 AppCategoryController.java:L35-L54](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/category/AppCategoryController.java:35)。
- [源SPU模型 ProductSpuDO.java:L93-L170](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/spu/ProductSpuDO.java:93)。
- [源SKU模型 ProductSkuDO.java:L48-L95](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/sku/ProductSkuDO.java:48)。
- [当前分页参数 StorePageReq.java:L16-L20](../../lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/req/StorePageReq.java:16)。
- [当前查询/可见性 ProductSpuMapper.xml:L46-L78](../../lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/resources/mapper/ProductSpuMapper.xml:46)。
- [当前详情字段 StoreDetailRes.java:L12-L20](../../lemnt-server/lemnt-product/lemnt-product-adapter/src/main/java/com/lemnt/product/adapter/app/product/res/StoreDetailRes.java:12)。
- [当前App分类查询 ProductCategoryGatewayImpl.java:L73-L76](../../lemnt-server/lemnt-product/lemnt-product-infrastructure/src/main/java/com/lemnt/product/infrastructure/product/gateway/ProductCategoryGatewayImpl.java:73)。

**边界**：源VO声明某字段，不代表Service/SQL实际使用。源分页VO的ids及详情vipPrice本次未核实业务赋值，不能仅按声明登记已实现。当前SPU价格/库存来自SKU聚合，也不能按DO缺少同名列就判定整个价格库存能力缺失。

### F05｜必须显式登记的既定差异：金额与销量口径

- 源商品价格采用整数分，当前部分商品接口采用人民币元字符串；转换应隔离在既有边界，不整体改写已确认金额协议。
- 源销量展示叠加真实销量和虚拟销量，且源码库存更新会反向增减salesCount；当前销量按已确认决定为“支付确认累计、售后不冲减”。二者**不是同一口径**。
- 当前Store*是本项目的前台商品读视图，不是源项目的自提门店或门店库存能力；迁移清单应使用“前台商品列表/详情”，不继续扩散门店语义。

证据：
- [源销量更新 ProductSpuMapper.java:L101-L116](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/mysql/spu/ProductSpuMapper.java:101)。
- [源App销量汇总 AppProductSpuController.java:L76-L78、L103-L106](../../../yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller/app/spu/AppProductSpuController.java:76)。
- [当前销量口径 lemnt_product.sql:L97](../../lemnt-server/lemnt-product/sql/lemnt_product.sql:97)。

**建议**：保留用户已单独确认的金额与销量口径作为有理由的适配差异；其他源字段和展示能力单独补齐。不自动撤销既有决定，也不再声称当前销量“完全等同芋道”。

### F06｜优先级P1：下单、权益与关单时序经过重构，不是原样迁移

- 源：创建前Handler处理商品库存和秒杀资源，随后保存订单/明细；创建后处理券、购物车和支付单等。
- 当前：先持久化订单，再由OrderRemoteService进行权益冻结、库存预占和预占结果流转；异常走关闭/补偿。
- 源取消订单的代表链是事务内资源恢复钩子、提交后异步关支付；当前资源恢复与支付关闭有独立事件消费者。

证据：
- [源建单 TradeOrderUpdateServiceImpl.java:L186-L205、L251-L281](../../../yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/TradeOrderUpdateServiceImpl.java:186)。
- [源商品库存钩子 TradeProductSkuOrderHandler.java:L26-L39](../../../yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/handler/TradeProductSkuOrderHandler.java:26)。
- [源秒杀库存钩子 TradeSeckillOrderHandler.java:L25-L61](../../../yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/handler/TradeSeckillOrderHandler.java:25)。
- [当前先落单 OrderSubmissionService.java:L18-L39](../../lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/service/OrderSubmissionService.java:18)。
- [当前预占编排 OrderRemoteService.java:L32-L79](../../lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/service/OrderRemoteService.java:32)。
- [当前权益处理 OrderBenefitsService.java:L37-L71](../../lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/service/OrderBenefitsService.java:37)。

**边界**：未核实源各Handler的确定排列、全部活动类型、退款和积分规则。不能凭注释把当前的进度查询当作同步写操作；不能只删除预占台账而不处理其调用方、失败恢复和旧订单状态。

### F07｜优先级P1：源支付有通知任务，当前另加通用Outbox/Inbox；必须分类收敛

源已核实的支付成功代表链：持久化通知任务 → 提交后异步HTTP通知业务接口 → 保存结果/重试时间。当前：资金成功事实 → PAY_SUCCESS Outbox → 订单Inbox消费 → ORDER_PAID事件 → 各服务消费/完成进度确认。

当前订单、支付DDL中明确有 `order_message_outbox/inbox`、`pay_message_outbox/inbox`；还存在 `pay_notify_task/log`、库存确认进度、优惠回退快照和钱包流水等不同用途的记录。

证据：
- [源支付通知 PayNotifyServiceImpl.java:L95-L128、L205-L295](../../../yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/service/notify/PayNotifyServiceImpl.java:95)。
- [当前资金通知 PaymentNotificationService.java:L28-L73](../../lemnt-server/lemnt-pay/lemnt-pay-application/src/main/java/com/lemnt/pay/application/payment/service/PaymentNotificationService.java:28)。
- [当前订单消息表 lemnt_order.sql:L468-L502](../../lemnt-server/lemnt-order/sql/lemnt_order.sql:468)。
- [当前支付消息表 lemnt_pay.sql:L236-L270](../../lemnt-server/lemnt-pay/sql/lemnt_pay.sql:236)。
- [当前支付通知任务/日志及资金流水 lemnt_pay.sql:L47-L67、L125-L159](../../lemnt-server/lemnt-pay/sql/lemnt_pay.sql:47)。

**纠正之前的笼统结论**：不能说“芋道没有MQ/事件”，也不能说“源支付没有持久化通知任务”。本次只确认上述代表支付路径；源注册发券还使用Spring进程内事件，MQ框架支持多个broker不等于本地业务实际启用了它们。

**建议**：遵守用户不默认保留本地消息表和已选RocketMQ的约束，逐事件设计调用及恢复依据，再确认Outbox/Inbox去留与必要代码变更。源支付通知任务不冒充资金流水；业务台账也不能仅因用于幂等就等同消息表。当前未授权删表、清积压或迁历史数据。

### F08｜已核实的正确归属：评价投稿在订单侧，不应再迁回商品

源会员投稿在trade订单入口，核验购买/完成事实后调用商品评价保存；自动评价也由交易任务编排。当前 `/mall/app/comments` 在order，product保留公开查询及已验证事实保存，方向已对齐。

证据：
- [源投稿 AppTradeOrderController.java:L197-L200](../../../yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app/order/AppTradeOrderController.java:197)。
- [源评价编排 TradeOrderUpdateServiceImpl.java:L914-L966、L1043-L1084](../../../yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/TradeOrderUpdateServiceImpl.java:914)。
- [当前入口 AppOrderCommentController.java:L26-L35](../../lemnt-server/lemnt-order/lemnt-order-adapter/src/main/java/com/lemnt/order/adapter/app/order/AppOrderCommentController.java:26)。
- [当前编排 OrderCommentService.java:L37-L135、L141-L205](../../lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/service/OrderCommentService.java:37)。

已确认用户投稿展示快照来自可信登录上下文，不为迁移重建product会员查询。后台自动评价没有会员请求线程；当前order仍有独立会员资料查询，需在调用清单中显式标明，不能把“从请求线程取昵称”的规则不加区分套到后台任务，也不能未经确认替换它。

### F09｜全量清单还需覆盖内容与统计，不能只复核现有313端点

源promotion有article/article-category，商城statistics另有会员、商品、支付、交易统计及前端API。当前定向搜索仅发现订单简要统计执行器，未找到同名商品统计/内容Controller；这属于**候选迁移缺口**，尚需按能力/页面/调用追踪，而不是按名称宣布整个模块缺失。

证据：
- [源内容 ArticleController.java:L26-L28](../../../yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller/admin/article/ArticleController.java:26)。
- [源商品统计 ProductStatisticsController.java:L38-L76](../../../yudao-cloud/yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/controller/admin/product/ProductStatisticsController.java:38)。
- [当前简要订单统计 OrderStatisticsQueryExe.java:L24-L32](../../lemnt-server/lemnt-order/lemnt-order-application/src/main/java/com/lemnt/order/application/order/executor/OrderStatisticsQueryExe.java:24)。

现有端点基线为313项（product57/order87/pay47/promotion122），它是当前项目内部契约，不是本地芋道完整能力总量，不拿该数字作为全量迁移验收标准。

### F10｜运行条件：已有配置源，但没有本轮真实运行证据

- 各服务application配置从Nacos导入公共/MQ/服务配置；product的数据源URL/账号/密码须由环境注入。
- 正式MQ消费者默认关闭。仅启动进程而未正确配置注册、订阅和必要任务，不能验收支付后处理及资源回滚。
- 小程序当前 `apiBaseUrl=''`、`memberApiReady=false`，服务请求要求HTTPS；本地验收必须明确域名/证书/开发者工具策略和业务开关，不能改成全局跳过认证或自动启用生产入口。
- 未检测本机中间件安装/运行状态，未核实Nacos配置是否已发布，也未连接业务数据库。

证据：
- [product启动入口 application.yaml:L1-L23](../../lemnt-server/lemnt-product/lemnt-product-start/src/main/resources/application.yaml:1)。
- [product数据源配置 lemnt-product.yaml:L4-L14](../../docs/config/lemnt-product.yaml:4)。
- [MQ配置 commerce-mq.yaml:L1-L10](../../docs/config/commerce-mq.yaml:1)。
- [小程序配置 config.js:L1-L3](../../lemnt-miniapp/config.js:1)。
- [小程序HTTPS及开关校验 storefront.js:L88-L96](../../lemnt-miniapp/services/storefront.js:88)。

## 3. 冲突处理的备选方案与权衡（待确认方向）

| 冲突 | 建议方案 | 备选方案及代价 |
|---|---|---|
| 当前路径与源路径不同，鉴权/调用方滞后 | 保留当前已确认路径，补齐路由、精确鉴权、前端地址、权限和契约测试 | 全量恢复源HTTP路径；影响面更大，且需用户再次确认路径变更，不作为默认 |
| 当前Store*视图裁剪源业务能力 | 在现有DDD/前端边界内恢复真实缺失能力与字段，建立源→目标映射；不把改类名当迁移 | 只保留裁剪模型、声明差异；短期更省但不能满足已确认的全量迁移目标 |
| 拆服务后时序与状态不同 | 先对齐源业务结果与规则，列出确实必要的服务适配及调用恢复，再定向收敛额外机制 | 连内部时序/状态也机械照搬；跨服务事务与失败窗口不同，需更大范围改造/验证，不能仅删台账 |
| 源口径与已单独确认的销量/金额口径不同 | 作为有明确依据的项目差异保留；源其他配置/展示能力单列补齐 | 全量回归源销量/分单位；必须重新得到用户对既定口径变更的确认，不能默认执行 |
| 无本地消息表诉求与现存消息链冲突 | 保持RocketMQ方向，先列消息、生产/消费/业务状态及恢复映射，再明确每张表和代码的处理清单 | 保留现有消息表仅作临时未收敛状态；虽改动少，但不能写成满足用户最终约束或迁移完成 |

## 4. 建议分批顺序（不是已批准实现计划）

1. 先固定源码引用和全量能力矩阵，同时把F01/F02这类确定运行阻塞单列。
2. 先修运行阻塞和必要的依赖隔离，不新增业务能力或全面改造消息链。
3. 对齐商品/分类/规格/前端真实消费字段，建立本地隔离运行所需配置和基础数据清单。
4. 以源业务规则验证下单、支付、关单、履约、售后、评价；额外机制去留先明确调用恢复方案。
5. 分批补齐其余营销/会员/统计/内容能力，区分源码已存在、当前缺失和外部渠道阻塞。
6. 分别记录静态盘点、自动化测试、真实本地服务、浏览器/小程序及外部沙箱验收，不混写结果。

## 5. 本检查点产出与后续门控

- 本文件与引用指纹清单为源码分析检查点产物，设计仍为in_progress。
- 用户确认这些分析发现后，再提供15章设计结构预览；结构确认后展开设计和精确实现计划。
- 当前不生成已批准 `design.md`，不将设计标记completed，不修改业务代码或原芋道源码。
