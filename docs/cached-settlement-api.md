# Cached miniapp settlement

2026-10-25. This supersedes the app quoteToken/productIds submit contract in miniapp-api.md. Admin `/trade/admin` and legacy `/trade/orders` compatibility endpoints remain unchanged; the consumer miniapp exclusively uses this cached flow. No live deployment is implied.

## Generate

`POST /trade/app/checkout/quote` with `{ "productIds": ["5"], "couponId": "8" }` (coupon optional). Independent member session required. Server locks the member while reading selected cart rows and pricing, creates a random settlement ID and immutable quote/cart-version references, then writes Redis using NX and the remaining lifetime. A failed Redis write returns no usable settlement. Lifetime is 15 minutes from server generation, not from client receipt. Generation does not reserve stock or lock a coupon.

Success data fields:

| Field | Meaning |
| --- | --- |
| settlementId | UUID, required for submission |
| expiresAt | ISO-8601 UTC expiration |
| items | server-priced CheckoutItemRes[] |
| originalAmount | decimal yuan string |
| discountAmount | decimal yuan string |
| payableAmount | decimal yuan string |
| couponId | optional owned coupon instance ID |

Amounts/IDs retain existing Jackson behavior. Do not submit productIds, couponId, quantities or prices to order creation. Redis key is `trade:settlement:v1:{settlementId}`; protect it from client access and retain normal Redis access controls.

## Submit

`POST /trade/app/checkout`:

```json
{"settlementId":"12345678-1234-1234-1234-123456789abc","requestNo":"REQUEST-1"}
```

Returns existing CheckoutReceiptRes (`RESERVING` first). Member identity comes exclusively from the independent authenticated member session. Within the trade transaction:

1. Lock member and look up durable request ledger. Same member/request + same settlement returns original order even when Redis has expired or is unavailable; same key/different settlement rejects.
2. Reject settlement already consumed by another request. Durable V6 primary-key association prevents reuse; it joins the same transaction as order/items/checkout/coupon lock/Outbox writes.
3. Read Redis snapshot; check ID, owner and expiration. Missing/foreign/expired snapshots cannot fall back to client data or regenerate pricing silently.
4. Check active membership, pending cart reservation and current stock/sellability/coupon validity. Repricing is only a guard: if it differs from cached quote, reject and require a new settlement. Order fields and quantities are generated from the cached snapshot. Cart changes after generation do not rewrite the snapshot; recorded row/version cleanup preserves later edits.
5. Persist RESERVING order and reserve Outbox event, plus settlement-order association. Any local failure rolls back all those writes. Remote stock reservation remains Outbox-driven.

Redis snapshot is **not deleted before or after order creation**; TTL cleans it up. DB mapping and request ledger, rather than a fragile cache delete, enforce one order/settlement and replay. Retain durable ledgers for reconciliation. A different requestNo cannot reuse the settlement and must query/retry the original request.

## Client compatibility and recovery

Miniapp persists only `{settlementId,requestNo}` under member-scoped `lemnt.checkout.v2.*` before transport. It blocks legacy v1 unresolved records rather than silently losing an unknown result. Current UI shows expiry and can retry original payload. Expired/unusable requests require reconciliation before generating a new settlement. Explicitly confirmed **local-only** reset is provided: it does not cancel server orders and warns that a new settlement can create another order. Never use reset as automatic retry logic.

Old clients submitting quoteToken/productIds to app checkout fail validation. Coordinate client/server rollout and reconcile unresolved v1 requests using the prior version; do not route those payloads through the new app API. Existing admin compatibility endpoints retain their old behavior, not an alternate miniapp route.

## Setup and verification limits

Apply additive trade V6 after V1..V5 before enabling the new app API. Trade now directly depends on existing lemnt-common-redis and requires configured Redis (`spring.data.redis.*` via Nacos/environment); no standalone cache module is duplicated. Redis cache serialization uses existing RedisSerializer. No live SQL, Nacos or Redis configuration was changed.

Tests cover actual serializer round-trip, mocked Redis NX/TTL/failure/miss, actual H2 V6/mappers/transactions for concurrent consume, replay after eviction, owner/expiry rejection, cached quantity, price change and rollback. These are not a live Redis/MySQL/Nacos/WeChat verification. Member identity verifier still has no production implementation, so consumer requests remain denied by default and miniapp readiness stays false. Real payment is still pending.

## Recover by original request

`GET /trade/app/checkout/requests/{requestNo}` returns the current member's CheckoutReceiptRes when a committed request ledger/order exists. Requires independent member session; no member ID input. It checks both ledger and order ownership. This is read-only and does not use Redis or create/retry orders. Both cached and legacy request-ledger records can be recovered. Missing or foreign requests return the same application failure; that failure does **not** prove a queued/in-flight request was cancelled or can no longer create an order.

Miniapp “按原请求找回订单” checks local v1 first, otherwise v2. Only a valid receipt removes the exact inspected local record; unknown results, session switches or changed local records leave it intact. Recovering a legacy record does not remove a coexisting v2 record. This supports upgrade reconciliation without resubmitting old payloads or blindly discarding them. Local-reset remains explicitly confirmed and is never automatic recovery.

## Multiple settlements from one cart version

Settlement-ID uniqueness is not sufficient when several previews reference the same cart rows. Cached submission now checks the recorded cart ID/version against all non-CLOSED orders while holding the member lock, in addition to the existing RESERVING check by cart ID. Once one order consumes that version, a different cached settlement cannot purchase the same version after reservation completes or cart cleanup runs. This also applies when a later cart edit preserved the row: the old version is consumed, but a fresh preview of the new version can be ordered. CLOSED orders do not consume the version for a new settlement. Original request replay still returns the original order before these guards. Existing tables/index are reused; no new migration.

## Confirmed abandonment of an uncompleted submission

`POST /trade/app/checkout/abandon` accepts the same `{settlementId,requestNo}` and independent member authentication. Result data is either `{abandoned:true}` or `{abandoned:false,order:CheckoutReceiptRes}`. This does **not** close or refund an order.

Under the same member lock as submit, first resolve the original request and settlement-order mapping. If an order already exists, return it unchanged (including when another request key consumed the same settlement). Otherwise persist a member/settlement tombstone in V7. Submit checks that tombstone before any Redis access, so later-arriving submissions for the same member/settlement are rejected even with a still-valid cached quote. Repeating abandon is idempotent; repeating a successful original submission still returns the existing order. No Redis access or deletion is needed for abandon. A member-scoped tombstone for a foreign settlement ID cannot affect the rightful owner's submission or reveal their order.

Miniapp asks for confirmation, sends the durable pending v2 identity, and clears only the unchanged local record after a valid server result. Unknown responses, transport failures or member changes preserve the record. Existing-order response is shown for further follow-up, not called cancelled. V2 records can no longer be removed through local-only discard. Legacy v1 records lack settlement identity, so retain the previous recovery/manual-confirmation workflow; local discard affects only v1 and never deletes a coexisting v2 record.

Apply additive V7 after V6 before deploying these handlers. All instances serving cached submit must check abandonment; do not mix older handlers that ignore the marker. Tombstones and request ledgers must be retained for delayed-request reconciliation; no automatic purge was added. No live migration performed. Real Redis/MySQL/gateway/member/payment/device checks remain pending.
