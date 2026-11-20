# 同步下单与统一异步补偿

2026-10-27。以用户后续确认覆盖此前 Outbox 方案：下单主链路所有调用同步；下单校验并锁券，支付后核销；库存与券回滚合并异步处理。

## 已完成

- 删除 Outbox Gateway/DO/Mapper、应用派发与重放执行器、HTTP 重放接口和相关专用测试。删除消息表初始化及租约迁移；不新增消息记录表、重试队列或数据库投递状态。
- 原 TradeOutboxScheduler 改为 TradeOrderScheduler，仅保留订单超时关闭任务。
- OrderSubmissionService 仅负责订单和明细本地事务。TradeOrderClientImpl 在该事务结束后，通过 OrderRemoteService 同步核验券绑定、预占每个 SKU 库存、转换订单待支付状态并消费购物车版本，然后返回。
- 明确库存不足时同步请求返回失败，订单关闭并发布统一补偿；远程响应未知时保留原订单号，原请求可幂等重试。
- 支付本地事务独立为 PaymentNotificationService；PaymentNotifyCmdExe 在事务提交后同步确认库存、核销优惠券并记录履约库存确认。核销失败返回异常，支付真实结果不回滚，原通知可继续重试。
- OrderCloseService 负责本地关闭，TradeOrderCloseCmdExe 提交后发布 OrderRollbackEvent。关闭过程不再同步单独释放券。
- 复用项目 BaseProducer/RocketMQListener。唯一新消息用途是订单补偿，topic 为 trade-order-rollback-topic；消费异常向容器抛出，交由既有 RocketMQ 重试。
- 消费者调用 mall 的单一 closed 远程接口。商城在同一本地事务里回滚全部库存和释放对应券；任一步失败则整个补偿事务回滚。没有通过异步消息执行正向库存预占或支付核销。
- 下单结算预览仍只写 Redis，提交回执与服务归属保持上一阶段边界。

当前接口、事务、缓存与部署说明见 [服务边界](../../docs/mall-trade-boundary.md)。MQ 配置示例见 docs/config/trade-order-rollback-mq.yaml，旧表清理脚本见 lemnt-server/lemnt-trade/sql/upgrade/remove_outbox.sql，均未执行到实际环境。

## 验证

全 lemnt-server clean verify：429 项通过，0 失败、0 错误、0 跳过。删除了不再适用的 Outbox 租约/重放专用测试，保留并调整双库购物车、券、订单、支付、履约及并发回归。

新增/调整的关键证据：

- 下单返回时已经同步预占库存，券仍为 LOCKED；支付通知返回时为 USED，未依赖后台派发。
- 同步核券失败不会继续扣库存；原请求重试只生成一个订单并扣一次库存。
- 库存/支付确认响应丢失时原请求重试，不双扣、不提前放开发货。
- Mall 释放券失败时库存回滚同时回滚；MQ 重试后库存和券一起恢复，重复消息不重复归还库存。
- 实际 JSON 序列化/反序列化进入消费者，补偿失败会抛给消息容器，空订单事件不被当成成功。
- 静态检查确认 trade 无 Outbox 源码、初始化无消息表，交易保留 6 张订单/支付相关表。
- git diff --check 通过。本轮未 commit/push。

测试使用两个独立 H2 数据库、真实生产 Mapper/服务/事务与 RPC 测试桥；测试队列只替代 MQ 传输。未做真实 broker、MySQL、Redis、Nacos/Dubbo 或支付渠道联调，也未操作实际数据库。

数据库提交与 MQ 发送之间没有新增跨资源原子保证；发送异常返回原调用方，原请求可以幂等重试。未将此描述为 Outbox 的同等投递保证。
