# 0 元订单售后退款与积分回退设计

2026-10-31 第144批。承接第143批，打通已支付零元积分订单的售后、退款和积分回退。

## 边界

- 零元订单没有 `trade_payment`，售后单 `payment_no` 允许为空，售后金额允许为 0。
- 零元订单只支持整单售后：数量必须等于该商品行可售后数量，避免按数量比例分摊 0 元现金。
- 不新增支付退款渠道；退款交易使用 `channel=POINT`、`paymentNo=null`、`status=SUCCESS`，仅作为权益回退业务凭据。
- 积分回退继续复用既有 `REFUND` 通知任务、`RefundBenefitsService`、商城 `refunded` 回调和会员 `tradeAsset`。

## 数据

- `trade_after_sale.payment_no` 改为可空，`amount` 约束改为 `>=0`。
- `trade_payment_refund.payment_no` 改为可空，`amount` 约束改为 `>=0`。
- 升级脚本 `upgrade/zero_cash_after_sale.sql`；初始化 V5/V6 同步修改。
- 非零订单仍要求成功支付记录、金额大于 0 和渠道交易编号。

## 流程

1. 售后申请：订单金额为 0 时跳过支付记录校验，要求 `usePoint>0`、金额为 0、申请数量为整行可售后数量；售后单 `paymentNo=null`。
2. 商家同意：沿用现有状态机，仅退款进入 `WAIT_REFUND`，退货退款需先收货。
3. 发起退款：`PaymentRefundService.prepare` 在金额为 0 时走零元分支：
   - 校验售后 `WAIT_REFUND`、订单已支付、库存已确认、会员一致；
   - 创建 `PaymentRefund(SUCCESS, channel=POINT, paymentNo=null, amount=0)`；
   - 售后 `REFUNDING -> REFUNDED`，订单 `REFUNDED`；
   - 由既有 `PayNotifyType.REFUND` 任务派发权益回退。
4. 权益回退：`RefundBenefitsService` 按售后商品行数量调用商城 `refunded`；商城恢复活动库存并将活动订单置为 REFUNDED；会员 `tradeAsset(REFUND, paidFen=0)` 整单退回已扣积分，不产生负余额。
5. 重复调用：退款单按售后单唯一，任务与回调幂等；重复售后申请返回原单。

## 验收

- 零元售后创建不依赖支付单。
- 零元退款交易成功、售后/订单 REFUNDED、任务可派发。
- 商城活动库存恢复、活动订单 REFUNDED、会员积分退回、经验仍为 0。
- 不声称真实支付渠道、真实 MySQL/Redis/Dubbo/MQ 或部署验收。
