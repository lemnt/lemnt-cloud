# 0 元订单售后退款与积分回退交付

2026-10-31 第144批。按 [144 设计](144-zero-cash-aftersale-refund-design.md) 打通零元积分订单的售后、退款和积分回退。

## 交易与数据

- `trade_after_sale.payment_no`、`trade_payment_refund.payment_no` 改为可空；两者金额约束改为 `>=0`。
- 初始化 V5/V6 同步修改，新增升级脚本 `lemnt-trade/sql/upgrade/zero_cash_after_sale.sql`。
- `AfterSaleService.create`：订单金额为 0 时跳过成功支付记录校验，要求消耗积分、金额为 0、数量为整行可售后数量，售后单 `paymentNo=null`。
- `PaymentRefundService.prepare`：零元订单创建 `channel=POINT`、`paymentNo=null`、`amount=0`、`status=SUCCESS` 的退款交易，售后 `REFUNDED`、订单 `REFUNDED`，并由既有 `REFUND` 任务派发权益。
- 非零订单售后/退款逻辑不变。

## 权益回退

- `RefundBenefitsService` 复用既有流程，调用商城 `refunded`；商城恢复活动库存并将活动订单置为 REFUNDED。
- 会员 `tradeAsset(REFUND, paidFen=0)` 整单退回已扣积分，不产生经验，不产生负余额。
- 重复售后申请、退款单、通知任务均沿用既有幂等键。

## 客户端

- 无需新增页面；后台售后页可查看 `paymentNo=null` 的零元退款记录。
- 小程序售后入口复用现有订单/售后页面，不新增零元专用交互。

## 验证

- trade 全模块 **227 项通过**、mall **165 项**、member **118 项**、前端 **292 项**、小程序 **155 项**，均 0 失败/错误/跳过。
  - `ZeroCashAfterSaleTest` 3：零元售后创建无支付单、零元退款成功并标记订单退款。
  - `PromotionOrderPersistenceTest` 零元订单用例扩展：退款后活动库存恢复、活动订单 REFUNDED。
  - `MemberAccountIntegrationTest.zeroCashPointOrderDeductsAndReturnsPointsWithoutExperience`：积分整单退回、经验为 0。
- 证据：`evidence/zero-cash-aftersale-refund-verification.json`。

## 限制

- 零元订单只支持整单售后，不支持按商品数量部分退款；部分退款需要后续补齐按积分数量比例回退。
- 零元退款交易不是真实支付渠道退款，`channel=POINT` 仅表示积分权益回退。
- 未执行真实 MySQL/Redis/Nacos/Dubbo/MQ、支付渠道、浏览器/真机联调；本会话未 commit/push。
