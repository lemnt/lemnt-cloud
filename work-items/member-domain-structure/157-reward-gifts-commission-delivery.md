# 营销赠礼与部分退款佣金回收交付

2026-11-01 第157批。补齐满减活动达到门槛后的包邮、赠积分、赠券派发，以及部分退款按累计退款比例扣回分销佣金。

## 数据

- `trade_order` 增加 `give_coupon_template_counts`（`V17__order_reward_gift.sql`），保存商城结算时冻结的赠券模板及数量。
- `mall_coupon` 增加 `source_order_no` 与唯一索引 `(source_order_no, template_id, member_id)`（`V28__coupon_reward_gift.sql`），保证支付回调重试不重复发券。
- 升级脚本：`trade/sql/upgrade/order_reward_gift.sql`。

## 业务

- 满减规则命中后同时激活同档位包邮、赠积分和赠券；报价返回 `freeDelivery`、`giveCouponTemplateCounts`，运费为 `0.00`，赠送积分计入 `givePoint`。
- 赠券快照随结算单写入交易订单；支付成功后按来源订单逐模板补足发放，重复支付回调不重复发券。
- 部分退款按累计成功退款金额/订单应付金额比例扣回已发佣金；待结算佣金先扣冻结，已结算佣金扣可用，扣回差额以类型4记录留存。整单退款只扣尚未扣回的剩余佣金。
- 报价令牌绑定包邮与赠券快照，提交重算不能替换营销权益。

## 验证

- mall 全模块 **190 项**、trade 全模块 **272 项**、网关 **35 项**测试通过，0 失败/错误/跳过。
- 新增 `SettlementDeliveryFreeShippingTest`、`RewardGiftServiceTest`、`PickUpStoreDistanceTest`，并扩展满减规则、佣金退款和控制器契约测试。
- 真实环境：
  - 满减活动 `2116701552884027394`（满5减1、包邮、赠10积分、赠券模板1）引用结算：运费由 `6.50` 降为 `0.00`，`freeDelivery=true`，赠券 `{1:1}`。
  - 订单 `TR7CF15629652149CEAF68E490` 支付后会员积分 `10000→10010`，券 `source_order_no` 指向该订单；重复沙箱支付只保留1张赠券、积分不再增加。
  - 两件订单 `TR34B9C68635D04FDFBD0012E6` 部分退款 `9.50`：一级佣金 `1.90→0.95`、二级 `0.95→0.47`，生成类型4扣回记录。

证据：`evidence/reward-gifts-commission-verification.json`。

## 未覆盖

- 退款是否撤回已赠优惠券、赠券模板活动结束后补发策略。
- 佣金已提现导致余额不足时的追偿；当前只扣实际可用余额并记录告警。
- 真实支付宝/微信渠道与真机验收。
- 本批未 commit/push。
