# 商城与交易服务归属调整

后续同步下单及移除 Outbox 的调整见 [同步调用与统一补偿](11-sync-order-rollback.md)。本页是此前阶段的实现与测试记录。

2026-10-27，承接 Mapper/DO 规范调整。购物车、优惠券、结算和促销引擎的 Adapter、Application、Domain、Infrastructure、相关测试迁入 mall。交易保留订单、支付及订单履约/事务消息。

当前设计与部署步骤见 [服务边界](../../docs/mall-trade-boundary.md)，结算接口见 [Redis 结算单](../../docs/cached-settlement-api.md)。

## 实现要点

- 完整结算单只写 Redis，提交时校验后经 TradeOrderClient 把已确认快照写入交易订单表和明细表。
- 商城本地准备事务与交易建单事务独立；订单号唯一约束与内容核对保证重试一致。响应丢失、交易 Outbox 插入失败后的重试不会重复建单。
- 商城保留既有提交后请求关联/券锁/购物车版本记录；不新增数据库报价快照。未提交预览不生成这些回执，也不写交易订单。
- 支付和关单事务不再读写商城券表，由既有 Outbox 调用商城生命周期契约幂等核券/释放/消费购物车。
- 促销引擎从商城本地目录获取事实；次数维护使用商城本地事务，通过交易服务读取订单状态，不伪造远程行锁。
- 18 张既有业务表随归属迁库并改为 mall_ 前缀；去除跨服务外键和 JOIN。模板保留全部业务主键，未删除源表，也未执行真实数据库迁移。
- 服务只通过 client 契约交互，静态审计无跨服务 Application/Domain/Infrastructure/Adapter 引用。交易保留 7 张订单/支付相关表，商城 25 张既有业务表。
- HTTP 路径和权限迁为 mall；后台页面目录和小程序调用同步。支付查询仍在 trade。权限升级保留菜单 ID 和角色关联。
- 移除交易服务残留的 MemberSessionVerifier，认证统一在网关处理。

## 最终验证

- 全 lemnt-server clean verify：438 项，0 失败、0 错误、0 跳过。系统文件服务测试需要本地监听端口，权限受限的首次全量运行失败后，已获准重新执行并全部通过。
- 后台：169 项测试通过，typecheck、lint、format:check、build 通过。
- 小程序：69 项测试通过。
- 双库真实 SQL/事务回归验证并发、库存、额度、归属、回滚、缓存失效、原请求重试。新增覆盖报价不写订单、订单提交后响应丢失不重复建单、商城回调失败不回滚已支付订单、迁移菜单保留角色关联。
- Redis NX/TTL 与序列化使用模拟 Redis 和真实序列化器测试；没有真实 Redis、目标 MySQL、线上 Dubbo/Nacos 或真实支付渠道验收。
- git diff --check 通过；未 commit、push 或部署。

证据文件位于本目录 evidence/mall-trade-*。
