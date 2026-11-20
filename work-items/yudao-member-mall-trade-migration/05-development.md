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

## Cross-settlement cart-version guard

Fixed reusing the same cart ID/version through another cached settlement after the first order leaves RESERVING. Query existing ledger for non-CLOSED consumption under member lock; preserve original request replay and cart-ID reservation guard. New cart versions and CLOSED-order retries remain allowed. Existing index/tables reused,no migration/module.

Two regression assertions reproduced placement succeeding before fix. After synchronizing remote35ba60c and preserving request recovery,full71-project verify and323 tests PASS,zero failures/errors/skips; integration suite40 cases. Three new cases cover consumed removed cart,later edited cart/new version,and closed-order retry. Miniapp27 tests PASS as merge smoke;React unchanged/not rerun. Evidence/cart-version/. Live MySQL/Redis/identity/provider/device validation remains pending;keep Draft.

## Confirmed settlement abandonment

Added authenticated /trade/app/checkout/abandon. Under member lock,return existing order or write V7 member/settlement fence;delayed submits then reject before Redis. No order closure/refund/stock mutation. Client clears only inspected v2 record after confirmed result;transport errors/member change retain it. Local-only discard now legacy-v1-only and preserves v2. Apply V7 and upgrade all cached-submit handlers;no live DB/config changes.

Root71 projects/330 tests PASS,zero failures/errors/skips;integration46 cases.Six new transaction cases cover fencing/idempotency,existing order,owner scope,submit-vs-abandon race,Redis independence,write-failure rollback;one HTTP contract test.Miniapp30 tests + JS/JSON checks PASS.React unchanged/not rerun.Evidence/settlement-abandon/.Live MySQL/Redis/member/provider/device validation pending;readiness false,PR Draft.

## Local payment-status report

Added owned-order local payment query with fixed channelVerified=false and exact amount/status checks. Foreign/missing orders conceal existence before reading payment records. Mismatches/multiple successes/paid-order inconsistencies report RECONCILE_REQUIRED. No provider call,write,close/refund or stock mutation. Native UI labels local-only report and validates response/account context. No schema or dependency change.

Full71-project Maven verify/338 tests PASS,zero failures/errors/skips.Integration48 cases.Four new query tests,two actual persistence/fulfillment cases,two HTTP identity tests.Miniapp34 tests + JS/JSON checks PASS.React unchanged/not rerun.Evidence/local-payment-status/.Real provider prepay/signature/query and member auth still pending;readiness false,PR Draft.


## Member account interface migration v5 — 2026-10-25

User request: complete member module interfaces. Existing six-layer member module reused; no new service module. Added independent member sessions (password/SMS/WeChat Mini/MP), refresh/logout/revocation, verification states and binding ownership, self profile/password/mobile, addresses, metadata CRUD/simple lists/paging, group/tag assignment, points/experience/level ledgers, sign-in/config/history and member config. 84 REST entries documented in docs/member-api.md, with explicit differences from Yudao reference 8db9153. Admin metadata/global-record pages complement per-member pages. Legacy anonymous registration now requires member:user:create. Member RPC implementation moved to application/dubbo; gateway and trade use authoritative MemberSessionClient, never admin identity. Member V2/V3 and reviewed gateway route snippet supplied, not applied live.

PASS: root Maven verify on Java17/Maven3.9.11, all71 projects,374 tests,zero failures/errors/skips; actual H2/MyBatis V1/V2/V3, transaction rollback/idempotency/concurrency, failed-attempt limits, credential revocation, SMS/state/binding ownership, real query SQL, HTTP contracts, fail-closed gateway and provider configuration. 36 added tests (26 member DB,2 HTTP,3 provider config,5 gateway). Miniapp34 tests PASS. Evidence/member/ retains counts/reactor/commands. React unchanged/not rerun.

NOT RUN: target MySQL/Redis/Nacos/Dubbo/gateway/roles, actual SMS/WeChat and native-device flows. No new member React pages or native login UI in this API slice. Unsupported third-party providers reject; WeChat Mini/MP only. Social response omits openid; level ledger has ID/experience/reason/time, not full historic level snapshot. Point-trade config persists but order deduction/gifting not integrated. API migration is not URL/filter compatibility with all Yudao parameters. MemberApiReady remains false; no production readiness claim. No main edit/merge/deployment/live migration. Next receiver: code QA/human review against design v5 and member API coverage; environment integration remains pending.


## Member admin/native pages v6 — 2026-10-25

