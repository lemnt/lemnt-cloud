# 第164批任务清单

状态：72项核心任务代码与本地验证完成；目标数据库执行和真实环境验收单列未验证，见第164批交付报告。
未使用模板文件：仓库没有templates/task_template.md，采用cs-task内置表格格式。

来源：[设计](164-order-promotion-refund-design.md)、[实现计划](164-order-promotion-implementation-plan.md)。路径相对于项目根目录 `/Users/wangxx/demo/lemnt-cloud`；每项均可对应具体文件和验证目标。耗时是人工开发复杂度的粗略估计，不是执行承诺，不含外部环境等待。

## 必须保持的业务约定

- 订单明细保存优惠总额及refunded_quantity；不保存退款中数量，也不新增退款单价字段。
- 同事务锁订单后按退单已有order_no查处理中状态；有则拒绝新申请，同requestNo幂等返回。
- 生成退单时固定单件现金基准，末次结清原行实付减历史成功退款的余额；审核/执行/重试不重算。
- trade保存优惠结果；mall保留活动执行及库存幂等；历史未知保持NULL。
- 业务代码、数据库执行和真实环境验收分别留证，不commit。

## 文件任务及依赖

清单列出当前设计明确的核心文件和已核对的联动文件。对应转换、DTO/Res、Gateway接口、RPC实现及其构造调用必须在同一任务内闭合；实施前按实际引用核对，不允许漏字段或擅自扩业务范围。测试在相应业务批次随实现运行，P6表示最终集中核对，不是推迟编译或必要回归。

### P1 数据与领域

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 1 | [lemnt-server/lemnt-trade/sql/V18__order_promotion_snapshot.sql](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/sql/V18__order_promotion_snapshot.sql) | 新增 | ★★☆ | 无 | 20–45min | 新库V18建三表，已有库独立upgrade增量DDL；未执行真实库 | ✅ 完成（代码/本地） |
| 2 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrder.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrder.java) | 修改 | ★★☆ | 1 | 20–45min | 金额汇总、完整性/版本 | ✅ 完成（代码/本地） |
| 3 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrderItem.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/order/model/TradeOrderItem.java) | 修改 | ★★☆ | 1 | 20–45min | 行优惠/积分及已退数量；不加退款中数量或退款单价字段 | ✅ 完成（代码/本地） |
| 4 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/model/AfterSale.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/model/AfterSale.java) | 修改 | ★★☆ | 1 | 20–45min | 冻结优惠归因、积分和退款规则版本 | ✅ 完成（代码/本地） |
| 5 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotion.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotion.java) | 新增 | ★★☆ | 1 | 20–45min | 订单优惠领域结果 | ✅ 完成（代码/本地） |
| 6 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderItemPromotion.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderItemPromotion.java) | 新增 | ★★☆ | 1 | 20–45min | 商品行分摊 | ✅ 完成（代码/本地） |
| 7 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotionRefund.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/model/OrderPromotionRefund.java) | 新增 | ★★☆ | 1 | 20–45min | 退单优惠归因 | ✅ 完成（代码/本地） |
| 8 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/gateway/OrderPromotionGateway.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/gateway/OrderPromotionGateway.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7 | 20–45min | 保存、批量读及退款归因边界 | ✅ 完成（代码/本地） |
| 9 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderDO.java) | 修改 | ★☆☆ | 1,2,3,4,5,6,7 | 10–20min | 新增汇总字段 | ✅ 完成（代码/本地） |
| 10 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderItemDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/dataobject/TradeOrderItemDO.java) | 修改 | ★☆☆ | 1,2,3,4,5,6,7 | 10–20min | 新增分摊及计数字段 | ✅ 完成（代码/本地） |
| 11 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/dataobject/AfterSaleDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/dataobject/AfterSaleDO.java) | 修改 | ★☆☆ | 1,2,3,4,5,6,7 | 10–20min | 冻结方案字段 | ✅ 完成（代码/本地） |
| 12 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionDO.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 13 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderItemPromotionDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderItemPromotionDO.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 14 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionRefundDO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/dataobject/OrderPromotionRefundDO.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 15 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionMapper.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionMapper.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 16 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderItemPromotionMapper.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderItemPromotionMapper.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 17 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionRefundMapper.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/mapper/OrderPromotionRefundMapper.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 18 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/converter/OrderItemConverter.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/converter/OrderItemConverter.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17 | 20–45min | 补齐行金额、积分、已退计数转换并兼容历史NULL | ✅ 完成（代码/本地） |
| 19 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/gateway/OrderPromotionGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/promotion/gateway/OrderPromotionGatewayImpl.java) | 新增 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18 | 20–45min | 三表持久化，普通CRUD复用BaseMapper | ✅ 完成（代码/本地） |
| 20 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/TradeOrderGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/TradeOrderGatewayImpl.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18 | 20–45min | 订单汇总快照持久化及完整性校验 | ✅ 完成（代码/本地） |
| 21 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/TradeOrderItemGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/TradeOrderItemGatewayImpl.java) | 修改 | ★★★ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18 | 45–90min | 已退数量在首次售后成功事务内更新，重复状态转换不累加 | ✅ 完成（代码/本地） |

