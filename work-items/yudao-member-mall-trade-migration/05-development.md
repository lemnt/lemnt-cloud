# Development delivery

v3 · 2026-10-25 · code-software-engineer. Inputs: design v3, UI baseline v2, user continuation. Base: aaa69ec3f7835ed2bd3c80282a7f92f6d119098a; branch feat/yudao-member-mall-trade-migration. Previous delivery/evidence is preserved in git at aaa69ec. Candidate version is the commit containing this report.

## Implemented slice

- Cart and fixed/percent coupon templates/issuance/ownership/state in the existing trade layers. Exact decimal server pricing, quota guards, one instance/member/template, threshold/window checks, owner locking and stable request fingerprints. Admin endpoints explicitly operate on a target member; no fabricated member login identity.
- New order creation (including the legacy endpoint) commits RESERVING order/items, coupon lock, checkout audit snapshot and reserve intent locally before any remote inventory mutation. Outbox reserves idempotently, transitions to PENDING_PAYMENT only after success, and closes/releases on definitive rejection. Separate database-clock timeout batches recover abandoned reservations without blocked payment orders starving the scan. New order timestamps use the same DB clock, independently of JVM zone.
- New mall batch snapshot RPC avoids per-cart-line remote calls. New cancellation RPC writes terminal tombstones for unknown/unattempted reservations. All reserve/confirm/release/cancel paths lock product then reservation, keeping quantity stable and fencing delayed reserve calls after cancellation. Existing SKU/order IDs remain intact.
- Coupon LOCKED -> USED joins the verified payment transaction. Close/failure releases the matching lock. Payment amount must equal order payable, and an inconsistent coupon lock aborts/rolls back payment and order updates together.
- A quoteToken covers exact item snapshots/quantities/prices and coupon totals. Cart submission requires it and rejects changed prices/quantities before placement. Idempotent retries return the original order/price even after cart deletion/repricing. A different key cannot settle the same RESERVING cart rows concurrently; finalization deletes only unchanged cart row IDs/versions, preserving edits and delete/re-add.
- React template create/enable/issue and admin member cart/quote/checkout/receipt pages, using existing Ant Design, Query and permission patterns. Decimal strings and large IDs are decoded safely. Unknown submit results retain an operator-scoped persisted request; storage failure prevents an untracked submission. A confirmed local reset explicitly does not cancel server orders.
- Added trade V4 migration, repeatable system menu seed without role grants, pass-through gateway snippet and docs/trade-checkout-api.md. No production data/configuration was modified.

## Verification / evidence

Full Java 17 Maven reactor: all 71 projects SUCCESS; 283 tests, zero failures/errors/skips. Node 22.13.0/Yarn 1.22.22: typecheck/lint/format:check, 148 tests and optimized build PASS. Evidence: evidence/checkout/.

CheckoutFlowIntegrationTest uses two independent H2 MySQL-mode databases, production gateways/mappers and real local transaction proxies. The RPC bridge can lose a response after the mall transaction commits, then retry/restart or cancel. It is not a live Nacos/Dubbo test. New V4 executes unmodified; existing V3 uses a dialect-compatible fixture because H2 does not support its MySQL multi-action ALTER. Trade sessions use UTC to exercise DB-clock order creation/expiry independently of JVM/mall defaults. Actual application Jackson/MVC binding and coupon date formats are tested separately.

UI Design: baseline COMPLETE (04-ui-assets.md/prototype/checkout-admin.html). Assets: reused existing components/icons, no new binaries or dependencies. Implementation: source/build/tests PASS. Browser/Visual QA: NOT RUN for this slice; previous runtime socket restriction is referenced, not presented as a fresh failed interaction run. Existing large vendor-chunk warning remains.

## Rollout / limitations

Stop old trade workers. Apply V4 after V1/V2/V3; deploy new mall batch/cancel provider before the new trade consumer. Do not mix old release-only consumers with new RESERVING orders. Apply the new system menu seed through reviewed tooling; assign roles explicitly; merge /trade/** gateway routing without StripPrefix. Review historic order time-zone assumptions before enabling expiry. Keep orders/request ledger/tombstones for retry/reconciliation; binary rollback needs checkout stopped and pending work drained/recovered.

Free orders are explicitly unsupported at placement (zero quote is shown, no hidden minimum charge). Coupons are all-product, fixed-date, single-per-template/member; no stacking, relative validity, refund return, or >1000-list paging. Existing pre-V4 orphan holds are not retroactively auto-reconciled. Real prepay/query/signature/close/refund adapters, member self-service authentication, addresses/shipping/delivery/receipt/after-sales/refunds and discount allocation for refunds remain pending. Historical/member/trade administration is still partial. No production-ready claim, merge or deploy; user acceptance pending. Actual cost/time unknown.

Next owner: engineering for payment-provider integration and complete order lifecycle; target MySQL/Dubbo/gateway/browser verification remains separate from local tests.
