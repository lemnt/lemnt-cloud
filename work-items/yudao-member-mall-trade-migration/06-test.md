# Test report

v4 · 2026-10-25 · engineering self-review. Inputs: design v4/UI baseline v3, code snapshot in evidence/fulfillment/source-snapshot.json. Base a3c88f1. OpenJDK 17.0.20/Maven 3.9.11; Node 22.13.0/Yarn 1.22.22. Prior evidence remains historical.

| Check | Result | Evidence/limit |
| --- | --- | --- |
| Full root Maven verify, 71 reactor projects | PASS | evidence/fulfillment/maven-reactor.txt |
| Available backend suites | PASS | 296 tests, zero failures/errors/skips; maven-tests.json |
| Actual checkout/reservation/payment/fulfillment SQL and transactions | PASS | CheckoutFlowIntegrationTest, 28 cases, isolated trade/mall H2 DBs and production gateways/mappers |
| Stock confirmation RPC ambiguity blocks shipping, idempotent recovery | PASS | Exception injected after actual mall confirmation commit; no marker until successful response |
| Lost Outbox acknowledgement followed by shipping and lease expiry/retry | PASS | Persisted marker permits shipment; retry in SHIPPED confirms without duplicate stock deduction |
| Shipping/receipt immutable replay and changed-content rejection | PASS | Original shippedAt/receivedAt retained; request mismatch rejects |
| Shipment/receipt records roll back with failed order transition | PASS | Actual transaction rollback with injected transition failure |
| Refund copies discounted payable; blocks shipment; rejection permits fulfillment | PASS | No stock/payment/coupon mutation inferred as refund success |
| Refund key conflicts, one active request and reject/re-request behavior | PASS | Actual DB unique keys and order serialization |
| Concurrent shipping vs refund | PASS | Two threads, actual DB locks; exactly one succeeds |
| Inconsistent or multiple successful payments | PASS | Refund registration refuses and inserts nothing |
| V5 historical backfill | PASS | Actual V5 script run against delivered and undelivered paid-event fixtures; only delivered marked |
| HTTP validation, exact money, permission declarations, no list recipient PII | PASS | FulfillmentHttpContractTest, 3 standalone MVC tests; not deployed authorization |
| Menu seeds repeated without duplicate pages/permissions | PASS | CheckoutMenuSeedTest on system-menu fixture; no role table exists in fixture |
| Frontend typecheck/lint/format:check/build | PASS | evidence/fulfillment/react-verification.txt; existing vendor chunk warning |
| Frontend API, boundary decoder, action gates and checkout status regression | PASS | 155 tests in 31 files; includes 7 new cases |
| Browser interaction / Visual QA | NOT RUN | Prior Unix socket restriction; browser-status.json; no screenshots or interaction PASS claimed |
| Target MySQL migration/isolation/backfill | NOT RUN | H2 is local evidence, not MySQL; V3 represented by existing compatible fixture |
| Live Nacos/Dubbo/gateway/security roles | NOT RUN | RPC bridge and declared permissions are not deployed verification |
| Real payment signature/query/close/refund execution | NOT RUN | Provider integration remains incomplete; REQUESTED does not mean refunded |

Initial verification failures were corrected: generated Res package mismatch; test checked-exception declarations; duplicate-payment fixture needed a different channel; missing-shipment assertion type; retry fixture used PENDING instead of actual FAILED; lint rejected intentional imprecise numeric literal, replaced with runtime Number conversion. No tests were removed or disabled. The stable final root verification and UI checks above passed after fixes.

Keep Draft. Engineering self-review is not independent QA or user acceptance. No production-ready claim.

## Payment replay hardening continuation

Successful payment notifications now require order PAID/SHIPPED/COMPLETED before acknowledging a replay. RESERVING/PENDING_PAYMENT/CLOSED are reconciliation errors, with no payment/order/Outbox writes. Added two regression cases; full root Maven verify passed: 71 projects, 298 tests, zero failures/errors/skips. Evidence: evidence/payment-replay/verify.txt. No UI change or frontend rerun. Real provider signature/close/refund adapters remain pending; no production-ready claim.

## Miniapp project boundary

Added independent lemnt-miniapp native bootstrap/client. Node 22 built-in test runner: 5 tests PASS (no-member/no-ready prevents transport, app paths only, identity/amount fields excluded, stable checkout payload retries, prepay/status separation, invalid input and HTTP/business failures). JSON and JavaScript syntax checked. Backend/React unchanged in this slice, not rerun. WeChat developer-tool compilation, visual QA, real login, deployed /trade/app APIs and real payment NOT RUN/NOT IMPLEMENTED; project is a bootstrap, not consumer migration completion.

## Consumer backend boundary verification

Full root Maven verify: 71 projects SUCCESS, 307 tests, zero failures/errors/skips. Added member-session service tests (2), fail-closed authority adapter tests (2), HTTP/session-boundary tests (3), foreign receipt unit test (1), persisted checkout ownership integration test (1). Existing two-database fixture now has 29 cases. Miniapp client tests 5 PASS. React unchanged, not rerun. Evidence: evidence/app-boundary/verify.txt. Real member login/token-audience authority, gateway integration, WeChat tools/devices and provider payment remain unimplemented/unverified; all /trade/app operations deny access by default with no verifier.

## Miniapp checkout workflow continuation

