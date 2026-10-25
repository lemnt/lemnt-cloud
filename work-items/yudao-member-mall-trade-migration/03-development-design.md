# Development design

v4 · 2026-10-25 · code-software-engineer. Inputs: 01-intake.md v1, existing trade code at a476b90; server AGENTS.md. Reference read: code-software-engineer/references/development-design.md and references/java.md.

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


## Checkout slice v3 (base aaa69ec)

v2 is preserved at aaa69ec. Inputs: existing trade/order/Outbox code and Yudao 8db9153 CartDO, CouponDO and CouponTemplateDO. Reuse existing modules, reservation ledger, order aggregates and dispatcher. New backend/UI contracts are admin operations on a specified member; no public member identity is inferred from an admin userId or accepted as trusted consumer identity.

### Reliability prerequisite and order placement

Current synchronous preoccupation can leak inventory on an ambiguous RPC result before local commit. Persist order/items, checkout request, optional coupon lock and TRADE_ORDER_RESERVE in one trade transaction, with status RESERVING. Existing order-create delegates to the same placement service; optional legacy requestNo is generated when absent. Cart checkout requires an explicit idempotency key, with member-scoped payload fingerprint validation. Retry returns the original order and immutable pricing snapshot, never reprices or debits again.

The outbox worker reserves all items using stable order/product keys. It finalizes RESERVING -> PENDING_PAYMENT only after all reserves succeed. Stock rejection closes the order locally and appends TRADE_ORDER_CLOSED. Unknown RPC failures retry through the existing bounded lease worker. Abandoned RESERVING orders are closed after 30 minutes by the optional scheduler, even if a reserve event is DEAD. No payment intent is allowed during RESERVING.

Mall gains cancelReservation(orderNo,productId,quantity): a terminal RELEASED tombstone is inserted if reservation has not arrived; an existing reserved row releases stock; a confirmed row rejects cancellation. All inventory state transitions lock the product before the reservation row and validate immutable quantity. This makes delayed reserve-vs-cancel races safe and treats unattempted SKU releases as idempotent. Closed events cancel every intended item, including an RPC whose response was lost. New mall providers must be deployed before the new trade consumer; do not mix old dispatcher behavior with RESERVING orders.

### Cart and coupons

- Cart keeps unique member/SKU rows with generated IDs, quantities 1..999, selected flag and monotonic version per surviving row. Per-member account locks serialize edits/checkout; maximum 200 lines. Reads use a batch catalog RPC, report unavailable SKUs explicitly, and never persist stale prices.
- Checkout uses 1..100 explicit selected SKU IDs; prices/names/quantities come from server snapshots and owned cart rows. Saved cart ID/version references are deleted only after inventory reservation succeeds; concurrent edits or delete/re-add are preserved.
- Coupon templates are immutable except enabled state; fixed yuan discount or pay-percent semantics (80 means pay 80%, i.e. 8折), optional percent discount cap, threshold, fixed validity interval and bounded total issuance. One instance per member/template. Issuance counter and instance commit atomically; idempotent retries do not consume another quota.
- Coupon states AVAILABLE -> LOCKED -> USED; order close/reservation failure releases LOCKED back to AVAILABLE. Expired or disabled templates cannot be newly redeemed. An already locked order retains its priced discount until success/close. Member ownership, threshold, time and state are revalidated while locking the coupon. Coupon mutation joins order/payment local transactions. No stacking, product-specific scope, relative-date validity, unlimited issuance or refund return in this slice.
- Pricing uses exact decimal yuan, positive server prices and cents precision; discount is capped at gross and percent discounts round down to cents. Quote can show zero payable; placement explicitly rejects zero-payable orders because the existing payment/order schema requires positive totals. No hidden minimum charge is invented. Store original/discount/payable and coupon ID for audit; line snapshots are gross.

### Contracts, tasks and verification

