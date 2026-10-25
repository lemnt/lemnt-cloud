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
