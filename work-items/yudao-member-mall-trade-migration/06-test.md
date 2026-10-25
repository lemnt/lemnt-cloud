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