New domain checkout models/gateway; application Cmd/DTO/converters/services/executors; infrastructure DO/mappers/gateway; adapter Req/Res/converters with RequiresPermissions. Admin routes under /trade/admin: member cart GET/PUT/DELETE, member coupon issue/list, coupon-template GET/POST/enable, member quote/checkout POST, checkout receipt GET; original /trade/orders remains and returns RESERVING initially. HTTP documentation: docs/trade-checkout-api.md. Real authenticated member checkout is still blocked on member authentication integration.

Task order: mall cancellation/batch contract + tests; cart/coupon persistence/rules + tests; placement/outbox/close/payment integration + transaction/race/failure tests; admin HTTP and React coupon/cart/quote/checkout UI; final Maven/frontend verification and same Draft PR. Test actual migration SQL/transaction proxies in H2, separating target-MySQL/live-Dubbo checks. Cover duplicate requests, reused-key mismatches, concurrent coupon quota, ownership, expired/locked coupons, delayed reserve cancellation and cart version preservation. Keep actual browser and real channel results separate; no invented PASS.

Checkout hardening: preview returns a canonical quoteToken over immutable item identities/quantities/prices/names and coupon totals; submit requires the same token and rejects changed quotes before order creation. The token is also part of the member-scoped request fingerprint. Active RESERVING references prevent a different request from settling the same cart rows twice. Pending-payment and RESERVING expiry batches are separate to prevent unavailable payment adapters starving reservation recovery; cutoff uses the trade database clock. Coupon use/release checks the persisted checkout lock invariant in the same payment/close transaction; payment amount must equal order payable, not merely the local payment record.


