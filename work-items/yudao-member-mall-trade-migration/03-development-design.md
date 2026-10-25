# Development design

v1 · 2026-10-25 · code-software-engineer. Inputs: 01-intake.md v1, existing trade code at a476b90; server AGENTS.md. Reference read: code-software-engineer/references/development-design.md and references/java.md.

## Reliability slice

Keep Application -> Domain gateway -> Infrastructure direction. Outbox dispatch is at-least-once, inventory remains idempotent. Add a unique token per claim. All acknowledgements and failure transitions require matching token and active lease. A lease expiring allows a new worker to acquire ownership; stale workers cannot mutate delivery state. Remote side effects can still repeat and must remain idempotent.

Select only eligible NEW/FAILED records and expired PROCESSING leases before LIMIT. Count attempts on claim, including crashed workers, with a bound of 10. Failed attempts schedule exponential delays (30 seconds doubling, capped at one hour). Exhausted events move to DEAD; explicit replay resets attempts and is permitted only from DEAD. No public unauthenticated replay endpoint. Add V3 additive schema migration with lease_token, lease_until, next_attempt_at and eligibility index; apply before new worker, never with mixed old workers. Rollback requires disabling workers before deploying old code; retain event rows.

Tasks: change existing TradeOutboxGateway/mapper/implementation/DO/dispatcher, add V3 SQL, update existing dispatcher tests and add mapper integration tests for lease takeover, stale ack, delay eligibility, bounded attempts and replay. No HTTP/UI contract changes in this slice. Maven reactor verify must run before calling this validated; MySQL runtime validation remains separate from H2 checks.

## Payment-aware expiry and remaining slices

Expiry cannot safely infer unpaid from missing callback. A real channel must atomically close/reconcile payment intents before local close; absent adapters must fail closed. Keep scheduled automatic close disabled until this contract is implemented and tested. Payment signature verification must use the chosen provider's actual protocol and merchant configuration; no invented generic signature advertised as Alipay/WeChat compatible.

Catalog/cart/coupons/order lifecycle/React remain required follow-up slices; inspect existing UI conventions and reference contracts before implementation. No mock data or unfinished endpoints may be presented as completed integration.

Build repair: add a root aggregation POM for existing modules and explicit parent relative paths in common/server, so a fresh checkout can run `mvn verify` without manually installing the parent. This creates no business module. Temporary Central mirror/proxy settings are environment-only. Mockito inline attachment hangs in this container; use the resolved Byte Buddy agent via Surefire argLine for verification and record this invocation.

Payment race repair: acquire the trade_order row lock in payment creation, notification and close transactions, always before payment rows. Close enumerates all intents and closes only when every intent is locally CLOSED or the concrete provider positively confirms an atomic close of an unpaid intent. Missing provider, unknown, paid or errors keep the order open. An empty intent list is safe because creation uses the same order lock. Provider contract is a domain port, not a claim of channel integration; no provider is configured by default. Late successful callbacks remain a reconciliation/refund gap. Test local lock orchestration and fail-closed policy; live channel verification is BLOCKED without a concrete integration.

Manual compensation API: `POST /trade/outbox/{eventId}/replay`, permission `trade:outbox:replay`. Accept a nonblank identifier up to 64 characters, return `R<Boolean>`; true only for an atomic DEAD -> NEW reset. DELIVERED/NEW/active/unknown events return false, never revive completed tasks. Normal worker handles replay with a fresh token. Operator must first repair the underlying failure and inspect event/order/reservation state. Add executor tests, API documentation and annotation checks. No visual assets are required for this backend endpoint.

Lease and retry deadlines use database `CURRENT_TIMESTAMP`/`TIMESTAMPADD`, rather than worker wall clocks, so host clock skew cannot authorize stale ownership. New inserts should rely on the database default due time.
