# 商城与交易服务边界（2026-10-27）

商城负责商品、购物车、优惠券、结算和促销引擎。交易负责订单、订单明细、支付单、订单履约与对应事务消息。所有层随业务归属迁移，服务间只依赖 client 契约，不共享 Application、Domain、Infrastructure 或 Mapper。

## 结算与下单

1. `POST /mall/app/checkout/quote` 从商城本地商品和优惠券数据生成报价。完整结算单（报价、数量、购物车版本、会员、过期时间）只写 Redis，键为 `mall:settlement:v1:{id}`，使用 NX，寿命为生成后 15 分钟。此时不写交易订单，也不锁库存或优惠券。
2. `POST /mall/app/checkout` 只接收 `{settlementId, requestNo}`，会员编号来自网关透传的已认证身份。校验 Redis 中的结算单归属和有效期；服务端重新校验可售、库存和优惠条件，变化时拒绝并要求重新预览，不能接受客户端金额或静默替换缓存报价。
3. 商城本地准备事务锁会员、锁券、保存已提交请求的幂等回执、结算 ID 与订单号关联、购物车版本引用。`mall_checkout` 是提交后的回执，不是结算预览缓存；不存完整报价明细。Redis 另存已确认的提交快照 `mall:checkout:submission:{orderNo}`，保留 30 天供尚未创建成功的原请求重试。
4. 商城本地事务提交后，`OrderPlacementService` 经 `OrderGateway` 调用 `TradeOrderClient.create(OrderCreateBo)`。交易在自己的事务中原子保存订单、明细与库存预占 Outbox 事件；首个订单状态为 RESERVING。交易库不再访问购物车、结算、优惠券或促销表。
5. 同一会员/requestNo/结算 ID 使用同一订单号。交易按订单号唯一键仲裁并核对会员、金额和明细。交易提交成功但 RPC 响应丢失时，原请求返回同一订单；同一结算单换 requestNo 被拒绝。已生成订单的重试不依赖 15 分钟的原结算缓存。

商城准备事务与交易写单事务独立。不能声称支付失败或交易 Outbox 写入失败会回滚商城数据库。交易失败保留商城已提交请求与锁券，原请求可重试；不会为同一个请求另造订单号。

## 订单事件

交易仍通过既有 Outbox 幂等驱动商城库存。库存预占完成后，通过 `CheckoutLifecycleClient.reserved` 消费购物车的精确版本；支付事件通过 `paid` 核券，关单事件通过 `closed` 释放券。商城操作在商城本地事务中提交。

Outbox 只有在远程操作完成后才标记投递成功。响应丢失或商城失败时保留事件重试；预占事件重试时即使订单已进入待付款，也会重试购物车通知。支付成功不会因商城暂时不可用被回滚。退款申请仍不执行渠道退款、返券或返促销次数。

促销管理、版本、范围、全局/会员次数、使用记录和审计均在商城库。计价事实直接查询商城本地目录。促销次数操作拥有自己的商城事务，读取交易服务的订单快照，不再宣称能跨服务持有订单行锁或与交易库原子提交。促销引擎已有实现迁移，不把历史设计中尚未实现的购买接入功能标记为完成。

## 接口与客户端

- 购物车、优惠券、结算、促销及结算回执接口由 `/trade/...` 改为 `/mall/...`，对应权限改为 `mall:*`。
- 后台直接购买入口改为 `POST /mall/orders`（`mall:order:create`）；`GET /trade/orders/{orderNo}` 仍由交易查询订单。
- 小程序本地支付状态仍为 `GET /trade/app/orders/{orderNo}/payment`。没有新增真实渠道预支付功能。
- 后台购物车和优惠券页面迁至 `lemnt-ui/src/modules/mall/checkout`。小程序按购物/支付接口分别访问 mall/trade。客户端持久化的未决提单 requestNo/settlementId 保持，以便找回原请求。
- 会员认证仍由 auth 签发，gateway 校验 token，服务读取身份上下文；移除交易服务残留的独立 MemberSessionVerifier。

## 数据部署

本次只准备文件，没有操作实际数据库、Nacos 或线上服务。新安装时商城执行原目录 V1/V2 及商城 V10/V12/V13/V14/V15；交易仅保留 V1/V2/V3/V5。

18 张既有购物及促销表迁到商城库，表前缀改为 mall_。去掉对交易库订单表的外键和跨库 JOIN；仍保留商城库内的外键、唯一键和并发约束。保留所有会员、SKU、优惠券、促销、请求和订单标识。

已有数据使用 `lemnt-server/sql/migrations/mall-shopping-data-move.sql.template`：替换真实库名、停止旧版相关写入、执行目标建表后复制。模板针对同一 MySQL 实例的跨库复制，目标相关表须为空，冲突直接失败；不删除源表。不同 MySQL 实例需以同样列映射执行受控导出/导入。复制后核对内容、计数与活动版本，再切流。旧版 15 分钟预览缓存需重新预览，不把旧缓存反序列化成新包名。

权限升级执行 `V20261027__mall_shopping_ownership.sql`，保留 sys_menu 主键和 sys_role_menu 关联；新安装菜单种子为 `V20261025__mall_checkout_menu.sql` 与 `V20261026__mall_promotion_permissions.sql`。迁移不额外授予角色权限。

## 验证层次

数据库回归使用两个独立的 H2 MySQL 模式数据库、真实 Mapper/服务/事务代理，以及真实订单 RPC 提供者和消费者之间的测试桥。Redis 使用真实序列化器及模拟存储校验 NX、TTL、归属、过期、数量快照和重试；不代表真实 Redis、目标 MySQL、Dubbo/Nacos 部署或真实支付渠道已经验收。最终验证日志见 `work-items/member-domain-structure/evidence/`。
