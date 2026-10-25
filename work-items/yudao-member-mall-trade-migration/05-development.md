# Development delivery

v1 · 2026-10-25 · code-software-engineer. Input: 03-development-design.md v1; base a476b909c780cfde338fcef9ae208500b604bc56. Branch: feat/yudao-member-mall-trade-migration. Candidate changes are tracked with this document's commit.

Implemented: root reactor and local parent resolution; missing Lombok dependencies in six migrated application/adapter POMs; fenced Outbox ownership, database-clock leases, due filtering before LIMIT, bounded claims, delayed retries, DEAD recovery and protected manual replay; payment/create/close order locking; conservative provider-aware close gateway (missing adapter refuses closing); payment duplicate handling keyed by channel transaction.

Evidence: `evidence/maven-reactor.txt` (all 71 reactor projects SUCCESS), `evidence/maven-tests.json` (227 tests, zero failures/errors/skips). Commands are recorded in evidence. A temporary Central mirror/proxy was required because Aliyun DNS failed. In this container Mockito dynamic attachment hung; explicit Byte Buddy test agent resolved it. An initial real compile failure exposed missing Lombok imports; repaired and verified.

Migration: stop old workers, apply trade V3 after V1/V2, deploy new workers. Never mix old and new outbox workers; old acknowledgements do not fence ownership. Five-minute leases, 10 claims per replay cycle, failed attempts back off 30 seconds doubling to one-hour cap. RPCs must be idempotent; partial successes can repeat. See `docs/trade-outbox-api.md` for recovery. No schema was applied to a live database.

Remaining scope: concrete payment provider query/atomic-close adapter, authenticated signature callback adapter and channel sandbox integration; late-payment refund/reconciliation; catalog SPU/SKU/category/brand; cart/coupons/full delivery/refund lifecycle; React administration. Existing order-create pre-reservation failure compensation still requires durable orphan reconciliation. Nothing here is production-approved.

Next: concrete channel protocol implementation and MySQL/Dubbo integration, then catalog and checkout lifecycle slices. UI Design/Assets/Implementation/Visual QA: NOT RUN for the requested React migration; this backend reliability slice requires no visual assets. Actual effort/cost unknown. User acceptance pending. No merge/deploy.
