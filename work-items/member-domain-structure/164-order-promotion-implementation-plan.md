# 第164批实现计划：订单优惠与按数量部分退款

2026-11-02。对应[设计](/Users/wangxx/demo/lemnt-cloud/work-items/member-domain-structure/164-order-promotion-refund-design.md)及[DDL草案](/Users/wangxx/demo/lemnt-cloud/work-items/member-domain-structure/164-order-promotion-model.sql)。本文件为待实施计划，以下文件并未因本次设计而完成开发。复杂度：★局部、★★跨对象、★★★跨事务/服务。

## 实施顺序与通过条件

| 批次 | 工作 | 依赖 | 通过条件 |
|---|---|---|---|
| P1 数据与领域 | 增量DDL、快照模型、优惠分摊、数量汇总及持久化 | 无 | 新/旧结构兼容；金额及数量约束；完整快照往返不丢字段 |
| P2 内部契约与报价 | 扩展可信报价、稳定行标识和退款权益方案；所有转换和旧构造兼容 | P1 | 报价→订单→优惠头/行分摊守恒，旧消息可读，新消息完整 |
| P3 订单与售后业务 | 原子保存快照、支付前调价、创建退单固定单价末次尾差、同订单单笔处理中 | P1、P2 | 10元6件退2/3/1件得3.32/4.98/1.70；生成后不重算；只保存成功已退计数 |
| P4 退款与可靠执行 | 零现金售后、本地成功与任务原子、租约及版本、下游精确幂等 | P3 | 未退完数量不整单结束；故障重放不重复退现金/积分/库存 |
| P5 查询和页面 | 促销子资源、现有可售后行计数、管理端/小程序详情及申请交互 | P3、P4 | 请求不传金额，页面显示后台冻结值，历史未知和权益处理中可辨 |
| P6 迁移和验收 | 盘点映射、核对计数、灰度路由、真实环境故障与退款验收 | P1至P5 | 无猜测迁移；全量核对无越界；真实HTTP与渠道/账户证据单列 |

P1/P2提供向后兼容契约后先部署消费者，再部署生产者；P4切换任务执行器前停止旧执行器，禁止新旧领取代码混跑。P6执行前完成DDL、数据映射和回退方案审查。本轮没有执行这些部署或数据库动作。

## 文件清单

以下为确定的核心变更点。新增类型须按对应六层补齐Converter/DO/Mapper与测试；新增文件名列在下方，不因已有Service存在而绕开分层。所有新增接口/方法/复杂规则写中文注释；修改后不commit。

### P1：数据层与领域

| 文件（精确路径） | 操作 | 复杂度/内容 |
|---|---|---|
| [V18__order_promotion_snapshot.sql](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/sql/V18__order_promotion_snapshot.sql) | 新增，实施时再次核对版本号 | ★★ 正式增量DDL；不能直接把工作项草案当已执行脚本 |
| [TradeOrder.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrder.java) | 修改 | ★★ 金额汇总、完整性/版本 |
| [TradeOrderItem.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrderItem.java) | 修改 | ★★ 行优惠/积分及已退数量；不加退款中数量或退款单价字段 |
| [AfterSale.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/model/AfterSale.java) | 修改 | ★★ 冻结优惠归因、积分和退款规则版本 |
| [OrderPromotion.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotion.java) | 新增 | ★★ 订单优惠领域结果 |
| [OrderItemPromotion.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderItemPromotion.java) | 新增 | ★★ 商品行分摊 |
| [OrderPromotionRefund.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotionRefund.java) | 新增 | ★★ 退单优惠归因 |
| [OrderPromotionGateway.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/gateway/OrderPromotionGateway.java) | 新增 | ★★ 保存、批量读及退款归因边界 |
| [OrderPromotionGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/gateway/OrderPromotionGatewayImpl.java) | 新增 | ★★ 三表持久化，普通CRUD复用BaseMapper |
| [TradeOrderDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderDO.java) | 修改 | ★ 新增汇总字段 |
| [TradeOrderItemDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderItemDO.java) | 修改 | ★ 新增分摊及计数字段 |
| [AfterSaleDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/dataobject/AfterSaleDO.java) | 修改 | ★ 冻结方案字段 |

三表DO和Mapper各新增一个，精确路径如下（均★★）：

- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionDO.java`
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderItemPromotionDO.java`
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionRefundDO.java`
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionMapper.java`
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderItemPromotionMapper.java`
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionRefundMapper.java`

