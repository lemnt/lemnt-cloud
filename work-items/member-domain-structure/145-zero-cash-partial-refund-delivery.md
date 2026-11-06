# 0 元订单按数量部分退款与积分回退交付

2026-11-01 第145批。按 [145 设计](145-zero-cash-partial-refund-design.md) 将零元积分订单售后从整单扩展为按数量部分退款，并按数量比例退回积分。

## 交易

- `AfterSaleService.create` 零元订单支持部分数量售后，金额仍为 0、无支付单。
- `PaymentRefundService.zeroRefund` 每次售后创建 0 元 `POINT` 成功退款；按 `TradeOrderItemGateway.refundItems` 与已退款售后数量判断：
  - 未达整行数量：订单保持 PAID/SHIPPED/COMPLETED，记录“部分售后”；
  - 达到整行数量：订单转 REFUNDED。
- `RefundBenefitsService` 继续按售后数量调用商城退款回调。

## 商城与数据

- 新增 `mall_promotion_order_item`（`V24__promotion_order_item.sql`），按订单行保存 SKU、数量、消耗积分和累计退款数量。
- 新增 `POST /mall/app/point-mall/orders/batch`，支持一个积分活动下多商品行下单。

- `PromotionOrderService.refunded` 新增 `PromotionRefundResult`：按累计退款数量计算本次应退积分增量，尾差在整单退款时补齐；活动库存按每次退款数量增量恢复。
- `SettlementLifecycleClientImpl.refunded` 将本次积分传入会员权益变更。

## 会员

- `MemberTradeChange`、`MemberTradeAssetBo/Cmd` 增加 `refundPoint`；`paidFen=0` 时按该值退回积分，整单退款返回剩余全部积分；不产生经验或负余额。
- 非零订单退款逻辑不变。
- 小程序积分服务增加 `batchOrder`，严格解码多商品下单响应。

## 验证

- trade **230 项**、mall **167 项**、member **118 项**、前端 **292 项**、小程序 **155 项**通过，0 失败/错误/跳过。
  - `ZeroCashAfterSaleTest` 6：部分售后创建、零元退款成功、部分退款保留订单已支付、权益按 0 金额派发。
  - `PromotionOrderPersistenceTest.zeroCashPartialRefundReturnsPointsByQuantity`：两次部分退款分别退 100 积分，活动库存 5→3→4→5，订单 PAID→REFUNDED。
  - `PromotionOrderPersistenceTest.zeroCashMultiLineOrderRefundsPointsByLine`：两行积分分别按 100/50 计算，库存按行恢复。
  - `MemberAccountIntegrationTest` 零元用例：部分退款退 25 积分，整单退款退回剩余 25 积分，经验为 0。
- 证据：`evidence/zero-cash-partial-refund-verification.json`。

## 限制

- 积分商城多商品零元订单已按商品行拆分积分；秒杀/拼团/砍价仍保持单商品限制。
- `channel=POINT` 仅表示积分权益回退，不是真实支付渠道退款。
- 未执行真实 MySQL/Redis/Nacos/Dubbo/MQ、支付渠道、浏览器/真机联调；本会话未 commit/push。
