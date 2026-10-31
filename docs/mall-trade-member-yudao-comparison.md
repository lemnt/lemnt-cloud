# Mall / Trade / Member 与芋道功能对照

> 2026-10-28 业务收敛变更见 [当前接口变更](yudao-alignment-api.md)。本文中的旧兼容入口、独立 SKU 写接口及发布版本机制已退役。

核对日期：2026-10-28。仅做静态代码分析，没有修改业务实现、执行数据库变更或进行线上验收。

参考：用户给出的 [Gitee 芋道项目](https://gitee.com/zhijiantianya/yudao-cloud)，实际读取其 [GitHub 官方镜像](https://github.com/YunaiV/yudao-cloud)，固定提交 `8db9153bfe6bdc03baf93b74e244a2e74477c5c4`。当前工作区包含尚未提交的改动，以当前文件为准。

对照关系：本项目 mall 对应芋道 product + promotion + trade 中购物车/结算部分；trade 对应芋道 trade 订单/售后/物流及 pay 相关部分；member 对应芋道 member，登录在本项目 auth。不能按模块目录同名机械判断缺失。芋道 statistics 单独列为扩展范围。

“已有”表示存在相应代码，不等同于真实渠道已接通或端到端验收通过。下面不以 Controller 数或表数计算迁移百分比。

## 关键结论

1. **促销交易链路未接通**：满减/折扣主表及管理接口已有，`PromotionPricingService` 是独立算法，`PromotionUsageService` 有额度逻辑，但生产结算 `SettlementPreparationService.price()` 只调用 `SettlementRules.quote()` 计算商品和优惠券；未找到生产结算调用促销计价和 hold/use/release 的链路。管理接口实现不能表述为促销交易迁移完成。
2. **折扣业务并不等价**：芋道折扣为活动下 SKU 逐个配置减价/打折；当前是指定商品范围的统一门槛、支付比例及封顶金额，仍是订单范围折扣。
3. **退款状态闭环不等于权益闭环**：`PaymentRefundService.complete()` 更新退款、售后和订单状态，未见售后商品回补库存、退还消费积分/回收赠送积分经验及相关券权益处理。未支付关单的库存/锁券回滚已有，不能拿它证明售后退款处理已完成。
4. **真实渠道未接通**：支付创建目前只落本地支付单；退款和物流轨迹只有适配接口/路由，生产源码未找到 `PaymentRefundClient` / `ShipmentTrackClient` 的具体实现。
5. **会员基础管理较完整，交易权益缺失**：会员资料、地址、标签、分组、等级、签到及积分经验流水已有；等级折扣、积分抵扣、消费赠送、退款返还尚未和商城/交易串联。
6. **后台页面与接口进度不同**：接口覆盖明显大于页面覆盖，不能把已有接口算成管理页面已交付。

## Mall：未迁移或部分迁移

| 编号 | 功能 | 当前情况与差异 | 范围判断 |
|---|---|---|---|
| M01 | 满减、折扣参与真实结算 | 后台管理/算法已有；结算、活动额度占用、支付使用、关单释放未串联 | 已确认促销范围内，优先补齐 |
| M02 | SKU 级限时折扣 | 缺活动商品配置、SKU 独立减价/折扣及相应冲突校验；当前统一订单折扣不能替代 | 已确认折扣范围内 |
| M03 | 满减送完整规则 | 当前只支持按金额阶梯减钱；芋道还支持按件数条件、包邮、赠积分、赠券 | 规则扩展，需要确定是否纳入 |
| M04 | 优惠券模板管理 | 有创建/列表/启停；缺芋道的模板编辑、删除、详情/分页管理契约 | 已有优惠券管理范围内 |
| M05 | 优惠券发放与回收 | 当前目标会员发券，每模板每会员一张；缺批量发券、可配置限领张数、回收券、券分页/详情管理 | 已有优惠券管理范围内 |
| M06 | 券适用范围及有效期 | 当前固定起止时间、订单金额门槛；缺指定商品/类别等适用范围和领取后 N 天有效 | 规则扩展 |
| M07 | 商品完整展示/经营字段 | SPU/SKU 基础字段已有；缺轮播图、关键词/简介、排序、市场价/成本价、条码、重量/体积、配送方式/模板等 | 按实际商品页面选取，不能为对齐盲加字段 |
| M08 | 商品经营统计/导出 | 已有商品状态数量；缺芋道销量/浏览/虚拟销量相关管理和商品导出 | 统计/导出未迁移 |
| M09 | 评价查询与表现 | 后台分页/详情/显隐/回复及真实订单评价写入已有；缺小程序商品评价分页、图片评价、匿名、多维评分 | 已确认评价范围内；多维字段按页面取舍 |
| M10 | 后台自评 | 本项目只允许真实完成订单评价，没有芋道后台添加自评 | 属额外运营玩法，不建议直接补 |
| M11 | 商品收藏、浏览历史 | 小程序收藏/取消/列表/数量、历史浏览/删除/清空和后台查询均未迁移 | 原后台页面清单之外，扩展项 |
| M12 | 购物车批量操作 | 已有列表、最终数量设置、选中及单条删除；缺独立批量选中、批量删除、规格重置、数量汇总接口 | 小程序完善项；不恢复已移除的请求流水表 |
| M13 | 小程序优惠券详情/数量 | 有领券中心和本人钱包；缺单张券详情、未使用数量等独立接口 | 小程序完善项 |
| M14 | 其他促销玩法 | 秒杀/时段、拼团、砍价及助力、积分商城未迁移 | 超出当前只确认满减/折扣范围，不自动当欠账 |
| M15 | 商城运营内容 | Banner、文章/文章分类、装修页面/模板、客服会话与消息未迁移 | 超出原管理页面范围 |
| M16 | 促销展示入口 | 未见芋道商品正在参与活动信息、满减活动小程序展示等接口 | 促销面向用户的链路缺口 |

已有不重复列为缺失：分类/品牌基础管理、规格/规格值、SPU/SKU 基础管理、商品回收与恢复、库存调整及历史、优惠券基本领取和锁定/核销、Redis 结算单。

芋道 property/property-value 在当前 SKU 组合语境下对应已实现的 ProductSpec/ProductSpecValue，名称不同不算缺失；用户已要求不新增占位 ProductAttribute 模型或表。

主要参考：[商品 Controller](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/controller)、[促销 Controller](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/controller)、[折扣商品模型](https://github.com/YunaiV/yudao-cloud/blob/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/discount/DiscountProductDO.java)。

## Trade：未迁移或部分迁移

| 编号 | 功能 | 当前情况与差异 | 范围判断 |
|---|---|---|---|
| T01 | 支付渠道实际下单 | 本地支付单创建/查询已有；没有实际渠道支付请求、收银台参数返回及完整查询同步实现 | 支付单交易闭环缺口 |
| T02 | 原路退款渠道 | 售后关联退款单、额度校验、状态机及防重复已有；没有真实退款渠道客户端 | 支付退单交易闭环缺口 |
| T03 | 物流轨迹渠道 | 已有物流单分页/详情/轨迹接口，缺真实承运商适配 | 物流管理范围内 |
| T04 | 售后成功后的库存/权益 | 缺按售后商品恢复库存以及积分/经验/券权益联动；不能复用仅面向 CLOSED 的整单补偿代替 | 售后交易闭环缺口，优先核对规则 |
| T05 | 订单调价、收货地址修改 | 已有订单分页/详情/备注/关单/发货/后台收货；缺芋道调价和修改收货地址 | 后台订单管理差异 |
| T06 | 运费计价 | 未见按地区、重量/件数/体积计费模板及满额包邮接入结算 | 跨 mall 结算与 trade 配送 |
| T07 | 快递公司管理 | 已有物流单承运商编码/运单号；缺快递公司字典 CRUD、分页、选择及导出 | 原“物流单页面”不等于已授权新增“快递公司页面” |
| T08 | 自提及核销 | 缺自提门店、店员、核销码/核销订单、配送方式选择 | 原范围之外，扩展项 |
| T09 | 交易配置 | 缺售后原因列表、运费及自提配置统一管理；已有超时关单固定 30 分钟逻辑，非完整配置中心 | 可按现有订单页面需求补 |
| T10 | 小程序订单中心 | 缺本人订单分页/分类数量、取消、删除、确认收货、订单项详情及物流查询完整接口；当前仅有结算回执/按请求找回、本地支付状态 | 后台优先后的小程序欠项 |
| T11 | 小程序售后中心 | 当前商品售后流程由后台 web 接口操作；缺本人申请、分页/详情、取消、退货提交及售后日志的 app 接口 | 后台核心状态流程已有，小程序未迁移 |
| T12 | 自动收货/评价及操作日志 | 已有超时关单任务；缺芋道自动收货、自动评价任务，以及订单/售后操作日志展示能力 | 生命周期完善项 |
| T13 | 分销体系 | 推广关系、佣金记录/冻结、分销统计、提现及转账均未迁移 | 原范围之外，扩展项 |
| T14 | 支付平台扩展 | PayApp、商户渠道配置 CRUD、钱包充值/交易、转账、通知任务管理等未迁移 | 用户明确支付配置只展示支持渠道，不要求复制完整 pay 服务 |
| T15 | 交易/支付经营报表 | 已有简单订单数量/退款统计；缺芋道独立 statistics 的趋势、排行、经营分析 | 原范围之外，不与简单统计混淆 |

已有不重复列为缺失：同步下单预占库存和校验锁券、支付确认库存及核券、未支付关单统一异步补偿、订单分页/详情/备注、登记发货、后台收货、商品部分退款、发货后售后、退货退款审批及退款交易模型。退款模型存在不代表 T02/T04 已完成。

芋道参考：[后台订单](https://github.com/YunaiV/yudao-cloud/blob/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/admin/order/TradeOrderController.java)、[小程序交易](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/controller/app)、[订单联动处理器](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/handler)、[支付模块](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-pay)。

## Member：未迁移或部分迁移

| 编号 | 功能 | 当前情况与差异 | 范围判断 |
|---|---|---|---|
| U01 | 交易积分/经验联动 | 有积分配置、积分经验调整、流水及自动升级；缺下单抵扣、消费赠送、退款退还/扣回的交易接入 | 会员配置尚未生效到交易 |
| U02 | 等级折扣生效 | 等级 discountPercent 已有；结算未使用该折扣 | 会员权益闭环缺口 |
| U03 | 完整会员档案/筛选 | 当前有基础资料、标签、分组和状态；缺芋道真实姓名、地区、备注、注册来源/IP、最近登录信息等档案能力，相应筛选也未完整对应 | 按会员页面需求选择字段，不能为对齐盲加 |
| U04 | 手机修改/找回密码 | 修改原密码已有，后台重置已有；缺本人验证后换手机号、忘记密码找回 | 芋道依赖短信/微信的方案已被用户排除，不应直接搬回 |
| U05 | 社交账号 | 绑定/解绑、社交登录、小程序手机号授权、小程序码、订阅消息、JS SDK 签名未迁移 | 用户明确移除短信和微信，属于排除项 |

已有不重复列为缺失：会员分页/详情/更新/状态、分组、标签、等级、积分经验调整及流水、等级记录、签到规则/签到统计/每日签到、地址簿。密码登录、刷新、退出在 auth，不能因为 member 中没有认证 Controller 就判定缺失。

参考：[会员后台/小程序接口](https://github.com/YunaiV/yudao-cloud/tree/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/controller)、[交易会员积分处理器](https://github.com/YunaiV/yudao-cloud/blob/8db9153bfe6bdc03baf93b74e244a2e74477c5c4/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/order/handler/TradeMemberPointOrderHandler.java)。

## 相对芋道多出的内容

“多出”仅指对照提交中没有同等业务入口/机制，不代表应该删除。如下三类必须分开。

### 可见业务入口与重复入口

| 模块 | 多出内容 | 证据与性质 | 判断 |
|---|---|---|---|
| mall | 后台查看/修改/删除指定会员购物车 | `adapter.web.cart.CartController`，还有 `MemberCartPage.tsx`；芋道购物车主要为本人 app 接口 | 超出用户给出的后台九类页面，属于优先清理候选 |
| mall | 后台代会员结算、直接创建订单 | `MallOrderPlacementController`、`SettlementQuoteController`；芋道后台订单 Controller 不提供同等创建入口 | 非原后台页面需求，保留前应有业务依据 |
| mall | 通用促销兼容管理入口 | `/mall/admin/promotions` 与两个分类型 Controller 并存 | 重复管理入口，兼容期结束可收敛 |
| mall | 独立 SKU 创建/修改/上下架/删除与旧基础商品 API | 芋道主要通过 SPU 管理 SKU；本地 `ProductController`、`ProductSkuController`、`ProductSkuManagementController` 并存 | 一部分是颗粒度差异，一部分旧兼容入口；先查调用者再清理 |
| mall | 单独库存调整流水与管理查询 | `StockAdjustment`、库存调整历史接口；芋道本次商品管理入口无同等独立操作账本 | 追溯扩展，可按库存管理实际需要保留 |
| mall | 两个设置购物车数量入口 | `/mall/app/cart/{productId}/add` 已改为最终数量，与 PUT `/mall/app/cart/{productId}` 职责重叠 | 可收敛为一个语义清楚的入口，不能再按增量加购宣传 |
| trade | 老整单退款申请与新商品售后入口并存 | `OrderRefundRequestController`/`RefundRequestController` 与 `AfterSaleController`；共用既有退款存储，不是两套完全独立财务账 | 重复/兼容业务路径，可逐步统一商品售后 |
| trade | 原始订单详情与履约详情并存 | `/trade/orders/{orderNo}` 与后台履约详情 | 投影差异，管理入口可以统一；不能仅凭 Controller 多就删除 |
| trade | 后台直接登记收货 | `ShipmentController.receive`；芋道主要为本人确认和系统自动收货 | 实际业务差异，应明确是否允许后台代确认 |
| trade | 单独物流单分页/详情管理入口 | 芋道主要从订单管理查询物流轨迹；本地独立 Shipment 查询 | 用户已明确要求物流单管理，属于应保留扩展 |
| member | 后台创建会员、直接重置密码 | `/member-users` POST、`/{id}/password`；芋道本次后台用户 Controller 无同等创建/重置入口 | 业务扩展，是否保留按运营需求 |
| member | 兼容会员创建/详情接口 | `/members/register`、`/members/{id}` 与 `/member-users` 重叠 | 明显兼容冗余，优先查调用并收敛 |
| member | 后台直接调整经验、指定会员流水查询 | `MemberUserExperienceController` 和用户维度记录入口；芋道后台有全局流水及积分/等级调整，未提供同等经验调整接口 | 管理扩展，不是经验流水功能本身多出 |
| member | 全量标签/分组列表、单独分配标签/分组、额外流水详情入口 | 芋道部分能力在会员更新/分页内完成，本地拆成独立接口 | 主要是接口组织差异，不算新业务，避免机械删除 |
| member | memberNo 业务会员编号 | 本地 `Member` 除 ID 外有独立 memberNo，芋道参考 MemberUserDO 无该字段 | 数据契约扩展；邮箱不是扩展，芋道也有 email |

### 额外技术机制

| 模块 | 内容 | 对照结论 |
|---|---|---|
| mall | 不可变促销发布版本、revision CAS、算法 hash、草稿/发布/暂停/归档 | 芋道主要为活动业务表直接管理；本地增加版本及并发机制，不是新增促销玩法 |
| mall | 活动总限次、会员限次、HELD/USED/RELEASED 使用历史及事件 | 相对参考满减/折扣活动主模型属于额外额度机制；当前还没有接入真实结算，不应因表/测试存在就当功能已上线 |
| mall | Redis 结算预览、提交上下文、放弃标记、请求恢复 | 用户明确要求结算单入 Redis；保留，不能以“不像芋道”建议删掉 |
| trade | RESERVING 中间状态、库存确认标记、按订单/SKU 预占、统一库存与券补偿消息 | 属于本项目跨服务事务实现，用户明确同步下单和异步回滚边界；不是重复业务功能 |
| member | 积分和经验统一资产流水存储、业务编号防重复 | 与芋道分开的积分/经验记录模型不同，属于存储组织差异；不把“表不同”当业务欠账 |

### 明确要求保留的架构差异

- 认证在 auth，网关统一 token 校验，小程序/后台授权分开；auth 无数据库，密码在 member_user。
- mall 负责商品/库存、购物车、结算、券、促销，trade 负责订单/支付/售后/物流；库存归商品域，不为参考项目机械增设服务。
- 下单同步预占库存、校验并锁券，支付后核券；未支付关单统一异步库存和券回滚。不改回参考项目其他处理时点。
- 不恢复 outbox、mall_cart_add_request、结算 SQL 辅助表或独立公开领券配置表；不恢复短信和微信。
- 支付配置只展示支持渠道，不为比较结果自动增建完整支付平台配置中心。

## 管理前端差异

已找到 mall 商品、分类、品牌、券模板、会员购物车，以及 trade 订单履约、退款申请等页面。尚未找到与下列新增接口对应的独立完整页面：规格/规格值、商品评价、两类促销活动、优惠券实例管理、商品售后完整流程、独立物流单、支付渠道展示、支付单、支付退款单。

页面缺失与后台接口缺失分别计算；部分现有详情页有展示字段，不代表独立管理流程已经完成。会员已有用户/元数据/流水/配置页面，本次没有据此宣称逐项 UI 已与芋道验收一致。

## 建议处理顺序

1. 先清理确认为原后台范围之外或重复的入口：后台会员购物车、后台代下单、通用促销兼容入口、旧会员创建/详情、旧整单退款兼容路径。清理前查调用，不在本次分析中自动删除。
2. 优先完成现有范围的真实闭环：促销接结算，确认 SKU 折扣业务模型，退款后库存/权益规则，真实支付/退款/物流适配。
3. 补齐已有后台页面范围所需接口：券模板/券实例管理、商品评价公开查询、订单调价/地址修改等按页面确认；同步实际后台页面。
4. 会员权益与交易接通后再扩展小程序订单/售后中心。
5. 秒杀、拼团、砍价、分销、装修、客服、钱包和经营报表不自动加入迁移计划。

## 本地证据入口

- 生产结算：`lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/settlement/service/SettlementPreparationService.java` 的 `price()`。
- 促销算法：`lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/promotion/service/PromotionPricingService.java`；额度：application/promotion/service/PromotionUsageService.java。
- 退款完成：`lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/PaymentRefundService.java` 的 `complete()`。
- 关单补偿：`lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderRemoteService.java` 的 `closed()`，限定 CLOSED。
- 未接入的渠道路由：trade infrastructure 的 `PaymentRefundChannelGatewayImpl`、`ShipmentTrackGatewayImpl`。
- 会员配置/等级：member domain/growth/model/Config.java、Level.java；结算没有相应计价调用。
- Controller 静态清单：`work-items/member-domain-structure/evidence/yudao-feature-comparison-20261028.json`，本地 76 个 Controller，对照包含 product/promotion/trade/statistics/member/pay 112 个 Controller。数量不用于推算覆盖率。

本次未运行测试；结论来自 Controller、领域模型、应用流程、渠道实现搜索和管理前端文件检查，不沿用之前“测试通过”作为迁移完整性的证明。