### P2 内部契约与报价

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 22 | [lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/bo/OrderCreateBo.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/bo/OrderCreateBo.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21 | 20–45min | 报价快照、版本与内容指纹，旧构造兼容 | ✅ 完成（代码/本地） |
| 23 | [lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/dto/OrderItemDTO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-client/src/main/java/com/lemnt/trade/client/dto/OrderItemDTO.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21 | 20–45min | 稳定商品行标识与分摊结果 | ✅ 完成（代码/本地） |
| 24 | [lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/settlement/model/SettlementQuote.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/settlement/model/SettlementQuote.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21 | 20–45min | 实际生效优惠及必要计算因子 | ✅ 完成（代码/本地） |
| 25 | [lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/ActivityPricingService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/ActivityPricingService.java) | 修改 | ★★★ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24 | 45–90min | 记录实际采用来源，避免会员价/活动价双算 | ✅ 完成（代码/本地） |
| 26 | [lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/settlement/service/SettlementDeliveryService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/settlement/service/SettlementDeliveryService.java) | 修改 | ★★☆ | 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24 | 20–45min | 同时保存原始运费与减免结果 | ✅ 完成（代码/本地） |

### P3 订单与售后业务

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 27 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/service/AfterSaleRefundCalculator.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/aftersale/service/AfterSaleRefundCalculator.java) | 新增 | ★★☆ | 22,23,24,25,26 | 20–45min | 无框架依赖的单件基准/末次尾差及积分方案计算，仅创建时调用 | ✅ 完成（代码/本地） |
| 28 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/gateway/AfterSaleGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/gateway/AfterSaleGatewayImpl.java) | 修改 | ★★☆ | 22,23,24,25,26,27 | 20–45min | 复用activeRefund查询；所有类型状态转换统一维护已有活动标记 | ✅ 完成（代码/本地） |
| 29 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/converter/AfterSaleConverter.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/aftersale/converter/AfterSaleConverter.java) | 修改 | ★★☆ | 22,23,24,25,26,27 | 20–45min | 创建时补齐所有处理中售后的已有active_order_no，结束清空 | ✅ 完成（代码/本地） |
| 30 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderSubmissionService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderSubmissionService.java) | 修改 | ★★★ | 22,23,24,25,26,27,28,29 | 45–90min | 订单、明细、促销同事务写入及完整内容幂等 | ✅ 完成（代码/本地） |
| 31 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderAdjustmentService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/order/service/OrderAdjustmentService.java) | 修改 | ★★★ | 22,23,24,25,26,27,28,29 | 45–90min | 调价差额、行分摊及快照版本；保留支付单生成后禁止调价 | ✅ 完成（代码/本地） |
| 32 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/service/AfterSaleService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/service/AfterSaleService.java) | 修改 | ★★★ | 22,23,24,25,26,27,28,29 | 45–90min | 锁订单后按order_no查处理中；无则计算并冻结；同请求幂等 | ✅ 完成（代码/本地） |

