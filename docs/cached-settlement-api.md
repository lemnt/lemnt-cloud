# 小程序 Redis 结算单

当前服务边界见 [商城与交易服务边界](mall-trade-boundary.md)。结算单由 mall 写入 Redis，提交时校验缓存并通过 trade 的服务契约创建订单及明细。

## 预览

`POST /mall/app/checkout/quote`：`{"productIds":["5"],"couponId":"8"}`，couponId 可省略。会员身份来自网关透传，不能提交 memberId。

返回 settlementId、expiresAt、items、originalAmount、discountAmount、payableAmount、couponId。金额保持精确元字符串，ID 保持项目统一序列化。完整结算单只写 Redis 的 `mall:settlement:v1:{settlementId}`，NX 写入且生成后 15 分钟失效。预览不写订单，不预占库存或锁优惠券。

## 提交

`POST /mall/app/checkout`：`{"settlementId":"12345678-1234-1234-1234-123456789abc","requestNo":"REQUEST-1"}`。

检查缓存存在、归属本人、未过期及当前商品/优惠条件。价格变化时要求重新预览；订单数量和价格来自已确认缓存，不接受客户端金额。商城提交请求准备事务后，调用 trade 原子保存订单、明细及库存预占事件。返回当前订单结算回执，首个状态为 RESERVING。

同一请求原样重试返回原订单；换请求号不能重复消费结算单。远程写单成功但响应丢失时不重复建单。15 分钟缓存过期后，已提交请求可按原 requestNo 恢复；未提交的失效预览不能重新生成订单。

## 找回与终止

- `GET /mall/app/checkout/requests/{requestNo}`：只读找回本人已创建的订单，不生成订单，也不依赖 15 分钟结算缓存。查无结果不表示请求已取消。
- `GET /mall/app/orders/{orderNo}`：本人订单的结算回执，不是全量订单履约列表。
- `POST /mall/app/checkout/abandon`：提交原 settlementId/requestNo，阻止尚未接受的提单；已有提交则返回原订单，不执行关单。

客户端先保存会员隔离的 settlementId/requestNo 再提交。超时后保留原请求，不能自动换请求号或清除未决状态。结算 Redis 缓存由 TTL 清理，幂等由已提交请求关联和交易订单唯一键保证。

## 认证与验证

auth 签发独立会员会话，gateway 校验 token，服务读取 SecurityUtils 用户上下文。后台身份不能替代会员身份。真实渠道预支付尚未实现。

验证包括真实序列化器、模拟 Redis NX/TTL/缺失、双数据库事务、并发提交、缓存过期、报价变化、归属校验和响应丢失重试；尚未执行实际环境的数据迁移或端到端支付验收。
