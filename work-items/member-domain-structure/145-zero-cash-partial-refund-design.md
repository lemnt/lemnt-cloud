# 0 元订单按数量部分退款与积分回退设计

2026-11-01 第145批。承接第144批，将零元积分订单售后从整单扩展为按数量部分退款，并按数量比例退回积分。

## 边界

- 零元订单售后金额仍为 0，支付退款记录仍使用 `channel=POINT`、`paymentNo=null`。
- 售后数量可小于商品行数量；订单仅在累计已退款数量达到商品行数量时置为 `REFUNDED`，否则保留原履约状态。
- 活动库存按每次退款数量增量恢复；活动订单仅在最终整单退款时标记 REFUNDED。
- 积分按累计退款数量计算：`floor(order.usePoint × 累计退款数量 / 订单数量)` 的增量，尾差在最后一次退款补齐。
- 非零订单仍按现金金额比例回退，沿用原逻辑。

## 交易

- `AfterSaleService.create` 零元订单不再要求整行售后，只校验金额为 0、消耗积分存在。
- `PaymentRefundService.zeroRefund` 每次售后创建一条 0 元 POINT 成功退款；通过 `TradeOrderItemGateway.refundItems` 和已退款售后数量判断是否整单完成：
  - 未完成：订单保持 PAID/SHIPPED/COMPLETED，记录“部分售后”日志；
  - 完成：订单转 REFUNDED。
- `RefundBenefitsService` 继续传递本次售后商品数量；退款任务按售后单号幂等。

## 商城与会员

- `PromotionOrderService.refunded` 返回本次应退积分：
  - `previousPoints = floor(usePoint × 已退数量 / 订单数量)`；
  - `currentPoints = floor(usePoint × 新累计数量 / 订单数量)`；
  - 本次退回 `currentPoints - previousPoints`。
- `SettlementLifecycleClientImpl.refunded` 将本次积分数传给会员 `MemberTradeChange.refundPoint`。
- 会员 `MemberTradeAssetCmd/Bo` 增加 `refundPoint`；`paidFen=0` 时按该值退回积分，整单退款返回剩余全部积分；赠送积分/经验为零。
- 活动库存恢复继续由 `PromotionOrderService.refunded` 按数量增量处理。

## 验收

- 零元订单部分售后成功，订单保持已支付。
- 多次部分退款累计达到整单后订单 REFUNDED。
- 每次退款按数量比例退回积分，累计等于原消耗积分。
- 活动库存按退款数量增量恢复。
- 不声称真实支付渠道、真实 MySQL/Redis/Dubbo/MQ 或部署验收。
