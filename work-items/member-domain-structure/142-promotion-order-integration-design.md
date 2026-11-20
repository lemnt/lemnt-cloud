# 促销活动订单集成设计（F07 订单处理器）

2026-10-31 第142批。承接第141批促销运营四子域，按 [完整迁移设计](27-full-migration-design.md) F07 补齐活动订单处理器、取消/退款恢复和任务闭环。本文先固定边界与状态机，再实施代码。

## 目标与范围

- 秒杀、拼团、砍价、积分商城订单统一走交易下单，活动价由商城服务端计算，客户端不能提交价格。
- 活动库存在下单同步链路预占，订单关闭/退款时恢复；拼团在支付后建团，砍价成功后下单绑定记录。
- 积分兑换在本批只支持有补差金额的订单；0 元兑换留到交易零支付批次，避免在交易域引入未验证的自动支付状态。
- 不改交易订单表结构，不给交易增加活动类型字段；活动与订单的对应关系保存在商城促销域，交易继续只保存通用订单快照。

## 服务边界

现有链路：商城结算准备 -> 交易 `TradeOrderClient.create` -> 交易预占 SKU 库存 -> 交易同步回调商城 `reserved` -> 返回；支付、关闭、退款由交易回调商城 `paid/closed/refunded`。本批复用全部回调，不新增 MQ、outbox 或跨服务事务。

新增商城表 `mall_promotion_order`：

| 字段 | 说明 |
|---|---|
| order_no | 交易订单号，唯一 |
| request_no | 商城请求编号，`member_id + request_no` 唯一 |
| activity_type | SECKILL / COMBINATION / BARGAIN / POINT |
| activity_id | 活动编号 |
| activity_record_id | 砍价记录编号；拼团支付后写入建团记录编号 |
| head_id | 拼团父团记录编号，0 表示新开团 |
| sku_id / quantity | 活动商品与数量 |
| activity_price | 下单时活动价快照 |
| use_point | 积分兑换消耗积分 |
| status | CREATED / RESERVED / PAID / CLOSED / REFUNDED / FAILED |

状态机：

```
CREATED --reserved--> RESERVED --paid--> PAID
CREATED --closed--> CLOSED
RESERVED --closed--> CLOSED
PAID --full refund--> REFUNDED
CREATED --trade create failed without order--> FAILED
```

所有状态变更使用条件更新；`reserved/closed/refunded` 重复回调返回原结果，不重复扣减或恢复库存。

## 下单流程

1. App 提交活动下单请求（活动/记录、SKU、数量、地址、requestNo）。会员身份来自网关。
2. 商城校验活动状态、时间、限购、库存和会员积分，按 SKU 原价构建订单行快照，实付使用活动价；积分商城同时计算 `usePoint = 商品积分 × 数量`。
3. 运费按现有配送服务计算；活动订单不支持优惠券、满减、折扣和会员折扣叠加。
4. 商城在一个本地事务内保存结算回执/订单快照到 Redis，并写入 `mall_promotion_order(CREATED)`；同 `requestNo` 重试返回原订单快照。
5. 调用交易 `TradeOrderClient.create`。交易预占 SKU 库存后同步回调 `reserved`：
   - 秒杀/积分商城：扣减活动与活动商品库存，状态 CAS 为 RESERVED；
   - 砍价：扣减砍价活动库存，并绑定砍价记录订单号，状态 CAS 为 RESERVED；
   - 拼团：不额外扣活动库存（库存由 SKU 预占），状态 CAS 为 RESERVED。
6. `reserved` 抛错时交易建单失败并按现有统一补偿关闭订单；商城的 SKU 库存和券由既有 `closed` 回调恢复。

## 支付、关闭、退款

- `paid`：拼团订单创建参团记录并绑定订单号，写回 `activity_record_id`；所有类型状态 CAS 为 PAID。积分抵扣在订单创建校验阶段已由会员服务扣除，赠送积分/经验沿用既有 `paid` 权益回调。
- `closed`：CREATED/RESERVED 状态按类型恢复活动库存（拼团无活动库存），CAS 为 CLOSED；PAID 不执行活动库存恢复，避免与售后重复。
- `refunded`：按退款商品数量恢复秒杀/砍价/积分活动库存；整单退款 CAS 为 REFUNDED。积分退回沿用会员服务既有按实付比例回退。
- 活动库存恢复失败时抛回交易回调，由原支付/关单补偿重试；重复回调因状态条件更新不会重复恢复。

## 价格与积分

- 秒杀：`单次限购` 与时段校验沿用 `SeckillStockService.validateJoin`，实付 = 秒杀价 × 数量。
- 拼团：`CombinationRecordService.validateJoin` 校验父团、限购和 SKU 库存，实付 = 拼团价 × 数量。
- 砍价：仅允许砍价成功记录下单，数量固定 1，实付 = 当前砍后价。
- 积分商城：`PointActivityManagementService.validateJoin` 校验兑换次数和库存；实付 = 补差价 × 数量，消耗积分 = 商品积分 × 数量；会员积分不足拒绝。补差为 0 的订单本批明确拒绝。
- 赠送积分按现金实付和会员积分配置计算，不使用活动价重复计算。

## 接口

```
POST /mall/app/seckill/orders
POST /mall/app/combination/orders
POST /mall/app/bargain/orders
POST /mall/app/point-mall/orders
```

请求统一包含 `requestNo`、`quantity`、`addressId`；秒杀/拼团/积分另含 `activityId`，拼团含可选 `headId`，砍价含 `recordId`。响应返回订单号、金额、状态和消耗积分。

## 测试与验收

- H2 生产 SQL + 真实 Gateway/服务/事务：活动下单快照、重复 requestNo、库存预占、关闭恢复、退款按数量恢复、拼团支付建团、砍价下单绑定、积分不足/超限/0 元拒绝。
- 交易回调集成：reserved/paid/closed/refunded 对活动订单幂等；无活动映射的普通订单不受影响。
- 前端/小程序：接口契约严格解码与按钮接入；不下单成功前提示成功。
- 不声称完成 0 元兑换、真实支付渠道、真实 MySQL/Redis/Dubbo/MQ 或部署验收。
