# 0 元积分兑换与交易零支付设计

2026-10-31 第143批。承接第142批活动订单集成，补齐积分商城补差为 0 的兑换。

## 边界

- 零元订单只允许出现在积分商城：交易接受 `totalAmount=0` 当且仅当 `usePoint>0`；普通结算仍由商城拒绝 0 元提交。
- 不新增支付渠道或支付单；订单在库存和活动预占成功后由交易自动进入 PAID，不创建 `trade_payment`。
- 活动订单映射、积分扣减/退回继续复用第142批 `mall_promotion_order` 和会员权益回调。
- 0 元订单的运费必须为 0；有配送费时订单金额大于 0，仍走普通支付路径。

## 交易

1. `trade_order.total_amount` 约束由 `>0` 改为 `>=0`；初始化 SQL 修改，升级脚本 `upgrade/zero_pay_order.sql`。
2. `TradeOrderClientImpl` 校验：金额不得为负；金额为 0 时必须消耗积分。
3. `OrderRemoteService.reserve` 在 `benefits.reserved` 成功后，若订单金额为 0：
   - `OrderLifecycleService.zeroPay` 将 `PENDING_PAYMENT` 直接转为 `PAID`，记录系统日志；
   - 调用既有 `paid` 流程确认 SKU 库存、通知商城已支付并确认履约库存。
4. 重试幂等：已 PAID 的零元订单再次进入 `reserve` 时跳过状态转换，`paid` 重复执行按既有 RPC 幂等处理；商城活动订单状态 CAS 保证不重复建团/扣库存。
5. 商城 `reserved` 回调失败时沿用第142批补偿：交易关闭订单并发布统一回滚。

## 商城

- `PromotionOrderPricingService`：允许 `payable=0`，但要求 `payable>0 || usePoint>0`；金额为负拒绝。
- `SettlementPreparationService.persist` 增加 `allowZero` 参数，仅活动订单准备允许 0 元快照；普通购物车/立即购买仍拒绝。
- 活动订单映射与状态不变；零元订单支付回调由交易自动触发。

## 会员权益

- `MemberGrowthService.tradeAsset` 允许 `paidFen=0`：
  - CREATE 扣减 `usePoint`；
  - PAID 不再写 0 经验，赠送积分为 0；
  - REFUND 在 `paidFen=0` 时按整单回退已扣积分/已赠积分，不按现金比例分摊；
  - 经验为 0，不产生负余额。

## 客户端

- 积分详情页当订单状态为 PAID 时显示“零元兑换已自动完成”，不再提示待支付。
- 仍使用默认收货地址；有配送费时按普通订单创建。

## 验收

- 交易零元订单自动支付、库存确认、商城已支付回调。
- 商城活动订单 0 元快照、积分扣减、活动库存预占/恢复。
- 会员 0 元订单扣积分、退款整单退积分、经验为 0。
- 不声称真实支付渠道、真实 MySQL/Redis/Dubbo/MQ 或部署验收。