### P4 退款与可靠执行

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 33 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/payment/model/PayNotifyType.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/payment/model/PayNotifyType.java) | 修改 | ★☆☆ | 27,28,29,30,31,32 | 10–20min | AFTER_SALE类型，旧REFUND任务兼容 | ✅ 完成（代码/本地） |
| 34 | [lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/member/model/MemberTradeChange.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-domain/src/main/java/com/lemnt/mall/domain/member/model/MemberTradeChange.java) | 修改 | ★★☆ | 27,28,29,30,31,32 | 20–45min | 明确应返积分及应回收赠积分 | ✅ 完成（代码/本地） |
| 35 | [lemnt-server/lemnt-member/lemnt-member-client/src/main/java/com/lemnt/member/client/bo/MemberTradeAssetBo.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-member/lemnt-member-client/src/main/java/com/lemnt/member/client/bo/MemberTradeAssetBo.java) | 修改 | ★★☆ | 27,28,29,30,31,32 | 20–45min | 版本兼容与明确权益增减 | ✅ 完成（代码/本地） |
| 36 | [lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/growth/cmd/MemberTradeAssetCmd.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-member/lemnt-member-application/src/main/java/com/lemnt/member/application/growth/cmd/MemberTradeAssetCmd.java) | 修改 | ★★☆ | 27,28,29,30,31,32 | 20–45min | 接入冻结方案及账户流水幂等 | ✅ 完成（代码/本地） |
| 37 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/payment/mapper/PayNotifyTaskMapper.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/payment/mapper/PayNotifyTaskMapper.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36 | 45–90min | 租约与单调版本，人工重置不会复用旧执行令牌 | ✅ 完成（代码/本地） |
| 38 | [lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/OrderBenefitsGatewayImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-infrastructure/src/main/java/com/lemnt/trade/infrastructure/order/gateway/OrderBenefitsGatewayImpl.java) | 修改 | ★★☆ | 27,28,29,30,31,32,33,34,35,36 | 20–45min | 发送明确行及权益方案 | ✅ 完成（代码/本地） |
| 39 | [lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/dubbo/SettlementLifecycleClientImpl.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/dubbo/SettlementLifecycleClientImpl.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36,37,38 | 45–90min | 新协议不重算积分；库存执行与去重保持mall本地事务 | ✅ 完成（代码/本地） |
| 40 | [lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/PromotionOrderService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-mall/lemnt-mall-application/src/main/java/com/lemnt/mall/application/promotion/service/PromotionOrderService.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36,37,38 | 45–90min | 保留活动执行；区分新版确定方案和历史兼容计算 | ✅ 完成（代码/本地） |
| 41 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/PaymentRefundService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/PaymentRefundService.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36,37,38,39,40 | 45–90min | 读取冻结金额；零现金售后；数量及金额双重判断；成功同事务增加已退数量和入任务 | ✅ 完成（代码/本地） |
| 42 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/RefundBenefitsService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/paymentrefund/service/RefundBenefitsService.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36,37,38,39,40 | 45–90min | 支持以售后事实驱动，无现金无需伪造支付退款 | ✅ 完成（代码/本地） |
| 43 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyTaskService.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyTaskService.java) | 修改 | ★★★ | 27,28,29,30,31,32,33,34,35,36,37,38,39,40 | 45–90min | 只入库与提交后执行分开；租约恢复与版本CAS | ✅ 完成（代码/本地） |
| 44 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyDispatcher.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/payment/service/PayNotifyDispatcher.java) | 修改 | ★★☆ | 27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43 | 20–45min | 按售后号读取已完成方案并派发 | ✅ 完成（代码/本地） |

