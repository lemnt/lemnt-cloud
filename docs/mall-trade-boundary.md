# 商城与交易服务边界（2026-10-27）

商城负责商品、购物车、优惠券、结算和促销引擎。交易负责订单、明细、支付单、支付退单及订单履约。服务间只依赖 client 契约，不共享 Application、Domain、Infrastructure 或 Mapper。交易不再维护 Outbox 表、消息轮询任务或手动重放接口。

## 同步下单

1. `POST /mall/app/checkout/quote` 从商城本地商品和优惠券数据生成报价。完整结算单只写 Redis：`mall:settlement:v1:{id}`，NX、15 分钟有效期。此时不写交易订单，不预占库存或锁券。
2. `POST /mall/app/checkout` 只接收 settlementId/requestNo。会员来自网关身份上下文；校验缓存归属、期限和当前商品/优惠条件，不接受客户端金额或静默改价。
3. 商城同步准备事务校验并锁定券，记录已提交请求的幂等回执、订单号与购物车版本。提交回执和原始订单快照统一存 Redis，键为 `mall:{settlement}:v2:submission:{orderNo}`，保留 30 天用于原请求重试；请求、购物车引用和结算绑定均指向同一份提交。SQL 不再创建结算辅助表。支付后的核券直接查优惠券的订单绑定，不依赖结算缓存。
4. 商城同步调用 `TradeOrderClient.create(OrderCreateBo)`。交易先以稳定订单号保存订单和明细，再同步调用商城核券接口确认归属及锁定绑定，然后逐 SKU 同步预占库存。
5. 预占全部成功后，交易进入 PENDING_PAYMENT，同步通知商城消费购物车的精确版本，才返回下单成功。没有异步建单、异步正向扣库存或异步核券。
6. 校验或远程调用失败向调用方返回异常。未知库存结果保留 RESERVING，沿用原订单号重试；明确库存不足则关闭订单并发送补偿消息。不能因为超时另造订单号。

同一会员/requestNo/结算 ID 使用同一订单号，交易唯一键与会员、金额、明细核对共同保证重试一致。已经创建的订单不依赖 15 分钟预览缓存。商城准备事务、交易订单事务与远程库存事务独立，不声称跨服务原子提交。

## 同步支付核销

下单只校验、锁券；支付成功后才核销。

支付通知先在交易本地事务中记录真实支付结果及 PAID 状态，再同步确认商城库存并核销优惠券，最后写订单履约的库存确认标识。核销失败会返回错误，由原通知重试；已确认的真实支付结果不会被撤销。所有远程操作使用原订单号幂等处理，库存确认未知时不能提前发货。

## 库存与券统一异步回滚

关单及明确的下单失败，在交易状态提交为 CLOSED 后，经项目既有 BaseProducer 发布 `OrderRollbackEvent`。只有这个补偿环节使用 MQ。

- topic：`trade-order-rollback-topic`
- consumer group：`trade-order-rollback-consumer-group`
- 消息内容：orderNo；按订单号顺序发送。

交易消费者校验订单为 CLOSED，再调用商城的单一补偿远程接口。商城在一个本地事务里依次取消/回滚所有 SKU 预占、释放对应优惠券。任一步失败则整个商城补偿事务回滚；消费异常直接抛给 RocketMQ 容器，复用其重试与死信能力，不吞异常，也不再落本地消息表。

MQ 发送失败向原关单/下单请求抛错，原请求可幂等重试。数据库提交与 MQ 发送之间没有新增分布式事务保证。无需为补偿重新实现数据库投递状态、租约、退避或重放调度。

## 促销与接口归属

促销版本、范围、全局/会员次数、使用记录和审计均在商城库，计价事实直接读取本地目录。次数维护拥有商城本地事务，通过交易服务读取订单状态，不跨服务持有订单行锁。原设计尚未实现的促销购买接入仍未标记为完成。

购物车、优惠券、结算、促销和结算回执 HTTP 路径使用 `/mall/...`、权限使用 mall:*。后台直接购买为 `POST /mall/orders`；订单查询 `GET /trade/orders/{orderNo}` 与本地支付状态 `/trade/app/orders/{orderNo}/payment` 仍在交易。真实渠道预支付没有新增。认证由 auth 签发、gateway 校验，业务服务读取身份上下文。

## 数据与部署

本次只准备代码和脚本，未操作实际数据库、MQ、Nacos 或线上服务。

- 商城执行既有 V1/V2 及 V10/V12/V13/V14/V15/V16。交易初始化包含 V1、V2__trade_payment.sql、V5、V6__payment_refund.sql，不再初始化 Outbox。
- 17 张既有购物/促销表（已移除仅用于行锁的账户技术表）归商城库，迁移模板为 `lemnt-server/sql/migrations/mall-shopping-data-move.sql.template`，保留全部业务主键，去除跨库外键/JOIN，不自动删除源数据。
- 权限升级 `V20261027__mall_shopping_ownership.sql` 保留菜单 ID 与角色关联；新安装使用 mall_checkout_menu、mall_promotion_permissions 种子。
- 停止旧 Outbox 调度器并核对旧事件后，旧表清理脚本为 `lemnt-server/lemnt-trade/sql/upgrade/remove_outbox.sql`；没有自动执行 DROP。
- trade 使用项目现有 `rocketmq.name-server`、`rocketmq.producer.group` 配置，需要部署可用的 broker、topic 和消费者。配置示例见 `docs/config/trade-order-rollback-mq.yaml`，未写入实际 Nacos。
- 旧版 15 分钟预览缓存需重新预览；客户端未决 settlementId/requestNo 保留，以便找回原请求。

## 验证层次

回归使用两个独立 H2 MySQL 模式数据库、真实 Mapper/服务/事务代理、真实订单 RPC 提供者/消费者测试桥，验证同步库存和核券、未知响应重试、支付核销、异步补偿原子性及重复投递。MQ 消费者使用真实消息序列化验证异常会向容器传播；测试中的消息队列仅替代传输，不代表真实 RocketMQ 验收。没有实际 Redis/MySQL/Dubbo/Nacos/支付渠道端到端验收。

## 后台支付管理补充（2026-10-27）

支付配置仅展示支持渠道，不建配置表。支付退单独立记录售后触发的逆向交易，新增 trade_payment_refund；交易当前共七张业务表。售后退款中禁止发货和驳回，渠道确认成功后在本地事务完成支付退单、售后单和订单状态。真实渠道未接入，详见 [后台支付接口](payment-management-api.md)。此前同步下单与 MQ 补偿验证为历史批次证据，不代替本次退款验收。

商品部分退款和发货后退货退款已扩展既有售后表，订单统计/备注/关单及独立物流单查询见 [订单售后物流接口](order-after-sale-logistics-api.md)。未新增业务表，交易仍为七张表；部分退款不改写整单报价和原支付成功事实。

## 商城域与基础锁调整

业务代码按 product、cart、settlement、coupon、promotion 分域，会员查询和操作锁按 member 域归属；库存归商品，不使用 shared。移除 mall_checkout_account，会员互斥使用基础锁包 LockTemplate/RedissonLockExecutor 并持有至事务完成；所有商城实例切换后再清理旧表。接口路径、权限及公开 RPC 契约保持兼容，详见 [商城域划分](mall-domain-structure.md)。