UI state and deployment inputs: operator-scoped persisted checkout intents retain member/SKU/coupon/quoteToken/requestNo until a known receipt. Restored invalid storage is an explicit error, never silently overwritten. New menu seed creates trade/coupon/index and trade/cart/index without role grants; /trade/** gateway snippet preserves its prefix. Coupon/date form serialization follows the installed Pro Components dateFormatter='string' default and the actual Jackson yyyy-MM-dd HH:mm:ss contract verified by MVC tests. Coupon use/release and exact payment/order amount invariants are tested within real local transactions, and all stock transitions lock product then reservation.

Time-zone hardening: newly saved trade orders explicitly use the trade DB creation/update timestamp, matching database-clock expiry. Integration tests open trade sessions in UTC independently of the JVM/mall defaults. Existing pre-migration timestamps are not guessed or rewritten; operators must check their configured historic time-zone assumptions during rollout.


## Fulfillment / refund-request slice v4 (base a3c88f1)

Preserve v3 at a3c88f1. Add admin order list/detail, shipment registration, receipt confirmation and pre-shipment full-refund request/rejection inside existing trade layers. This does not implement actual carrier status queries or payment-channel refunds.

- Paid is not sufficient to ship: all stock-confirmation RPCs must succeed and a durable inventory-confirmation marker must exist. Paid Outbox retries accept PAID/SHIPPED/COMPLETED so an expired lease cannot strand an event after a legitimate shipment. Marker creation is idempotent and local; there is no public endpoint that can forge it. V5 backfills markers only from recorded DELIVERED paid events for PAID orders; operators must review historic integrity.
- Shipping acquires the order lock, requires PAID + confirmed inventory + no active refund request, and atomically persists immutable recipient/carrier/tracking snapshots and PAID -> SHIPPED. A stable requestNo/fingerprint makes same-payload retries idempotent; mismatched reuse/duplicate shipment rejects. Personal recipient fields are absent from order list summaries and require detail permission.
- Admin receipt confirmation transitions SHIPPED -> COMPLETED with database time, atomically updating shipment receipt time. Duplicate confirmation returns the original result. This is an admin recorded acknowledgement, not a claim of verified carrier delivery.
- Full-refund requests are limited to PAID/pre-shipment orders with exactly one matching successful local payment. Amount/payment identifiers are copied from the server; no client amount is accepted. REQUESTED is not refunded and causes no channel transfer, restock or coupon return. One active request/order is enforced, and its existence blocks shipment. REQUESTED -> REJECTED with a decision reason removes that block. Real execution/approval, post-shipment returns, partial refunds and coupon reversal remain pending.
- Lock order is trade order first, then shipment/refund rows. Concurrent ship vs refund resolves under that lock. Existing payment amount/coupon invariants remain intact. No changes to main, deployed services, real payments or role grants.

New additive trade V5 tables: trade_inventory_confirmation, trade_shipment and trade_refund_request (unique active_order_no, unique order/request key). No existing IDs or prices are rewritten. Admin HTTP under /trade/admin/orders and /trade/admin/refund-requests, permissions trade:fulfillment:list/query/ship/receive and trade:refund:list/query/request/reject. Bounded paging 1..100, explicit status filters and full Req/Res contracts documented in docs/trade-fulfillment-api.md. UI reuses existing admin/table/modal design; actual browser QA remains separate.

Verify stock-confirmation gating, paid-event lease retry after shipping, duplicate/mismatched shipment, receipt idempotency, concurrent ship/refund, matching payment amounts, active-request uniqueness and rejection. Exercise actual production SQL/local transaction proxies; distinguish tests from real payment/carrier behavior.

## Miniapp boundary correction

User clarification: cart, quote/checkout, prepay and payment belong to miniapp consumers. Add lemnt-miniapp as a separate native WeChat project, keep React for administration. Existing admin checkout is not consumer completion. No shared admin user ID is treated as a member ID. Define /trade/app contracts separately; no unguarded alias of admin controllers. Until member identity/session isolation and channel adapters exist, client business calls default disabled. New project initially supplies bootstrap, transport contract and tests; full consumer pages/auth/payment remain pending. No new backend module or npm dependency.

## Consumer backend boundary continuation

Introduce /trade/app cart/coupon/quote/checkout/order-receipt entry points reusing existing executors and converters. Identity is resolved by MemberSessionService -> domain MemberSessionGateway -> infrastructure authoritative MemberSessionVerifier integration port. There is deliberately no verifier supplied until independent member login/session/audience/revocation validation exists; zero or multiple verifiers deny access. Never fall back to admin SecurityContext or request memberId headers. Active-member lookup remains mandatory. Order receipt query checks both order and checkout record ownership before returning DTO. No member session is issued, no gateway allowlist is expanded, and no fake prepay/status endpoint is added. This is an authenticated-boundary implementation that fails closed, not a completed login integration.

## Miniapp checkout workflow continuation

Added cart display, selected-sellable item preview and submit UI on native index page. Exact displayed amount strings; no fake payment. Durable member-scoped immutable checkout intent saved before request; storage failure aborts transport, unknown/malformed response keeps intent, retry ignores new input, duplicate click rejected and session switch prevents original result exposure. Page binds loaded cart and quote to current member. No cart editing/coupon selector or receipt polling UI yet. Business readiness remains false. Node tests 9 PASS; JS syntax/JSON checks PASS. Backend/React unchanged, not rerun. WeChat WXML/developer-tool/device/visual tests NOT RUN.

## Native cart editing and order refresh

Added quantity +/- (1..999), selected toggle, confirmed delete and manual receipt refresh. Check durable pending checkout before mutations; invalidate quote before uncertain writes; false deletion is failure. Session switch clears stale page data. Receipt refresh accepts only matching order ID, known status and decimal string amount. Six new page behavior tests, 15 total Node tests PASS; JS/JSON syntax checked. No backend or React changes, those checks not rerun. No native compiler/device/visual validation; member auth/provider still pending and default business switch remains disabled.

## Miniapp coupon selection

Added owned-coupon loading, AVAILABLE selection and explicit no-coupon choice. Validate owner/status/ID precision/duplicate records. Invalidate quote before coupon changes and before each preview request; validate quote coupon matches selection and carry same ID into submit intent. Pending checkout prevents coupon changes. UI only shows ID/status because coupon API has no rule metadata; server preview remains authoritative. Five new behavior/decoder tests; 20 Node tests PASS plus JS/JSON syntax checks. Backend and React unchanged/not rerun. Member auth/provider/native compiler/device/visual tests remain pending; business readiness remains false.

## Cached settlement migration

User requires server-generated cached settlement and cached-snapshot order placement. Consumer quote becomes generate/cache (15-minute immutable snapshot, NX), submit accepts settlementId/requestNo only. Preserve six layers using SettlementGateway, existing RedisSerializer/StringRedisTemplate and new V6 durable settlement-order mapping. Member row locking plus primary-key association enforces one consume; request replay precedes cache read, so cache loss does not destroy confirmed-order recovery. No pre-commit Redis delete; TTL cleanup only. Current eligibility revalidation rejects changed pricing without changing cache-derived order fields. Client v2 intent preserves only ID/key and protects unresolved v1 migration. See docs/cached-settlement-api.md. Admin legacy compatibility retained explicitly; no new module.

## Request-ledger recovery

Add member-scoped read-only request lookup at GET /trade/app/checkout/requests/{requestNo}. Resolve request ledger and order ownership in application service; no Redis/placement call and no client member ID. Client recovers v1/v2 records by original request key, clears only the unchanged verified record after a valid receipt, preserves uncertain results/session changes. Missing lookup never means cancellation. This removes need to blindly resubmit or discard legacy payloads during cached-contract upgrades; no new DB schema or module.

## Cart-version consumption invariant

One cart snapshot can have several cached settlement IDs. The settlement-ID mapping alone does not prevent another settlement from reusing that cart version after the first order leaves RESERVING. Add a gateway query over existing trade_checkout_cart and trade_order records: cart_id + cart_version referenced by any non-CLOSED order is consumed. Check it under the existing member lock before cached placement; original-request replay still precedes this check. Keep the existing cart-ID RESERVING guard for concurrent edits/versions. CLOSED orders release this version for another settlement; newer cart versions remain independently purchasable. Reuse existing cart-ID index/tables, no migration/module needed. Regressions cover removed cart, preserved edited cart and closed-order retry.

## Durable abandonment fence

Add POST /trade/app/checkout/abandon with settlement/request identity. Same member lock serializes with cached submit: existing order returns unchanged; no order persists member/settlement tombstone. Submit checks marker before Redis. V7 composite primary key scopes the fence to member and prevents affecting another owner. No Redis access, order close/refund or inventory mutation in abandon. Client requires confirmation and valid server result before clearing exactly the inspected v2 record; keep unknown outcomes. V2 local-only discard prohibited, legacy v1 manual workflow retained without touching v2. Deploy all submit instances with the fence after V7; no marker purge added.

## Owned local payment report

Add member-scoped GET /trade/app/orders/{orderNo}/payment, query local order/payment records with ownership checked before payment reads. Explicit local states and fixed channelVerified=false; anomalies require reconciliation. No provider calls, writes, stock release or safe-close inference. Keep existing Qry/executor/gateway and DTO/Res layers. Native page displays local-only label and validates ID/state/decimal strings; account switch clears report. Prepay/authenticated provider query still pending, no fake SDK success.

## Member API migration continuation v5 (2026-10-25)

Base 8eac9f6. Reference member controllers at YunaiV/yudao-cloud 8db9153bfe6bdc03baf93b74e244a2e74477c5c4. User requires completion of member interfaces, retaining Java17, Boot4.1 and existing six layers. No new member service module or deployment. Existing account work adds private member sessions, addresses, metadata, balances, sign-in and verified identity flows. This continuation completes query coverage and verifies security before commit. References read: code-software-engineer/references/development-design.md, api-documentation.md and java.md. No UI prototype changes in this API slice.

Tasks: (1) fix exact method/path public gateway rules; gateway strips all caller identity headers and validates private member access through Dubbo. (2) SQL-backed admin metadata paging, global member record paging and record details; bounded pages and parameterized optional member filters, never reuse these admin queries for public ownership checks. (3) test actual V1/V2/V3 SQL with local transaction proxies for SMS limits, expiration, one-time consumption, binding ownership/state/replay and mobile-change revocation. (4) full Maven verify plus existing miniapp tests, docs and same Draft PR. Tests do not send SMS or call WeChat. MySQL/Redis/Nacos/Dubbo/live provider/device tests remain separate.

V2 adds account tables and member profile columns; V3 adds SMS budget/challenges and namespaced social bindings/states. Apply V1 then V2 then V3 only after review/backup; no production SQL executed here. Rollback app before removing schema; deleting schema loses identity/ledger history. Opaque access/refresh token hashes only in DB; passwords BCrypt12; SMS HMAC requires configured pepper. WeChat Mini/MP identities are namespaced by appId and provider proofs stay in infrastructure. SMS send budget commits before vendor call so uncertain delivery cannot bypass limits. Public endpoints accept credentials/proofs rather than caller member IDs. Status1=enabled preserves lemnt semantics. Sign-in uses DB date; money configuration is integer fen and not yet integrated with trade deduction.

API mappings and examples will be in docs/member-api.md. Standard R fields code/msg/data; account advice sends HTTP401/400/409/503, while existing gateway returns HTTP200 with R.code401/503. No claim of production readiness or full external-provider equivalence. Unsupported providers fail closed. Admin permissions are explicit on controllers; deployment must grant approved roles separately.

## Member pages v6 — 2026-10-25

Input: user requests admin and miniapp member pages; backend a261ec3, docs/member-api.md and current Ant Design/ProTable/AdminModalForm + native checkout visual baseline. Skill references development-design.md/api-documentation.md (previously read) and typescript-web.md. No external assets/dependencies required. Reuse existing admin and native components, typography, spacing, loading/error/empty states; no invented backend data or live-provider success.

Tasks in dependency order: (UI1) typed member models/decoders/API in lemnt-ui/src/modules/member/account, request whitelist and exact ID/number validation; (UI2) MemberUserPage list/detail and create/update/password/point/experience/level/group/tag actions, permission-scoped reads, stable requestNo retained on uncertain adjustment, full address detail only with address permission; (UI3) Level/Group/Tag metadata CRUD pages, RecordPage paged ledger/sign-in queries, ConfigPage point/sign rules; registered routes and repeatable reviewed system menu SQL, no role grants; (UI4) miniapp member client, memory-only independent session lifecycle and guarded page controller; (UI5) native login/password/SMS/reset/WeChat, account profile/security/mobile/social, address CRUD/default and benefits/sign-in/records; navigation connects existing checkout and preserves pending settlement intents; (UI6) decoder/API/session/page behavior tests, UI typecheck/lint/format/build and native JS/JSON/WXML static checks, available browser smoke. Each actual verification recorded separately.

Field/interaction mapping: admin search mobile->AccountPageReq; user row ProfileRes -> edit nickname/email/avatar/sex/birthday/status (no unverified phone overwrite); signed delta/reason/requestNo -> asset adjustment, never absolute balance; levelId/reason/requestNo -> level assignment; nullable groupId and tagIds array -> dedicated assignments. Metadata uses exact corresponding Req fields. Records use optional memberId and bounded page parameters; separate permissions for each record family. Config fen integers are shown as fen, no frontend ratio derivation; sign rule key is day, rewards integers. Native identity comes from session token only, never memberId body/header. Login stores only verified token response in memory; no passwords/OTPs/phone codes written to storage or logs. Unknown refresh outcome requires login, never automatic write replay. Account changes clear page state; pending checkout durable intent remains owner-scoped for later recovery. Sign-in completion refreshes server rewards/history and does not invent rewards. Address API exposes numeric areaId with no region dictionary; UI labels administrative area code explicitly and does not invent name->ID mapping.

No backend schema or new endpoints planned. Existing SQL migrations still required; readiness config remains false by default. Full member pages do not imply provider readiness or order-time address integration. UI rollback removes menu entries/routes/client pages while preserving identities and checkout intents. Known environment limits: real backend/provider/native-device validation unavailable until configured; browser attempt and result must be recorded, not assumed PASS.