补齐已有Converter、Gateway的所有构造调用；行锁SQL放resources/mapper根目录，按项目规则flushCache=true/useCache=false。新增行计数只能在持锁、CAS状态转换的本地事务中维护，不能提供任意设置接口。

### P2：内部API与报价

| 文件 | 操作 | 复杂度/内容 |
|---|---|---|
| [OrderCreateBo.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/bo/OrderCreateBo.java) | 修改 | ★★ 报价快照、版本与内容指纹，旧构造兼容 |
| [OrderItemDTO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/dto/OrderItemDTO.java) | 修改 | ★★ 稳定商品行标识与分摊结果 |
| [SettlementQuote.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/settlement/model/SettlementQuote.java) | 修改 | ★★ 实际生效优惠及必要计算因子 |
| [ActivityPricingService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/ActivityPricingService.java) | 修改 | ★★★ 记录实际采用来源，避免会员价/活动价双算 |
| [SettlementDeliveryService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/settlement/service/SettlementDeliveryService.java) | 修改 | ★★ 同时保存原始运费与减免结果 |

所有券、积分、会员价、赠送规则的计算阶段和mall→trade转换都须检查；不只扩一个DTO后填默认0。新版退款RPC传售后号、orderItemId、SKU、数量及冻结积分方案；保留无版本旧消息兼容路径，待P6排空后另批清理。

### P3：订单与售后业务

| 文件 | 操作 | 复杂度/内容 |
|---|---|---|
| [OrderSubmissionService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderSubmissionService.java) | 修改 | ★★★ 订单、明细、促销同事务写入及完整内容幂等 |
| [OrderAdjustmentService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderAdjustmentService.java) | 修改 | ★★★ 调价差额、行分摊及快照版本；保留支付单生成后禁止调价 |
| [AfterSaleService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/service/AfterSaleService.java) | 修改 | ★★★ 锁订单后按order_no查处理中；无则计算并冻结；同请求幂等 |
| [AfterSaleGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/gateway/AfterSaleGatewayImpl.java) | 修改 | ★★ 复用activeRefund查询；所有类型状态转换统一维护已有活动标记 |
| [AfterSaleConverter.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/converter/AfterSaleConverter.java) | 修改 | ★★ 创建时补齐所有处理中售后的已有active_order_no，结束清空 |
| [AfterSaleRefundCalculator.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/service/AfterSaleRefundCalculator.java) | 新增 | ★★ 无框架依赖的单件基准/末次尾差及积分方案计算，仅创建时调用 |

不要在审批或支付退款执行器里调用计算器；不要将计算器做成可变配置通用规则引擎。退单amount是应退现金冻结值，支付退款表保存实际渠道执行结果。

### P4：退款执行与权益

| 文件 | 操作 | 复杂度/内容 |
|---|---|---|
| [PaymentRefundService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/PaymentRefundService.java) | 修改 | ★★★ 读取冻结金额；零现金售后；数量及金额双重判断；成功同事务增加已退数量和入任务 |
| [RefundBenefitsService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/RefundBenefitsService.java) | 修改 | ★★★ 支持以售后事实驱动，无现金无需伪造支付退款 |
| [PayNotifyType.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/payment/model/PayNotifyType.java) | 修改 | ★ AFTER_SALE类型，旧REFUND任务兼容 |
| [PayNotifyDispatcher.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyDispatcher.java) | 修改 | ★★ 按售后号读取已完成方案并派发 |
| [PayNotifyTaskService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyTaskService.java) | 修改 | ★★★ 只入库与提交后执行分开；租约恢复与版本CAS |
| [PayNotifyTaskMapper.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/payment/mapper/PayNotifyTaskMapper.java) | 修改 | ★★★ 租约与单调版本，人工重置不会复用旧执行令牌 |
| [OrderBenefitsGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/OrderBenefitsGatewayImpl.java) | 修改 | ★★ 发送明确行及权益方案 |
| [SettlementLifecycleClientImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/dubbo/SettlementLifecycleClientImpl.java) | 修改 | ★★★ 新协议不重算积分；库存执行与去重保持mall本地事务 |
| [PromotionOrderService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/PromotionOrderService.java) | 修改 | ★★★ 保留活动执行；区分新版确定方案和历史兼容计算 |
| [MemberTradeChange.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/member/model/MemberTradeChange.java) | 修改 | ★★ 明确应返积分及应回收赠积分 |
| [MemberTradeAssetBo.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-member/lemnt-member-client/src/main/java/com/lemnt/member/client/bo/MemberTradeAssetBo.java) | 修改 | ★★ 版本兼容与明确权益增减 |
| [MemberTradeAssetCmd.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/growth/cmd/MemberTradeAssetCmd.java) | 修改 | ★★ 接入冻结方案及账户流水幂等 |

