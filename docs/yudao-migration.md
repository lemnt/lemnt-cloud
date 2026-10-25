# Yudao member / mall / trade migration

Reference: https://github.com/YunaiV/yudao-cloud

This branch adapts the business boundaries to lemnt-cloud's Java 17, Spring Boot 4.1, Dubbo/Nacos and six-layer DDD layout. This is an initial migration slice, not a claim that all Yudao features have been ported.

## Implemented in this slice
- Member: basic profile domain, registration use case, MyBatis persistence, registration endpoint, member client contract and Dubbo provider.
- Mall: product domain and creation flow, product persistence, conditional atomic stock reserve/release methods, product catalog client contract/provider.
- Trade: order aggregate and item snapshots, order creation use case, order header/detail persistence and admin order creation endpoint.
- SQL bootstrap scripts for member, product and trade order tables.
- Layered Maven module structure: client, adapter, application, domain, infrastructure, start.

## Important limitations before production
- Member registration is a profile-only flow; it does not include SMS/email OTP, member credentials, OAuth/social login, rate limiting or anti-abuse.
- Product is a first vertical slice, not the full Yudao catalog: SPU/SKU, specifications, categories, brands, rich descriptions, reviews, search and shipping templates remain.
- Promotion capabilities (coupons, flash sale, group-buy, bargaining and discount rules), shopping cart, after-sales, delivery, commission and statistics are not yet migrated.
- Payment/refund channels and callback processing are not included in this slice.
- The order endpoint is admin-permission protected. Do not expose it as a member-facing API until the authenticated member identity is derived from a trusted security context instead of request body memberId.
- Inventory compensation currently attempts synchronous release if order persistence fails. Production needs durable reservation records, idempotent release, reliable events/outbox, timeout close-order jobs and reconciliation.
- Product stock changes are atomic conditional SQL updates. Product service and trade service use separate local transactions, so distributed consistency still needs the above compensation mechanisms.
- Frontend pages/menu permissions for React have not been added. Yudao Vue pages cannot be copied directly.

## SQL
- `lemnt-server/lemnt-member/sql/V1__member.sql`
- `lemnt-server/lemnt-mall/sql/V1__mall_product.sql`
- `lemnt-server/lemnt-trade/sql/V1__trade_order.sql`

Review and apply SQL through the normal migration process in a test database first.


## 第二阶段：订单库存与支付可靠性基础（尚未生产验证）

- 商品域新增 `mall_stock_reservation` 幂等台账，支持库存预占、确认及释放。
- 交易域新增 `trade_payment`、`trade_outbox` 表。
- 支付成功和订单关单会在本地事务中写入 Outbox，库存远程 RPC 在独立执行器中处理。
- `TradeOutboxDispatchExe` 通过数据库抢占处理事件；其行为按至少一次交付设计，库存 RPC 必须保持幂等。
- 交易启动模块提供可选定时调度器，默认关闭（需显式配置 `lemnt.trade.scheduler.enabled=true`）。
- 新增 Outbox 调度的单元测试代码，但尚未执行测试或验证 Maven 构建。

### 上线前阻断项

1. 不要直接启用自动超时关单。必须先接入支付渠道的主动查询与对账，避免支付回调延迟导致已付款订单被关单。
2. Outbox 当前基于超时状态抢占处理事件；还需要明确租约所有权、最大重试和人工补偿策略，避免慢任务跨租约重复执行。
3. 交易与商品跨服务事务仍需端到端故障注入测试（库存 RPC 超时、重复回调、关单与支付竞态）。
4. 尚无真实支付宝/微信预支付和已验签回调适配器。禁止对外公开未经验签的通知入口。
5. 为了便于审阅，交易模块保留在 Draft PR，不应直接合并部署。

## 2026-10-25 verified reliability continuation

- Root `mvn verify` now aggregates the existing modules; corrected six missing Lombok dependencies and explicit parent paths.
- Outbox claims now have unique lease tokens, database-clock deadlines, due-before-limit selection, 10-attempt exhaustion, exponential retry delays and DEAD recovery. Apply `V3__outbox_delivery_lease.sql` with old workers stopped.
- `POST /trade/outbox/{eventId}/replay` requires `trade:outbox:replay`; see [recovery/API contract](trade-outbox-api.md).
- Payment create, notify and close acquire the same order row lock. Close requires every waiting intent to be atomically closed by its provider; missing/ambiguous adapters refuse close. No concrete provider adapter is installed by this slice.
- Verified locally: all 71 Maven reactor projects; 227 tests, zero failures/errors/skips. Evidence: `work-items/yudao-member-mall-trade-migration/evidence/`.
- Earlier statements that Maven tests were never run are historical and superseded by that evidence. Earlier unfenced/unbounded Outbox and unconditional-close warnings describe the old implementation, now replaced.
- Still not production-ready: MySQL migration/concurrency, real channels/signatures, late refunds, Dubbo fault injection and complete business/React migration remain unverified or unimplemented. Tests with mocks/H2 do not establish live service correctness.
