# Development delivery

v4 · 2026-10-25 · engineering self-review. Base a3c88f1524adc2d67812c1b581bde272b2572d77, branch feat/yudao-member-mall-trade-migration. Inputs: design v4/UI baseline v3; authorized continuation. Previous cart/coupon/catalog delivery is preserved in git and evidence/checkout and evidence/catalog. Candidate is the commit containing this report.

## Implemented continuation

- Added durable whole-order stock-confirmation marker after every paid-event catalog confirmation returns successfully. Shipment cannot proceed while stock confirmation is missing or ambiguous. Marker and original database timestamp survive acknowledgement loss; paid-event retries accept PAID, SHIPPED and COMPLETED so successful fulfillment does not poison a pending acknowledgement.
- Added immutable whole-order shipping record with normalized payload fingerprint and requestNo. Same request/payload replays original record; changed key/content rejects. Shipping requires PAID + confirmed stock + no active refund. Receipt transitions SHIPPED -> COMPLETED, preserving original receivedAt on retry. Shipment/receipt records and order transition join the same trade transaction and roll back together.
- Added pre-shipment full-order refund request and rejection. Amount/payment/member are copied server-side from exactly one matching successful payment; one active request per order and an order/request unique key. All mutations serialize on the order lock, including shipping vs refund. REQUESTED blocks shipping; REJECTED reopens fulfillment. These operations do not execute refunds, mark payments refunded, restock or return coupons.
- Added bounded, permission-protected order/refund pagination and detail APIs through existing Req -> Cmd/Qry -> Executor/service -> domain Gateway -> infrastructure layers, with DTO -> Res conversion. List rows exclude recipient name/phone/address; order detail loads shipment PII separately. No new module or infrastructure dependency in application.
- React order and refund queue pages use existing Ant Design/Query/AdminModalForm/permission patterns, explicit load/error/retry/empty states, shipping form, receipt confirmation, refund reason and rejection reason forms. Stable requestNo retained while a modal stays open; recipient information is not persisted to browser storage. Unknown outcomes require refreshing before reopening a new operation. No fake refund-success button. Existing checkout receipt decoder accepts SHIPPED/COMPLETED.
- Added trade V5 schema/backfill, repeatable additive menu seed without role grants, docs/trade-fulfillment-api.md, design baseline and labeled prototype. No new npm dependency/lockfile changes and no live DB/config changes.

## Verification

Java 17 Maven verify: all 71 projects SUCCESS, 296 tests, zero failures/errors/skips. UI typecheck/lint/format:check, 155 tests and build PASS. Evidence: evidence/fulfillment/; code/test/SQL source checksums in source-snapshot.json.

CheckoutFlowIntegrationTest now has 28 cases with actual production gateways/mappers, two isolated H2 databases and real local transaction proxies. New cases cover confirmation ambiguity, acknowledgement loss followed by shipment/retry, stable shipping/receipt/refund replays, discounted payable amount, inconsistent/multiple payment records, active request guards, shipping/refund concurrency, transaction rollback and actual V5 backfill. The RPC boundary is a test bridge; target MySQL/Dubbo is not verified. Standalone MVC tests cover request validation, exact money, PII exclusion and permission declarations; they do not exercise a deployed security chain. Browser interaction/Visual QA NOT RUN; prior runtime restriction remains documented.

## Rollout and remaining scope

Stop old consumers; review historical paid/stock integrity, apply V5 after V1..V4, deploy the new worker and compatible status readers. Backfill only trusts PAID orders with DELIVERED paid events; unresolved stock confirmation stays blocked for reconciliation. All consumers must accept SHIPPED/COMPLETED retries. Menu seed is a setup input, grants no roles and preserves custom menus. Existing trade gateway route retains the prefix. No live migration, merge, deployment or main edits occurred.

Still pending: real prepay/query/atomic-close/signature/refund channel adapters and payment sandbox verification; member authentication/address book, order-time addresses, split shipments, carrier events/automatic receipt, refund execution/discount allocation/coupon return, target MySQL/live Nacos/Dubbo/gateway/role/browser verification. Current refund feature is an application queue, not a completed after-sales/refund lifecycle. Catalog and coupon limits from prior reports remain. Keep PR Draft; no production-ready or user-acceptance claim.

## Payment replay hardening continuation

