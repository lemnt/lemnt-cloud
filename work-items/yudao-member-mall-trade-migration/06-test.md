# Test report

v3 · 2026-10-25 · engineering self-review. Inputs: design v3/UI baseline v2, candidate identified by this report's commit. Prior reports/evidence remain at aaa69ec. Environment: OpenJDK 17.0.20, Maven 3.9.11, two independent H2 MySQL-mode databases, Node 22.13.0 and Yarn 1.22.22.

| Check | Result | Evidence / limit |
| --- | --- | --- |
| Full Maven verify, 71 reactor projects | PASS | evidence/checkout/maven-reactor.txt |
| Available backend suites | PASS | 283 tests, zero failures/errors/skips; maven-tests.json |
| Exact money, percent semantics/cap/rounding, validity, zero quote, template rules | PASS | CheckoutRulesTest, 5 cases |
| Cancel-before-reserve tombstone, stable quantity, concurrent duplicate reserve/cancel, batch availability | PASS | Additional CatalogPersistenceTest cases; actual mall gateway transaction/SQL |
| Cart/coupon/order/payment transactional chain across two databases | PASS | CheckoutFlowIntegrationTest, 18 cases; actual local transaction proxies, test RPC bridge |
| Post-commit lost reserve response, worker restart and cancellation fencing | PASS | Same integration suite; bridge injects an exception after actual mall commit |
| Outbox write failure rolls back order, coupon and checkout before inventory debit | PASS | Same suite; database CHECK fault injection |
| Concurrent identical request and concurrent coupon quota/spend | PASS | Same suite; real database locks/unique constraints |
| Ownership, selected state, expiry/disable, changed quote token, zero-placement rejection | PASS | Same suite; zero quote remains supported, zero order is rejected explicitly |
| Cart edits and delete/re-add preserved | PASS | Same suite, persisted cart ID/version conditions |
| Separate timeout batches and UTC database creation/expiry clock | PASS | Same suite; blocked pending payments do not starve reservation recovery |
| Coupon-state and order/payment-amount corruption rolls back payment | PASS | Same suite, database fault injection; not a signature verification test |
| HTTP validation, permission declarations, IDs/money/date serialization | PASS | CheckoutHttpContractTest, 4 cases; actual Jackson customizer, standalone MVC (not deployed authentication) |
| Repeatable menu seed without role writes | PASS | CheckoutMenuSeedTest, 1 case on system-menu column fixture |
| React typecheck/lint/format:check/build | PASS | evidence/checkout/react-verification.txt |
| React API/decoder/intent storage tests | PASS | 148 tests, 29 files; same evidence |
| Browser interaction / Visual QA | NOT RUN | Previous runtime Unix socket restriction; evidence/checkout/browser-status.json. No new interactions/screenshots claimed |
| Target MySQL migration/isolation/locking | NOT RUN | H2 is not MySQL verification; V4 production script exercised locally, prior V3 represented by fixture |
| Running Nacos/Dubbo/gateway/roles/real payment channel | NOT RUN | Test bridge and permission declarations do not prove deployed integration |
| Full delivery/refund lifecycle and member authentication | NOT RUN | Still pending implementation |

Recommendation: keep Draft; no production-ready or user-acceptance claim. Self-review is not independent QA. Positive local tests do not replace real channel signatures, network failure testing or browser accessibility/visual verification.
