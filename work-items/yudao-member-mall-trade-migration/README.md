# Yudao migration continuation

- Base35fc8ec;branch feat/yudao-member-mall-trade-migration;PR[#1](https://github.com/lemnt/lemnt-cloud/pull/1) remains Draft.
- Latest:promotion-v1/T3 authoritative facts RPC and six admin HTTP endpoints,2026-10-26 Asia/Shanghai. [Implementation](../../docs/promotion/management.md),[contracts](../../docs/promotion/api.md),[design](../../docs/promotion/design.md).
- New independent PromotionCatalogClient methods for bounded trusted SKU/SPU/price/stock/hash and static scope existence;legacy ProductCatalogClient unchanged. Provider visibility/price and consumer protocol/data/error checks;no price fallback. Stock excluded from hash. First create/save/publish validate scope after successful receipt replay;retries survive provider failure.
- Six protected admin routes:create,draft,publish,state,detail,page. Explicit real admin SecurityUser operator/permission/expiry;unknown/duplicate/trailing/coerced JSON rejected only in new requests;0-based int32 revisions aligned with T2;safe matching HTTP/R.code errors. Req→Cmd/Qry→Executor→DTO→Res and no application→infrastructure dependency. Permission seed supplied,not run/no grants/no nonexistent page.
- PASS full root Maven:71 projects/481 tests/0 failures/errors/skips;26 added (3 actual mall SQL,3 provider,5 consumer,14 HTTP+trade SQL,1 seed). Original455 retained. Design attachment validation and T1/14 baseline source hashes PASS;[evidence](evidence/promotion-management/).
- MockMvc security context and scope test bridge are not live gateway/Redis/Dubbo/provider evidence. Target MySQL/Redis/Nacos/Dubbo/providers/browser/native tool/device NOT RUN. React/native unchanged NOT RERUN (169/69 historical).
- T4..T7 PLANNED:server-cached V2 pricing and ID-only submission/order snapshots,worker/payment-close/Outbox/compensation integration,pages and live verification. Five V2 routes not implemented;V1 unchanged. Next:T4 cached settlement. Keep readiness false/Draft;no main edit,merge,deployment/live SQL/role grants.

The following entries preserve historical slice results;their missing-feature statements must be read at that slice's version.

Latest continuation: Redis cached consumer settlement generation and ID-only submission implemented; V6 durable association, member-scoped replay and six new transaction cases. Current verification317 backend tests/22 miniapp tests PASS; see evidence/cached-settlement and docs/cached-settlement-api.md. Admin legacy protocol unchanged, real member auth/provider/runtime validation pending.

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
