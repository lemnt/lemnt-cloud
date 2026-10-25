# Development design

v2 · 2026-10-25 · code-software-engineer. Inputs: 01-intake.md v1, existing trade code at a476b90; server AGENTS.md. Reference read: code-software-engineer/references/development-design.md and references/java.md.

## Reliability slice

Keep Application -> Domain gateway -> Infrastructure direction. Outbox dispatch is at-least-once, inventory remains idempotent. Add a unique token per claim. All acknowledgements and failure transitions require matching token and active lease. A lease expiring allows a new worker to acquire ownership; stale workers cannot mutate delivery state. Remote side effects can still repeat and must remain idempotent.

Select only eligible NEW/FAILED records and expired PROCESSING leases before LIMIT. Count attempts on claim, including crashed workers, with a bound of 10. Failed attempts schedule exponential delays (30 seconds doubling, capped at one hour). Exhausted events move to DEAD; explicit replay resets attempts and is permitted only from DEAD. No public unauthenticated replay endpoint. Add V3 additive schema migration with lease_token, lease_until, next_attempt_at and eligibility index; apply before new worker, never with mixed old workers. Rollback requires disabling workers before deploying old code; retain event rows.

Tasks: change existing TradeOutboxGateway/mapper/implementation/DO/dispatcher, add V3 SQL, update existing dispatcher tests and add mapper integration tests for lease takeover, stale ack, delay eligibility, bounded attempts and replay. No HTTP/UI contract changes in this slice. Maven reactor verify must run before calling this validated; MySQL runtime validation remains separate from H2 checks.

## Payment-aware expiry and remaining slices

Expiry cannot safely infer unpaid from missing callback. A real channel must atomically close/reconcile payment intents before local close; absent adapters must fail closed. Keep scheduled automatic close disabled until this contract is implemented and tested. Payment signature verification must use the chosen provider's actual protocol and merchant configuration; no invented generic signature advertised as Alipay/WeChat compatible.

Catalog/cart/coupons/order lifecycle/React remain required follow-up slices; inspect existing UI conventions and reference contracts before implementation. No mock data or unfinished endpoints may be presented as completed integration.

Build repair: add a root aggregation POM for existing modules and explicit parent relative paths in common/server, so a fresh checkout can run `mvn verify` without manually installing the parent. This creates no business module. Temporary Central mirror/proxy settings are environment-only. Mockito inline attachment hangs in this container; use the resolved Byte Buddy agent via Surefire argLine for verification and record this invocation.

Payment race repair: acquire the trade_order row lock in payment creation, notification and close transactions, always before payment rows. Close enumerates all intents and closes only when every intent is locally CLOSED or the concrete provider positively confirms an atomic close of an unpaid intent. Missing provider, unknown, paid or errors keep the order open. An empty intent list is safe because creation uses the same order lock. Provider contract is a domain port, not a claim of channel integration; no provider is configured by default. Late successful callbacks remain a reconciliation/refund gap. Test local lock orchestration and fail-closed policy; live channel verification is BLOCKED without a concrete integration.

Manual compensation API: `POST /trade/outbox/{eventId}/replay`, permission `trade:outbox:replay`. Accept a nonblank identifier up to 64 characters, return `R<Boolean>`; true only for an atomic DEAD -> NEW reset. DELIVERED/NEW/active/unknown events return false, never revive completed tasks. Normal worker handles replay with a fresh token. Operator must first repair the underlying failure and inspect event/order/reservation state. Add executor tests, API documentation and annotation checks. No visual assets are required for this backend endpoint.

Lease and retry deadlines use database `CURRENT_TIMESTAMP`/`TIMESTAMPADD`, rather than worker wall clocks, so host clock skew cannot authorize stale ownership. New inserts should rely on the database default due time.


## Catalog slice v2 (base 372ce71)

The reliability design remains valid; v1 is preserved by git commit 372ce71. Input: user continuation, current mall module, server/UI AGENTS.md, Yudao reference 8db9153bfe6bdc03baf93b74e244a2e74477c5c4 (ProductCategoryDO, ProductBrandDO, ProductSpuDO, ProductSkuDO). This is an adaptation, not a full field-for-field port. Keep decimal yuan precision (2 decimals), rather than Yudao integer cents, for compatibility with existing trade contracts.

### Data and transaction contract

