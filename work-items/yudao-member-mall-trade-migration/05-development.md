# Development delivery

v2 · 2026-10-25 · code-software-engineer. Input: 03-development-design.md v2, UI baseline 04-ui-assets.md v1. Base: 372ce71a82770df802f9832e01a512f2e8d3da79. Branch: feat/yudao-member-mall-trade-migration. v1 reliability delivery is preserved in commit 372ce71; the candidate is the commit containing this report.

## Catalog implementation

- Extended existing mall modules with Category/Brand/SPU/SKU commands, DTOs, converters, gateways, persistence and permission-protected HTTP endpoints. Two-level immutable category ancestry, referenced deletion constraints, bounded lists, paged SPU list and detail are implemented.
- Preserved mall_product IDs as SKU IDs. New SPUs atomically create 1..100 SKUs with canonical, unique property combinations. Metadata updates do not rewrite inventory or variant identities. Existing standalone products remain compatible.
- New inventory adjustment ledger provides stable request keys and delta-based atomic updates. Negative/overflow stock rejects and rolls back the ledger. Capacity checks preserve room for outstanding reservation release. Snapshot/reserve paths enforce SPU, ancestor category and brand availability; existing reservations remain confirmable/releasable after disable/off-shelf.
- Moved ProductCatalogClientImpl into application/dubbo to follow project layering. Added missing explicit Dubbo and @Mapper scans to member/mall/trade start classes, following existing system configuration. Live provider registration remains unverified.
- Added React category, brand, SPU creation/edit/detail, SKU edit and inventory forms. Uses existing Ant Design/ProTable/ModalForm/Query/permission patterns, no new dependencies. Wire decoders follow actual application Jackson: string decimal prices, string-normalized safe/large IDs, omitted nulls. Editing prices above 99,999,999,999.99 yuan is explicitly refused by the UI; such prices remain displayable.
- Supplied additive mall V2 migration, repeatable system menu seed (no role grants), pass-through mall gateway route snippet and docs/mall-catalog-api.md. These configuration inputs have not been applied to a running environment.

## Verification

Full Java 17 Maven verify: 71 reactor projects SUCCESS; 253 tests, zero failures/errors/skips. React Node 22.13.0/Yarn 1.22.22: typecheck, lint, format:check, 141 tests and build PASS. Evidence: evidence/catalog/maven-reactor.txt, maven-tests.json and react-verification.txt.

Actual production mapper/gateway SQL and local transaction proxies were tested on H2 MySQL mode, including aggregate rollback, concurrent stock changes, idempotency, legacy migration data preservation, sellability gates and repeatable menu seed. H2 needed a retained DDL session for its CHECK-IN comparator; production SQL constraints were not removed to make tests pass. Actual Jackson customizer serialization and HTTP validation/binding are tested separately.

UI Design: COMPLETE baseline (04-ui-assets.md and prototype/catalog-admin.html). Assets: COMPLETE/N/A for new binary assets; existing components/icons reused. Implementation: build/tests PASS. Visual QA and browser interaction smoke: BLOCKED because Chrome could not create a Unix socket in this runtime. No screenshots or browser PASS are claimed; see browser-status.json and the replayable mock-HTTP smoke script. Frontend build retains the existing large vendor-chunk warning.

## Deployment / remaining scope

Apply mall V2 only after testing against target MySQL. Apply the menu seed to the system database through reviewed migration tooling and grant narrowly reviewed permissions; merge the gateway snippet without StripPrefix. No production migration, role grant, merge, deployment or main edit was performed. Old mall binaries do not enforce SPU/taxonomy gates, so stop SKU checkout traffic before any binary rollback; retain catalog and ledger rows.

Remaining: real payment prepay/query/close/signature/refund adapters; late-payment and orphan-reservation reconciliation; cart/coupons/promotions; full delivery/refund/order lifecycle; member/trade React administration. Catalog also does not yet implement a global property dictionary, additional variant creation/deletion after initial SPU creation, shipping templates, search/reviews, or taxonomy pagination above 1000 rows. This remains a partial Yudao adaptation, not production-ready.

Next owner: engineering for cart/coupon/order-lifecycle slices and isolated MySQL/Dubbo/channel integration. User acceptance pending; actual effort/cost unknown. No user approval was inferred from passing builds.
