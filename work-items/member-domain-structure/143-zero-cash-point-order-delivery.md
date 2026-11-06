# 0 元积分兑换与交易零支付交付

2026-10-31 第143批。按 [143 设计](143-zero-cash-point-order-design.md) 补齐积分商城补差为 0 的兑换和交易零支付状态。

## 交易

- `trade_order` 金额约束改为 `total_amount>=0`；新增升级脚本 `lemnt-trade/sql/upgrade/zero_pay_order.sql`，初始化 V1 同步修改。
- `TradeOrderClientImpl` 允许 0 元但要求 `usePoint>0`；`TradeOrder.pendingPayment` 允许 0 元。
- `OrderLifecycleService.zeroPay`：`PENDING_PAYMENT -> PAID`，系统日志“零元订单自动支付”。
- `OrderRemoteService.reserve` 在商城 `reserved` 成功后对 0 元订单自动 `zeroPay` 并执行既有 `paid` 流程；已 PAID 重试幂等。
- 商城 `reserved` 回调失败仍关闭订单并发布统一补偿。

## 商城

- `PromotionOrderPricingService` 允许 `payable=0`，要求金额与积分不同时为零。
- `SettlementPreparationService.persist` 增加 `allowZero`，仅活动订单准备允许 0 元；普通结算保持拒绝。
- 积分订单 `totalAmount=0`、`usePoint=商品积分×数量`，活动库存预占/支付/退款沿用第142批映射与补偿。

## 会员

- `MemberGrowthService.tradeAsset` 允许 `paidFen=0`：
  - CREATE 扣积分；
  - PAID 不写 0 经验；
  - REFUND 在 `paidFen=0` 时整单回退积分，不按现金比例分摊。
- 0 元订单不产生经验或赠送积分。

## 客户端

- 积分详情页在订单 `PAID` 时显示“零元兑换已自动完成”；有配送费时仍按普通订单创建。

## 验证

- 后端 mall **165 项**、trade **224 项**、member **118 项**通过，0 失败/错误/跳过。
  - `PromotionOrderPersistenceTest` 零元积分订单：0 元快照、消耗积分、预占/支付后活动库存扣减。
  - `OrderRemoteServiceZeroPayTest` 1：0 元订单预占成功后自动 PAID、确认库存、商城 paid 回调。
  - `MemberAccountIntegrationTest.zeroCashPointOrderDeductsAndReturnsPointsWithoutExperience`：扣积分、无经验、退款整单退积分。
- 前端 **292 项**、小程序 **155 项**通过（积分页零元完成提示）。
- 证据：`evidence/zero-cash-point-order-verification.json`。

## 限制

- 零元订单不创建 `trade_payment` 支付单；支付流水展示为空。
- 已支付零元订单的售后/退款链路未打通：售后金额、支付退单均要求大于0，当前只能通过订单关闭（未支付）退回积分；会员服务的0现金退款能力已具备，待售后零金额批次接入。
- 0 元订单发生部分退款时按整单退回积分，不做按数量比例；有配送费时不是零元订单。
- 仍使用默认收货地址、第一个 SKU；未做真实支付渠道、真实 MySQL/Redis/Dubbo/MQ、浏览器/真机联调。
- 本会话未执行 commit/push。

## 144 后续交付

第144批已打通零元订单售后：售后单/退款单允许无支付单和0金额，零元退款使用 `channel=POINT` 成功记录并派发既有 REFUND 权益任务；商城恢复活动库存并将活动订单置为 REFUNDED，会员整单退回积分。上文“已支付零元订单售后未打通”不再是当前缺口；零元订单部分退款仍待后续，见[144 交付](144-zero-cash-aftersale-refund-delivery.md)。
