# Yudao migration continuation

- Version: v3, 2026-10-25; owner: humanflow-code/code-software-engineer.
- Branch: feat/yudao-member-mall-trade-migration. Current slice base aaa69ec3f7835ed2bd3c80282a7f92f6d119098a; previous catalog/reliability history retained.
- Goal: Java 17 / Boot 4.1 / Dubbo/Nacos / existing DDD six layers; no main edits/merge/deploy.
- Current stage: durable cart/coupon checkout and React admin implementation/technical verification complete locally; user acceptance pending. No production readiness inferred.
- Intake 01-intake.md v1; design 03-development-design.md v3; UI/assets baseline 04-ui-assets.md v2 with catalog and checkout interactive references in prototype/.
- Delivery 05-development.md v3; test report 06-test.md v3. Evidence/checkout/ records 71 Maven projects, 283 backend tests, frontend checks/build and 148 tests. Evidence/catalog/ remains historical.
- API/setup: ../../docs/trade-checkout-api.md, trade V4 SQL, new system menu seed and ../../docs/config/trade-gateway-route.yaml. Files supplied; no live migration, route change or role grant performed.
- New order create now returns RESERVING first; new mall batch/cancel provider must precede new trade worker. Retain tombstones/request ledger; stop old release-only workers. Historic pre-V4 orphans are not automatically repaired.
- Remaining: real payment adapters/signatures/late refunds, member authentication/self-service, zero-pay order support, full addresses/shipping/delivery/receipt/refund lifecycle and corresponding admin pages. Coupon/catalog limits are explicit in delivery/API docs.
- Next owner/action: engineering continues payment/order lifecycle, plus isolated target MySQL/Dubbo/gateway/browser validation. Browser Visual QA remains unverified; no missing user approval is inferred to block independent authorized work.
- PR https://github.com/lemnt/lemnt-cloud/pull/1 remains Draft.