### P5 查询与页面

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 45 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/OrderPromotionDetailQueryExe.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/OrderPromotionDetailQueryExe.java) | 新增 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 46 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/AfterSalePromotionDetailQueryExe.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/promotion/executor/AfterSalePromotionDetailQueryExe.java) | 新增 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44 | 20–45min | 按设计补齐对应分层类型，保持项目现有风格 | ✅ 完成（代码/本地） |
| 47 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/dto/AfterSaleItemDTO.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/main/java/com/lemnt/trade/application/aftersale/dto/AfterSaleItemDTO.java) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44 | 20–45min | 仅加已退数量；有处理中退单时可申请数量为0；保留数组形态 | ✅ 完成（代码/本地） |
| 48 | [lemnt-ui/src/modules/trade/fulfillment/types.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/types.ts) | 修改 | ★☆☆ | 33,34,35,36,37,38,39,40,41,42,43,44 | 10–20min | 类型与可空历史值 | ✅ 完成（代码/本地） |
| 49 | [lemnt-ui/src/modules/trade/fulfillment/api/fulfillmentApi.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/api/fulfillmentApi.ts) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 新只读子资源；同步decoder | ✅ 完成（代码/本地） |
| 50 | [lemnt-miniapp/services/trade.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/services/trade.js) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 同步只读接口与字段兼容 | ✅ 完成（代码/本地） |
| 51 | [lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/web/fulfillment/FulfillmentOrderController.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/web/fulfillment/FulfillmentOrderController.java) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 新增后台订单促销子资源，复用trade:fulfillment:query | ✅ 完成（代码/本地） |
| 52 | [lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/web/aftersale/AfterSaleController.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/web/aftersale/AfterSaleController.java) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 新增后台售后促销子资源，保持申请请求字段不变 | ✅ 完成（代码/本地） |
| 53 | [lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/app/order/AppOrderController.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/app/order/AppOrderController.java) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 会员订单促销子资源及订单归属校验 | ✅ 完成（代码/本地） |
| 54 | [lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/app/aftersale/AppAfterSaleController.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-adapter/src/main/java/com/lemnt/trade/adapter/app/aftersale/AppAfterSaleController.java) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 会员售后促销子资源及售后归属校验 | ✅ 完成（代码/本地） |
| 55 | [lemnt-ui/src/modules/trade/fulfillment/fulfillmentDecoders.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/fulfillmentDecoders.ts) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48 | 20–45min | 新增字段与历史NULL兼容解码 | ✅ 完成（代码/本地） |
| 56 | [lemnt-ui/src/modules/trade/fulfillment/pages/OrderFulfillmentPage.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/OrderFulfillmentPage.tsx) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 订单优惠来源及行分摊 | ✅ 完成（代码/本地） |
| 57 | [lemnt-ui/src/modules/trade/fulfillment/pages/AfterSaleDetailPanel.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/AfterSaleDetailPanel.tsx) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 冻结退款、优惠归因及权益状态 | ✅ 完成（代码/本地） |
| 58 | [lemnt-ui/src/modules/trade/fulfillment/pages/RefundRequestPage.tsx](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/pages/RefundRequestPage.tsx) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 售后列表展示本次数量；订单详情展示已退/可申请数量；有当前处理单时由服务端禁止新申请 | ✅ 完成（代码/本地） |
| 59 | [lemnt-miniapp/pages/after-sale-create/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-create/index.js) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 数量选择与提交后采用服务端冻结金额 | ✅ 完成（代码/本地） |
| 60 | [lemnt-miniapp/pages/after-sale-detail/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-detail/index.js) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 本次退款结果和权益进度，联动对应wxml | ✅ 完成（代码/本地） |
| 61 | [lemnt-miniapp/pages/order-detail/index.js](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/order-detail/index.js) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 优惠来源及行计数，联动对应wxml | ✅ 完成（代码/本地） |
| 62 | [lemnt-miniapp/pages/after-sale-create/index.wxml](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-create/index.wxml) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 剩余数量、处理中阻断及服务端冻结金额展示 | ✅ 完成（代码/本地） |
| 63 | [lemnt-miniapp/pages/after-sale-detail/index.wxml](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/after-sale-detail/index.wxml) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 优惠归因及权益处理进度展示 | ✅ 完成（代码/本地） |
| 64 | [lemnt-miniapp/pages/order-detail/index.wxml](/Users/wangxx/demo/lemnt-cloud/lemnt-miniapp/pages/order-detail/index.wxml) | 修改 | ★★☆ | 33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55 | 20–45min | 订单及商品行优惠展示 | ✅ 完成（代码/本地） |

### P6 验证、迁移准备及交付

