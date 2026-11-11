# 促销活动订单集成交付与验证

2026-10-31 第142批。按 F07 补齐活动订单处理器：秒杀、拼团、砍价、积分商城统一走交易订单，活动库存在下单同步链路预占、关闭/退款恢复；拼团支付后建团，砍价成功后下单绑定记录。设计与边界见 [142 设计](142-promotion-order-integration-design.md)。

## 数据

- `V22__promotion_order.sql`：`mall_promotion_order`，按订单号保存活动类型、活动/记录、SKU、数量、活动价、消耗积分、退款数量与状态（CREATED/RESERVED/PAID/CLOSED/REFUNDED/FAILED）；`(member_id, request_no)` 唯一，订单号唯一。
- `V23__promotion_order_refund.sql`：`mall_promotion_order_refund`，退款单号唯一，重复售后回调不重复恢复库存。
- 未修改 `trade_order`、`OrderCreateBo` 或交易表结构；交易继续只保存通用订单快照。

## 下单

新增 `POST /mall/app/seckill/orders`、`/combination/orders`、`/bargain/orders`、`/point-mall/orders`，入参：活动/砍价记录、可选拼团父团、SKU、数量、收货地址、requestNo。

- `PromotionOrderPricingService`：活动订单只使用活动价，不叠加优惠券、满减、折扣或会员折扣；积分商城实付=补差价×数量，消耗积分=商品积分×数量；按现金实付计算赠送积分；地址存在时复用现有运费服务。
- `PromotionOrderPreparationService`：会员锁内复用 requestNo，同一 requestNo 返回原结算快照；活动关闭后仍可重试原请求；同 requestNo 不同内容拒绝；结算快照与活动订单映射在同一本地事务保存。
- `PromotionOrderService.place`：本地准备事务提交后同步调用交易建单；交易未生成订单时标记 FAILED 供原请求重试，已生成订单交给 `closed` 统一补偿。

## 生命周期

交易既有回调统一接入：

| 回调 | 活动订单处理 |
|---|---|
| `reserved` | 秒杀/积分扣活动与商品库存；砍价扣活动库存并绑定砍价记录订单号；拼团无活动库存；状态 CAS 为 RESERVED。回调失败时交易关闭订单并发布统一补偿 |
| `paid` | 拼团创建参团记录并绑定订单号，写回记录编号；所有类型 CAS 为 PAID |
| `closed` | CREATED/RESERVED 恢复活动库存并 CAS 为 CLOSED；PAID 拒绝关闭；重复回调幂等 |
| `refunded` | 按退款单号去重，按退款数量增量恢复秒杀/砍价/积分活动库存；整单退款 CAS 为 REFUNDED；拼团无活动库存 |

活动库存恢复失败抛回交易回调，由原关单/支付补偿重试；所有状态更新使用条件更新，重复回调不会重复扣减或恢复。

## 客户端

- 小程序四个服务增加 `order`，严格解码订单号/金额/状态/积分，写操作携带会员令牌。
- 秒杀/拼团/砍价/积分详情页接入默认收货地址、requestNo 持久复用、下单结果显示；拼团记录卡片增加“参团”。
- 下单成功只展示订单号、应付金额和消耗积分，并明确“支付渠道尚未接通”，不伪造支付成功。
- 后台不需要新增页面；活动订单通过交易订单中心查询。

## 验证

- 后端 mall 全模块 **165 项通过**、trade 全模块 **223 项通过**，均 0 失败/错误/跳过：
  - `PromotionOrderPersistenceTest` 7：四类活动下单快照、requestNo 幂等/内容冲突、秒杀预占与关闭恢复、砍价绑定与恢复、积分退款按数量去重、0 元与积分不足拒绝、唯一键。
  - `SettlementLifecyclePromotionTest` 1：reserved/paid/closed/refunded 回调转发到活动订单。
  - `SettlementPromotionPreparationTest` 3：新建活动结算、重复请求复用快照、fingerprint 冲突拒绝。
  - `OrderRemoteServiceCallbackTest` 1：商城 `reserved` 回调失败时交易关闭订单并发布统一补偿，避免占用 SKU 库存。
  - `ControllerMappingTest` 收录四个下单路由；原有促销、订单、支付、会员测试全部回归通过。
- 前端 **292 项通过**（未改后台页面）。
- 小程序 **155 项通过**：新增 `promotion-order.test.cjs` 2 项，四类下单路径、会员令牌、请求编号与数量校验；原有秒杀/拼团/砍价/积分页面测试通过。
- 证据：`evidence/promotion-order-verification.json`。

## 未完成与限制

- **0 元兑换未支持**：积分商城补差为 0 时明确拒绝，避免在交易域引入未验证的自动支付状态；后续零支付批次补齐。
- 活动订单使用默认收货地址；未做多 SKU 选择器，默认取活动商品第一个 SKU。
- 拼团订单支付后建团仍依赖活动时间和父团状态，支付晚于活动结束会失败并按支付通知重试；未做超时人工补偿。
- 拼团整单退款未主动把团标记失败；仅恢复 SKU/活动库存和退款状态。
- 未做真实支付渠道、真实 MySQL/Redis/Nacos/Dubbo/MQ、浏览器/真机联调，未由本会话执行 commit/push。

## 143 后续交付

第143批已补齐 0 元积分兑换与交易零支付：交易允许 `total_amount=0`（要求消耗积分）、预占成功后自动 PAID 并执行支付回调；商城活动订单允许 0 元快照；会员允许 `paidFen=0` 扣/退积分且不产生经验。上文“0 元兑换未支持”不再是当前缺口；`trade_payment` 支付单、零元订单按数量比例退款仍待后续，见[143 交付](143-zero-cash-point-order-delivery.md)。