Added cart display, selected-sellable item preview and submit UI on native index page. Exact displayed amount strings; no fake payment. Durable member-scoped immutable checkout intent saved before request; storage failure aborts transport, unknown/malformed response keeps intent, retry ignores new input, duplicate click rejected and session switch prevents original result exposure. Page binds loaded cart and quote to current member. No cart editing/coupon selector or receipt polling UI yet. Business readiness remains false. Node tests 9 PASS; JS syntax/JSON checks PASS. Backend/React unchanged, not rerun. WeChat WXML/developer-tool/device/visual tests NOT RUN.

## Native cart editing and order refresh

Added quantity +/- (1..999), selected toggle, confirmed delete and manual receipt refresh. Check durable pending checkout before mutations; invalidate quote before uncertain writes; false deletion is failure. Session switch clears stale page data. Receipt refresh accepts only matching order ID, known status and decimal string amount. Six new page behavior tests, 15 total Node tests PASS; JS/JSON syntax checked. No backend or React changes, those checks not rerun. No native compiler/device/visual validation; member auth/provider still pending and default business switch remains disabled.

## Miniapp coupon selection

Added owned-coupon loading, AVAILABLE selection and explicit no-coupon choice. Validate owner/status/ID precision/duplicate records. Invalidate quote before coupon changes and before each preview request; validate quote coupon matches selection and carry same ID into submit intent. Pending checkout prevents coupon changes. UI only shows ID/status because coupon API has no rule metadata; server preview remains authoritative. Five new behavior/decoder tests; 20 Node tests PASS plus JS/JSON syntax checks. Backend and React unchanged/not rerun. Member auth/provider/native compiler/device/visual tests remain pending; business readiness remains false.

## Cached settlement verification

Full root Maven verify: 71 projects,317 tests,zero failures/errors/skips. Added six transactional integration cases (suite now35): cache-eviction replay/different-key duplicate rejection, owner/expiry, cached quantity with later cart edit preservation, price-change rejection, concurrent different requests, bind-failure rollback/retry. Three infrastructure tests cover actual immutable serialization and Redis mock NX/TTL/miss/failure. One HTTP test covers old payload rejection and session-derived identity with cached submit. Miniapp22 tests PASS, including old-v1 protection and explicit scoped reset; JS/JSON syntax checked. Evidence: evidence/cached-settlement/. Live Redis/MySQL/Nacos, member verifier, real payment and WeChat compiler/device/visual checks NOT RUN. React unchanged/not rerun.

## Request-ledger recovery continuation

Added authenticated member-scoped GET /trade/app/checkout/requests/{requestNo}. Queries durable ledger/order with ownership checks, not Redis or placement. Supports cached and legacy records. Miniapp recovers by original key and removes only unchanged inspected record after valid receipt; uncertain results, switched member and changed local record preserve intent. Recovering v1 preserves coexisting v2. Lookup miss is not proof of cancellation.

Full root Maven verify:71 projects,320 tests,zero failures/errors/skips. Three new backend cases:actual cached/legacy request recovery and HTTP identity boundary. Five new miniapp tests,27 total PASS; JS/JSON checks PASS. Evidence: evidence/request-recovery/. Real identity/provider/runtime/device checks remain pending; readiness false,PR Draft. React unchanged/not rerun.

## Cross-settlement cart-version guard

Fixed reusing the same cart ID/version through another cached settlement after the first order leaves RESERVING. Query existing ledger for non-CLOSED consumption under member lock; preserve original request replay and cart-ID reservation guard. New cart versions and CLOSED-order retries remain allowed. Existing index/tables reused,no migration/module.

Two regression assertions reproduced placement succeeding before fix. After synchronizing remote35ba60c and preserving request recovery,full71-project verify and323 tests PASS,zero failures/errors/skips; integration suite40 cases. Three new cases cover consumed removed cart,later edited cart/new version,and closed-order retry. Miniapp27 tests PASS as merge smoke;React unchanged/not rerun. Evidence/cart-version/. Live MySQL/Redis/identity/provider/device validation remains pending;keep Draft.

## Confirmed settlement abandonment

Added authenticated /trade/app/checkout/abandon. Under member lock,return existing order or write V7 member/settlement fence;delayed submits then reject before Redis. No order closure/refund/stock mutation. Client clears only inspected v2 record after confirmed result;transport errors/member change retain it. Local-only discard now legacy-v1-only and preserves v2. Apply V7 and upgrade all cached-submit handlers;no live DB/config changes.

Root71 projects/330 tests PASS,zero failures/errors/skips;integration46 cases.Six new transaction cases cover fencing/idempotency,existing order,owner scope,submit-vs-abandon race,Redis independence,write-failure rollback;one HTTP contract test.Miniapp30 tests + JS/JSON checks PASS.React unchanged/not rerun.Evidence/settlement-abandon/.Live MySQL/Redis/member/provider/device validation pending;readiness false,PR Draft.

## Local payment-status report

Added owned-order local payment query with fixed channelVerified=false and exact amount/status checks. Foreign/missing orders conceal existence before reading payment records. Mismatches/multiple successes/paid-order inconsistencies report RECONCILE_REQUIRED. No provider call,write,close/refund or stock mutation. Native UI labels local-only report and validates response/account context. No schema or dependency change.

Full71-project Maven verify/338 tests PASS,zero failures/errors/skips.Integration48 cases.Four new query tests,two actual persistence/fulfillment cases,two HTTP identity tests.Miniapp34 tests + JS/JSON checks PASS.React unchanged/not rerun.Evidence/local-payment-status/.Real provider prepay/signature/query and member auth still pending;readiness false,PR Draft.