| 编号 | 文件 | 操作 | 复杂度 | 前置依赖 | 估计耗时 | 完成要求 | 状态 |
|---|---|---|---|---|---|---|---|
| 65 | [lemnt-server/lemnt-trade/lemnt-trade-domain/src/test/java/com/lemnt/trade/domain/aftersale/service/AfterSaleRefundCalculatorTest.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-domain/src/test/java/com/lemnt/trade/domain/aftersale/service/AfterSaleRefundCalculatorTest.java) | 新增 | ★★☆ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 20–45min | 10元6件按2/3/1退得3.32/4.98/1.70，零现金和末次多件均覆盖 | ✅ 完成（代码/本地） |
| 66 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/test/java/com/lemnt/trade/application/aftersale/ZeroCashAfterSaleTest.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/test/java/com/lemnt/trade/application/aftersale/ZeroCashAfterSaleTest.java) | 修改 | ★★★ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 45–90min | 零现金行部分退、同订单单笔处理中及已退计数幂等 | ✅ 完成（代码/本地） |
| 67 | [lemnt-server/lemnt-trade/lemnt-trade-start/src/test/java/com/lemnt/trade/start/promotion/OrderPromotionPersistenceTest.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-start/src/test/java/com/lemnt/trade/start/promotion/OrderPromotionPersistenceTest.java) | 新增 | ★★★ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 45–90min | 三表快照、汇总守恒、历史未知及回滚；目标MySQL另列真实证据 | ✅ 完成（代码/本地） |
| 68 | [lemnt-server/lemnt-trade/lemnt-trade-application/src/test/java/com/lemnt/trade/application/payment/PayNotifyTaskServiceTest.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-application/src/test/java/com/lemnt/trade/application/payment/PayNotifyTaskServiceTest.java) | 修改 | ★★★ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 45–90min | 原子入任务、租约恢复、旧版本完成及人工重置隔离 | ✅ 完成（代码/本地） |
| 69 | [lemnt-server/lemnt-trade/lemnt-trade-start/src/test/java/com/lemnt/trade/start/payment/PayNotifyTaskPersistenceTest.java](/Users/wangxx/demo/lemnt-cloud/lemnt-server/lemnt-trade/lemnt-trade-start/src/test/java/com/lemnt/trade/start/payment/PayNotifyTaskPersistenceTest.java) | 修改 | ★★★ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 45–90min | 实际Mapper领取及版本CAS；不能由H2替代MySQL验收 | ✅ 完成（代码/本地） |
| 70 | [lemnt-ui/src/modules/trade/fulfillment/api/fulfillmentApi.test.ts](/Users/wangxx/demo/lemnt-cloud/lemnt-ui/src/modules/trade/fulfillment/api/fulfillmentApi.test.ts) | 修改 | ★★☆ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 20–45min | 新查询契约、数量与历史空值回归 | ✅ 完成（代码/本地） |
| 71 | [docs/order-promotion-refund-migration.md](/Users/wangxx/demo/lemnt-cloud/docs/order-promotion-refund-migration.md) | 新增 | ★★★ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64 | 45–90min | 旧数据映射、单笔规则切换、维护窗口及真实对账清单 | ✅ 完成（代码/本地） |
| 72 | [work-items/member-domain-structure/164-order-promotion-refund-delivery.md](/Users/wangxx/demo/lemnt-cloud/work-items/member-domain-structure/164-order-promotion-refund-delivery.md) | 新增 | ★★☆ | 45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71 | 20–45min | 仅在实施结束后记录真实代码及测试证据，不预填通过结果 | ✅ 完成（代码/本地） |

## 依赖图与并行策略

```text
P1 数据与领域 → P2 内部契约与报价 → P3 订单与售后业务
                                        ↓
P6 验证与迁移准备 ← P5 查询与页面 ← P4 退款与可靠执行
```

依赖采用保守的阶段闸门：后一阶段所有核心任务依赖前一阶段全部任务；阶段内按模型→DO/接口→Mapper→Converter→Gateway、计算器→应用Service、契约→执行→派发等顺序列出编号依赖。所有依赖编号均小于当前编号，已检查无环；同层无引用关系的任务可并行，共享文件及状态机串行处理。

- P1中不同表的DO/Mapper可在模型及字段契约确定后并行，Gateway集成随后验证。
- P2跨服务契约先完成，再联动报价与转换；生产者不能先于兼容消费者切换。
- P3/P4的数量状态机、退款完成、任务原子性及幂等按事务依赖串行，不拆成并行部署。
- P5管理端与小程序可在读接口稳定后分别实现；接口和解码器需一起验证。
- 不同模块的聚焦回归可并行；迁移写入、旧任务切换与逐笔对账有顺序依赖。
- 并行说明仅用于调度分析；本轮未启动子代理。

## 验证闸门

1. 退款算例：10.00元/6件退2、3、1件=3.32/4.98/1.70；0.01元/3件按件退=0/0/0.01。
2. 两个并发申请（即使不同订单行）同订单只创建一个处理中退单；成功回调重复10次已退数量只增加一次。
3. 现金退完但仍有未退数量时不能整单REFUNDED；合法零现金方案不伪造渠道退款。
4. 旧订单、旧RF/PR任务、NULL历史字段及支付单生成后禁止调价均回归。
5. MySQL锁与DDL、真实HTTP、支付渠道和会员账户对账单列证据；未运行就标未验证。

任务拆解与代码生成已完成；真实环境闸门5未运行，不计入已通过验收。
