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