- Reuse mall_product as SKU and preserve all existing IDs and order/reservation links. Add nullable spu_id and variant_key; legacy standalone products retain null values and existing behavior.
- Add mall_category (two levels, nullable root parent), mall_brand, mall_spu and mall_sku_property. Foreign keys prevent deletion of referenced classifications, brands, SPUs and SKU properties; no destructive migration or backfill required.
- Categories/brands: create/update/detail/list/delete, ENABLED/DISABLED; parent cannot be changed on category update. Root or one child level only. Category disabling takes effect on future sales through its ancestors; reserved orders may still confirm/release. Deletes fail if referenced, including referenced off-shelf SPUs. Every operation is admin permission protected.
- SPU creation is atomic with 1..100 SKU drafts. Each draft has a unique trimmed code, name, positive price with at most 2 decimals, nonnegative available stock, and 0..10 named properties. All variants must use the same property-name set. Canonical length-delimited sorted property pairs produce a SHA-256 variant key, unique per SPU, independent of map iteration order. Initial shelf state OFF_SHELF.
- SPU metadata update changes name/category/brand/description/image only, never rewrites SKU inventory or variant identities. SKU price/name/image update requires an explicit SKU endpoint. No hard deletion of SKU/SPU in this slice because historical reservations/orders reference IDs.
- Publishing requires enabled category/ancestors/brand and at least one SKU. Availability and stock preoccupation both recheck parent shelf state and taxonomy status in SQL, so legacy child shelf endpoints cannot bypass SPU/category/brand gates. Existing reservations remain confirmable/releasable after off-shelf changes.
- Inventory adjustments use a unique requestNo ledger and signed delta (nonzero int), atomic conditional SQL preventing negative stock/INT overflow. Same requestNo/product/delta is idempotent; mismatched reuse rejects. Record reason and resulting stock in a local transaction with the inventory update. Never replace stock from a stale page value.

### Layers and API

New code stays in existing six modules, under catalog packages. Domain models/gateways are plain Java; application Cmd/Qry/DTO and executors isolate HTTP and persistence; infrastructure owns DO/mappers/converters. Move existing product Dubbo implementation into application/dubbo as required by AGENTS.md; wire no direct infrastructure dependencies into application.

Admin routes: /mall/categories, /mall/brands (GET list, POST create, PUT /{id}, DELETE /{id}); /mall/spus (GET paged, POST create, GET/PUT /{id}, PUT /{id}/shelf); /mall/skus/{id} (PUT metadata), /mall/skus/{id}/stock (POST delta). Permissions mall:category/brand/spu/sku:list/query/add/edit/remove/stock as applicable. Query pagination bounded 1..100; no unbounded public catalog dump. Responses use adapter Res converted from application DTO, IDs are long, amounts are decimal yuan, status strings explicit. Endpoint documentation lives in docs/mall-catalog-api.md.

### Verification and sequence

1. Taxonomy models/gateway/application/HTTP + additive SQL; test normalization, two-level rules and referenced deletion.
2. SPU/SKU variants and inventory ledger; test canonical properties, duplicate variants/codes, amount bounds, invalid references, transactional rollback and stock idempotency/overflow; execute actual mapper SQL in H2 and production SQL migration where supported. MySQL runtime checks stay separate.
3. Existing RPC snapshot/reservation paths recheck sellability and retain stable IDs; test off-shelf/disabled gates and confirm/release behavior.
4. Run targeted Maven verify after each behavior change, then whole reactor; update delivery/test evidence and same PR. No live payment deployment or production readiness claim.

React management will reuse existing Ant Design/ProTable/ModalForm/Query patterns after backend contracts are stable. UI baseline: existing PostPage/DeptPage layout, no new external assets or dependencies; catalog fields and states defined above must be mapped explicitly before UI implementation. UI Design, Assets, Implementation and Visual QA are reported separately; runtime screenshots do not replace design evidence.


Serialization/UI clarification: actual JacksonConfig serializes BigDecimal as strings and large Long as strings, omitting nulls. The catalog API decoders normalize IDs to strings and preserve price strings; form edit is bounded to 99,999,999,999.99 yuan and refuses unsafe larger amounts. Actual serializer tests cover this contract. UI source uses existing components and no new dependency/lockfile. Menu seed and pass-through gateway snippet are additive deployment inputs, not applied changes. Browser smoke launch is blocked by the runtime Unix socket restriction; do not claim visual/interaction PASS.

Startup review found migrated start classes lacked the explicit Dubbo and MyBatis scanning used by lemnt-system. Add EnableDubbo for each migrated service root and MapperScan on its infrastructure package filtered by @Mapper; this covers both legacy direct mapper packages and the new catalog mapper subpackage. This repairs startup wiring configuration; a successful compile still does not prove Nacos/Dubbo runtime registration.
