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