Input: user request to complete admin and miniapp pages, backend a261ec3, member API v5 and design/UI baseline v6. Added six registered React member pages (users,levels,groups,tags,records,config) with real typed/decoded APIs, permission-scoped queries, detail/address reads, create/profile/password and idempotent point/experience/level adjustments, group/tag assignments, metadata CRUD, global paged records, point config and sign-rule management. Existing Ant Design/ProTable/AdminModalForm and query/request infrastructure reused. Repeatable member menu SQL assigns no roles.

Four native pages: password/SMS/WeChat login and SMS reset, account profile/security/mobile/social management, address CRUD/default/delete confirmation, benefits/levels/sign-in/records. Memory-only independent member tokens; refresh/401/logout/revocation and stale-account guards. Checkout navigation clears old displays and preserves member-scoped pending intent; damaged intent produces an explicit error. No invented provider/payment success or area-name mapping. Docs: docs/member-pages.md; no new dependencies/lock changes.

PASS: React typecheck/lint/format/build and168 tests (13 added); native54 tests (20 added), JS/JSON and new WXML structural/binding checks; full root Maven71 projects/375 tests/zero failures/errors/skips, including one new actual H2 repeatable menu seed test. Evidence/member-pages/ holds exact reports. Build retains existing vendor chunk warning. UI mocking/SSR tests are not live browser evidence.

BLOCKED browser interaction/visual QA: missing Chromium executable and repeated invalid/truncated download. NOT RUN native compiler/developer-tool/device and actual MySQL/Redis/Nacos/Dubbo/SMS/WeChat. MemberApiReady remains false; production readiness not claimed. Address form currently requires explicit areaId because no region dictionary exists; address-to-order integration and point-to-order deduction/gifting remain outside this slice. Next receiver code QA/human review using design v6 and member-pages contract; no user acceptance fabricated. No main edits,merge,deployment or live migration/config/role grants.


## Storefront and coupon marketing v7 — 2026-10-25

Input: user continues goods/marketing migration,base957c75a,design/UI baseline v7 and existing catalog/checkout. Added public mall/app categories/product search+paging/detail,with SPU/SKU shelf and category/ancestor/brand visibility gates,server price range and no admin codes. Added transactional member-scoped idempotent increment-to-cart, original-result ledger replay and stock/count guards. Added explicit default-off public coupon claim setting,authenticated claim center/wallet with rules and server usability,quota/expiry/ownership and duplicate-claim guards. Admin existing coupon page exposes read/confirmed toggle under existing permissions. Native startup now product listing,detail/SKU/quantity/addition with immutable durable intent,center/wallet pages; existing checkout retains server-cached settlement and gains navigation. Docs/storefront-marketing-api.md and trade V8 supplied. No new service modules/dependencies/lock changes.

PASS: all71 Maven projects/392 tests/0 failures/errors/skips (17 backend tests added);React169 tests,typecheck/lint/format/build (one test added),vendor warning retained;native69 tests (15 added),JS/JSON/new WXML structure/bindings. Actual H2 production SQL and local transactions cover hidden taxonomy,price ranges,claims default-off/expiry/quota/races/rollback,cart increment replay/concurrency/rollback/deletion,HTTP identity/validation and exact public gateway reads. Evidence/storefront/ contains reports. A mapper overloaded method bug was found by claim tests and fixed with distinct SQL statement names before final verify.

NOT RUN: browser visual/interaction,native compiler/developer-tool/device and target MySQL/Redis/Nacos/Dubbo/real providers. Prior Chromium runtime/download failure remains; no new browser PASS. Public claim setting is not auto-enabled;V8 not applied live;readiness false,PR Draft. Remaining: seconds sale/group buy/bargain,stacking/relative coupon dates/refund return,SKU later add/delete/property dictionary,shipping templates/reviews and real payment. Member point/level settings still not order pricing. Next receiver code QA/human review against design v7 and storefront API; no production/acceptance claim. No main edit,merge,deployment/live migrations.


## Promotion kernel T1 — promotion-v1/T1 (2026-10-25)

Responsibility:code-software-engineer;input534048e and docs/promotion/design.md promotion-v1. T1 complete as an unused pure Java domain kernel in existing trade-domain;T2..T7 planned. Detailed deliverable/model contract/acceptance mapping/commands/limits: docs/promotion/implementation.md. No HTTP,SQL,RPC,feature activation,framework or dependency changes. Existing CheckoutRules and all14 design baseline source hashes preserved.

PASS full root Maven verify:71 projects/431 tests/0 failures/errors/skips.39 new tests include canonical23 vectors plus16 boundary/property/differential cases.2000 seeded allocation properties,500 full pricing/permutation/unit partitions,1500 percentage and1500 fixed legacy Java comparisons execute inside those tests,not added to the431 test count. Independent design checks PASS separately. Evidence/promotion-kernel contains verify summary,test case report,source hashes,design result. React/native unchanged,NOT RERUN;live database/cache/RPC/providers/browser/native tool/device NOT RUN.

