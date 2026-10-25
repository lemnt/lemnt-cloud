# Yudao migration continuation

- Version v4, 2026-10-25; owner humanflow-code/code-software-engineer.
- Branch feat/yudao-member-mall-trade-migration; latest slice base a3c88f1524adc2d67812c1b581bde272b2572d77. No main edits, merge or deploy.
- Goal remains Java 17 / Boot 4.1 / Dubbo / Nacos / existing six-layer DDD modules.
- Current slice: durable stock-confirmation gate, immutable shipping/receipt, pre-shipment full-refund request/rejection and React order/refund pages implemented and locally verified. Refund request is not refund execution.
- Inputs: intake v1, design v4, UI/assets baseline v3 and prototype/fulfillment-admin.html. Delivery 05-development.md v4; test report 06-test.md v4.
- Evidence/fulfillment/: 71 Maven projects, 296 backend tests; UI checks/build and 155 tests PASS. Catalog/checkout evidence retained as historical. Browser Visual QA NOT RUN.
- API/setup: ../../docs/trade-fulfillment-api.md, trade V5 and additive system menu seed. Actual V5 backfill tested locally against H2; no live SQL/config or role changes.
- Stop old consumers before V5/new worker; review stock integrity before historical backfill. All readers/consumers must understand SHIPPED/COMPLETED. Prior mall batch/cancel, V4 ledger/tombstone rollout requirements remain.
- Remaining: real payment/refund adapters, member self-service/auth/address book, carrier/split fulfillment and full after-sales, plus target MySQL/live service/security/browser validation. Catalog/coupon limitations remain documented.
- Next: payment-provider integration and isolated runtime verification. User acceptance pending; no production readiness inferred.
- PR https://github.com/lemnt/lemnt-cloud/pull/1 remains Draft.

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
