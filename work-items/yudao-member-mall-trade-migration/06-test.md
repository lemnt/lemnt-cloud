# Test report

v2 · 2026-10-25 · engineering self-review. Inputs: design v2 and UI baseline v1; candidate identified by the commit containing this report. v1 evidence is preserved at commit 372ce71. Environment: OpenJDK 17.0.20, Maven 3.9.11, H2 MySQL mode, Node 22.13.0, Yarn 1.22.22.

| Check | Result | Evidence / limit |
| --- | --- | --- |
| Complete Maven reactor verify, 71 projects | PASS | evidence/catalog/maven-reactor.txt |
| Available Maven suites | PASS | 253 tests, zero failures/errors/skips; maven-tests.json |
| SPU variants, canonical keys, money precision, invalid drafts | PASS | CatalogRulesTest, 5 cases |
| Category levels/reparenting, enabled ancestors, paging, stock validation | PASS | CatalogExecutorsTest, 5 cases |
| HTTP pagination defaults, Bean Validation, normalized string IDs/amounts, declared permission annotations | PASS | CatalogHttpContractTest, 4 cases; standalone MVC is not deployed authentication verification |
| Actual JacksonConfig serialization of decimal/large Long/null omission | PASS | CatalogSerializationTest, 1 case |
| Additive migration preserving pre-existing ID/stock/price | PASS | CatalogPersistenceTest on H2, production migration scripts |
| Atomic SPU/SKU rollback, sellability gates, idempotent confirm/release, inventory idempotency/overflow/concurrency, metadata/list behavior | PASS | CatalogPersistenceTest, 10 cases including migration; real gateway transaction proxies and production mappers |
| Menu seed repeatability | PASS | CatalogMenuSeedTest, 1 case; system-menu column fixture, no role-grant table touched |
| React typecheck, lint, format:check | PASS | evidence/catalog/react-verification.txt |
| Frontend unit/API decoder tests | PASS | 141 tests, 26 files, same evidence |
| Frontend optimized build | PASS | Same evidence; existing large vendor chunk warning remains |
| Browser interaction / desktop-mobile visual QA | BLOCKED | Chrome launch failed on socket() Operation not permitted; browser-status.json. Smoke script exists but no interactions executed |
| MySQL migration/concurrent transactions | NOT RUN | H2 is not target MySQL verification |
| Running Nacos/Dubbo registration / gateway auth / live HTTP-to-DB chain | NOT RUN | No running service stack or target configuration supplied |
| Real payment provider signatures/query/close/refund | NOT RUN | Still unimplemented concrete adapters |
| Cart/coupon/full lifecycle and corresponding trade UI | NOT RUN | Scope still pending |

Self-review is not independent QA or user acceptance. Recommendation: keep Draft and do not deploy as production-ready. UI Design and Assets baseline completion is distinct from browser Visual QA; implementation/build evidence does not substitute for it.
