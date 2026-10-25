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