同步Gateway接口、任务领域模型/DO/Converter及会员资产执行器的相关调用，不允许只改外层BO而内层继续按当前金额比例重算。mall本地行映射如需新增列，另提供该库增量脚本，不建立跨库FK。

### P5：查询与页面

新增查询执行器：

- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/OrderPromotionDetailQueryExe.java`（新增★★）
- `/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/AfterSalePromotionDetailQueryExe.java`（新增★★）

HTTP在已有app/web Controller增添设计规定的只读子资源，分别转换App/Web Res；复用既有权限和归属校验，不直接调用Mapper。不为列表逐单同步RPC查询。新增Application DTO及Adapter Res只包含设计规定的展示字段。

| 文件 | 操作 | 复杂度/内容 |
|---|---|---|
| [AfterSaleItemDTO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/dto/AfterSaleItemDTO.java) | 修改 | ★★ 仅加已退数量；有处理中退单时可申请数量为0；保留数组形态 |
| [types.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/types.ts) | 修改 | ★ 类型与可空历史值 |
| [fulfillmentApi.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/api/fulfillmentApi.ts) | 修改 | ★★ 新只读子资源；同步decoder |
| [OrderFulfillmentPage.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/OrderFulfillmentPage.tsx) | 修改 | ★★ 订单优惠来源及行分摊 |
| [AfterSaleDetailPanel.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/AfterSaleDetailPanel.tsx) | 修改 | ★★ 冻结退款、优惠归因及权益状态 |
| [RefundRequestPage.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/RefundRequestPage.tsx) | 修改 | ★★ 售后列表展示本次数量；订单详情展示已退/可申请数量；有当前处理单时由服务端禁止新申请 |
| [trade.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/services/trade.js) | 修改 | ★★ 同步只读接口与字段兼容 |
| [after-sale-create/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-create/index.js) | 修改 | ★★ 数量选择与提交后采用服务端冻结金额 |
| [after-sale-detail/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-detail/index.js) | 修改 | ★★ 本次退款结果和权益进度，联动对应wxml |
| [order-detail/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/order-detail/index.js) | 修改 | ★★ 优惠来源及行计数，联动对应wxml |

页面预览不能伪装最终退款金额：若尚未申请，展示预计；申请成功后只能显示退单amount。当前设计不增加独立预览接口，避免两次计算承诺不一致；真正创建仍需服务端持锁校验。

### P6：核对、测试与配置

1. 按设计13/14章建立单元、Mapper/MySQL、HTTP、管理端及小程序验收；新增计算器测试必须能区分旧剩余均摊和新固定单件算法。覆盖重复消息、回滚、零现金行、整单数量判断、计数回填未知值。
2. Maven从 `/Users/wangxx/demo/lemnt-cloud/lemnt-server/pom.xml` 聚合构建，覆盖trade/mall/member及受影响客户端；前端沿用各自package.json脚本。历史已通过测试不作为本批验证证据。
3. 正式迁移清单须含：原库备份依据、映射清单/异常行、旧活动任务排空、历史多笔处理中退单排空或核对、维护窗口、高水位、头行及售后计数校验、新旧协议切换、回退路由。禁止按当前商品/活动补造历史快照。
4. 对原订单支付、部分售后、全部售后、券和积分及佣金做真实环境逐笔对账，分别记录代码测试、HTTP验证、真实支付渠道证据；缺少哪层就明确标未验证。
5. 发布说明写入 `/Users/wangxx/demo/lemnt-cloud/docs/order-promotion-refund-migration.md`（新增★★），交付报告写入 `/Users/wangxx/demo/lemnt-cloud/work-items/member-domain-structure/164-order-promotion-refund-delivery.md`（新增★，只在实施完成后生成）。

本批不新增Redis/MQ中间件。原任务租约预算需与实际RPC超时核对；所有配置密钥沿用运行环境注入，不进入文档或仓库。