Successful payment notifications now require order PAID/SHIPPED/COMPLETED before acknowledging a replay. RESERVING/PENDING_PAYMENT/CLOSED are reconciliation errors, with no payment/order/Outbox writes. Added two regression cases; full root Maven verify passed: 71 projects, 298 tests, zero failures/errors/skips. Evidence: evidence/payment-replay/verify.txt. No UI change or frontend rerun. Real provider signature/close/refund adapters remain pending; no production-ready claim.

## Miniapp consumer entry points

Added /trade/app cart/coupons/quote/checkout/order-receipt controller using existing application executors and converters. MemberSessionService resolves an independent member session through a domain gateway then validates active membership. Infrastructure has an integration port but no real session verifier: requests fail closed by default. No admin user-ID fallback, no memberId request identity, no gateway bypass, no fake login/prepay. Order receipt checks order and checkout record ownership. See docs/miniapp-api.md. Backend consumer identity/provider integration and end-to-end purchase remain pending; keep miniapp memberApiReady false.

## Miniapp checkout workflow continuation

Added cart display, selected-sellable item preview and submit UI on native index page. Exact displayed amount strings; no fake payment. Durable member-scoped immutable checkout intent saved before request; storage failure aborts transport, unknown/malformed response keeps intent, retry ignores new input, duplicate click rejected and session switch prevents original result exposure. Page binds loaded cart and quote to current member. No cart editing/coupon selector or receipt polling UI yet. Business readiness remains false. Node tests 9 PASS; JS syntax/JSON checks PASS. Backend/React unchanged, not rerun. WeChat WXML/developer-tool/device/visual tests NOT RUN.

## Native cart editing and order refresh

Added quantity +/- (1..999), selected toggle, confirmed delete and manual receipt refresh. Check durable pending checkout before mutations; invalidate quote before uncertain writes; false deletion is failure. Session switch clears stale page data. Receipt refresh accepts only matching order ID, known status and decimal string amount. Six new page behavior tests, 15 total Node tests PASS; JS/JSON syntax checked. No backend or React changes, those checks not rerun. No native compiler/device/visual validation; member auth/provider still pending and default business switch remains disabled.

## Miniapp coupon selection

Added owned-coupon loading, AVAILABLE selection and explicit no-coupon choice. Validate owner/status/ID precision/duplicate records. Invalidate quote before coupon changes and before each preview request; validate quote coupon matches selection and carry same ID into submit intent. Pending checkout prevents coupon changes. UI only shows ID/status because coupon API has no rule metadata; server preview remains authoritative. Five new behavior/decoder tests; 20 Node tests PASS plus JS/JSON syntax checks. Backend and React unchanged/not rerun. Member auth/provider/native compiler/device/visual tests remain pending; business readiness remains false.

## Cached settlement implementation

Implemented Redis-backed immutable 15-minute consumer settlement snapshots. Consumer quote returns settlementId/expiresAt; submit accepts only settlementId/requestNo. Orders are generated from cached quote and cart references after ownership/expiry/eligibility checks. New V6 durable association plus member serialization prevents another request from consuming same settlement. Same request retry returns DB order before any Redis access. Cache is never deleted for consumption; TTL cleanup only. Order/checkout/coupon/Outbox/association writes join one transaction. Current price/coupon changes reject instead of silently altering amount. Existing admin compatibility remains old-schema explicitly. New service depends on existing Redis module, no duplicate module.

Miniapp v2 intent stores only settlement ID/key and blocks unresolved v1 records. Explicit local-reset confirmation warns it cannot cancel server orders and a new settlement can create another order; no automatic discard. Contract/rollout: docs/cached-settlement-api.md. Full Maven 71 projects,317 tests PASS; miniapp22 tests and JS/JSON checks PASS. Redis is mocked with real serializer, DB uses actual V6/H2, not live Redis/MySQL. Keep Draft/readiness false until real member identity/runtime/payment verification.

## Request-ledger recovery continuation

Added authenticated member-scoped GET /trade/app/checkout/requests/{requestNo}. Queries durable ledger/order with ownership checks, not Redis or placement. Supports cached and legacy records. Miniapp recovers by original key and removes only unchanged inspected record after valid receipt; uncertain results, switched member and changed local record preserve intent. Recovering v1 preserves coexisting v2. Lookup miss is not proof of cancellation.

Full root Maven verify:71 projects,320 tests,zero failures/errors/skips. Three new backend cases:actual cached/legacy request recovery and HTTP identity boundary. Five new miniapp tests,27 total PASS; JS/JSON checks PASS. Evidence: evidence/request-recovery/. Real identity/provider/runtime/device checks remain pending; readiness false,PR Draft. React unchanged/not rerun.