Next receiver:code QA/human review against P-01..P-08 and T1 contract,then T2 persistent versions/usage. Application must later fetch authoritative member/catalog/coupon/rule/clock facts and revalidate submission;kernel neither reserves quota nor creates orders. No production-readiness claim,main edit,merge,deployment or live migration. Rollback can remove the unused kernel with no data rewrite.


## Promotion T2 recovered delivery — 2026-10-26

Responsibility:code-software-engineer;input83810ab/promotion-v1/T1. Workspace maintenance removed unpublished00b9454 and its evidence before upload;reconstructed internal persistence implementation from design and freshly verified. Existing domain/application/infrastructure reused,V9 eight additive tables and24 actual SQL/transaction tests. No HTTP,RPC,flags,permissions or production caller;V1 unchanged. docs/promotion/persistence.md maps rules,paths,DDD boundaries,lock tradeoff and remaining T3..T7.

PASS fresh full root Maven3.9.11/Java17.0.20 verify:71 projects/455 tests/zero failures/errors/skips,including24 new persistence tests and T1's39 retained. Actual Mapper/Gateway/services/proxies,H2 unmodified V1+V9 fixture,admin replay/revision races,global/member last slot,version quotas,lower-limit/publish/hold race,states/windows/late callback,duplicate/competing terminals,outer/audit failure rollback,transaction requirement and constraints. One test overrides Gateway time explicitly;DDL connection kept alive for H2 CHECK behavior. Locking reads flush/bypass MyBatis cache;member maximum is a current lock read. Root exclusive locks serialize traffic;no capacity claim.

Fresh evidence/promotion-persistence contains current result,cases,verify summaries,23 source/SQL hashes and design check;old test results are not substituted. T1 and14 historical source hashes unchanged. Target MySQL/full target upgrade,Redis/Nacos/Dubbo/providers/browser/native tool/device NOT RUN. React/native unchanged NOT RERUN. V9 provided,not executed live;no worker integration or production readiness. Next code QA/human review D/R local persistence criteria then T3. Preserve ledgers on rollback;no main edit,merge/deploy/live SQL.


## Promotion T3 facts RPC and admin HTTP — 2026-10-26

Responsibility:code-software-engineer;input35fc8ec,promotion-v1/T1/T2. Existing mall-client/domain/infrastructure/application and trade six layers extended;no new module/runtime dependency. New independent PromotionCatalogClient and Bo/DTO;provider in application/dubbo,actual visibility/price facts SQL,consumer rejects unavailable/malformed responses. Scope exists validation runs first create/save/publish after receipt replay;ALL/replayed requests skip external RPC.

Six admin routes implemented with Req→Cmd/Qry→Executor→DTO→Res,explicit authenticated SecurityUser operator and permissions,strict locally scoped JSON converter,parameter/amount checks,read transactions,paging and safe HTTP/R.code errors. Initial revision0 retained;Schema minimum corrected and int32 increment bound tested. Six hidden F permission definitions supplied without page/routes or role grants;new SQL seed and testResource only,no live execution. No frontend or V2 purchase activation. docs/promotion/management.md/api.md map actual RPC/contracts/errors/tests/rollout.

PASS full71-project Maven verify481 tests/zero failures/errors/skips (26 added:3 real catalog SQL,3 RPC provider,5 consumer,14 real MockMvc+trade SQL/AuthLogic-context,1 repeatable no-role/no-page permission seed). T2's24 transaction tests and all prior tests retained;explicit scope bridge added to old fixture,not fake production fallback. Design attachments PASS;T1 sources/14 historical runtime baseline hashes unchanged. T2 admin/Gateway/Mapper changed intentionally for validation/query/revision bound;historical evidence not rewritten. Evidence/promotion-management contains fresh counts,cases,verify andchanged-source hashes.

NOT RUN target MySQL/Redis/Nacos/Dubbo/gateway/real session/provider and browser/native-tool/device. MockMvc uses authenticated test SecurityUser and actual AuthLogic checks;does not prove deployed token verification. Scope bridge is not a real Dubbo call. React/native unchanged NOT RERUN. T4 cache/order snapshots,T5 lifecycle/scanner,T6 pages,T7 integration remain planned. Next code QA/human local C/A/D boundary review and T4. Keep Draft/readiness false,no main edit,merge,deploy/live SQL/grants;rollback preserves V9 resources.
