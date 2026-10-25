# Miniapp API boundary

Current source implementation; not deployed or end-to-end verified. Admin endpoints remain under their existing paths. New consumer endpoints use `/trade/app` and an independent member session, not admin role permissions or a caller-provided member ID.

| Method/path | Request | Response data |
| --- | --- | --- |
| GET /trade/app/cart | none | CartItemRes[] |
| PUT /trade/app/cart/{productId} | CartSaveReq: quantity, selected | CartItemRes |
| DELETE /trade/app/cart/{productId} | none | boolean |
| GET /trade/app/coupons | none | CouponRes[] |
| POST /trade/app/checkout/quote | CheckoutQuoteReq: productIds, couponId optional | CheckoutQuoteRes |
| POST /trade/app/checkout | CheckoutSubmitReq: productIds, couponId optional, requestNo, quoteToken | CheckoutReceiptRes |
| GET /trade/app/orders/{orderNo} | none | CheckoutReceiptRes |

Schemas, validation, amount strings and idempotency semantics reuse [trade-checkout-api.md](trade-checkout-api.md). Responses use existing R wrapper. Order endpoint is a checkout receipt, not full shipment/detail or all-orders pagination. Foreign/missing orders both return application failure `订单不存在`. It checks ownership on both order and checkout record before returning a DTO.

Every entry first calls MemberSessionService with Authorization. Missing/invalid member sessions return HTTP 401 with a generic message. Header/query/body memberId is not used to establish identity. Active-member validation is mandatory after authentication. Existing admin SecurityContext is never a fallback.

## Authentication integration is pending

MemberSessionGatewayImpl requires exactly one MemberSessionVerifier implementation. No implementation is provided in this repository yet: **all consumer requests are denied by default**. The port must ultimately use authoritative independent member sessions with token audience, expiry and revocation validation, and reject admin tokens. Unit tests use a test verifier; they are not real login/audience validation. Do not deploy a fixed-ID verifier.

The gateway's existing admin authentication chain is unchanged. Independent member route/session validation and safe identity propagation still need implementation. Do not whitelist these routes to make them appear functional. Keep miniapp memberApiReady false. No login/session issuance endpoint, app prepay or app payment-status endpoint is fabricated.

## Local validation coverage

Tests exercise malformed/missing tokens, no/ambiguous verifier denial, active-member validation, session-derived identity instead of spoofed request headers/query, and foreign order concealment. An actual two-database checkout fixture verifies owner can read its receipt while another member cannot. They do not prove live gateway/authentication or real payment behavior.

## Cached settlement contract update

App quote now creates a Redis-backed settlement and returns settlementId/expiresAt with the server quote. App checkout now accepts only settlementId/requestNo. Prior quoteToken/productIds submit schema above is superseded for consumers. See [cached-settlement-api.md](cached-settlement-api.md); admin compatibility contracts are unchanged.

## Local payment record query

GET /trade/app/orders/{orderNo}/payment is now implemented as a member-scoped **local** status query with channelVerified=false, not a real provider query. Prepay remains unimplemented. See miniapp-local-payment-status.md; local WAITING must not authorize closing/releasing inventory.


## Member authority implementation update

Member password/SMS/WeChat Mini/MP authority and authoritative Dubbo verifier are now implemented in the existing member module. Prior statements that no verifier exists describe the earlier scaffold. See [member-api.md](member-api.md) for endpoints, V2/V3 SQL, gateway route and credential requirements. This does not enable the native client automatically: memberApiReady remains false until deployment/provider/device verification. Trade still resolves member identity from its independent token, never admin headers.


## Storefront/coupon continuation v7

New public mall/app product/category browsing and authenticated coupon-center/wallet/claim plus idempotent cart-add routes are documented in [storefront-marketing-api.md](storefront-marketing-api.md). Native startup now uses the product listing; existing cart/settlement routes remain. Trade V8 and strict gateway read rules are required; readiness remains false. Public template claim defaults closed, separately controlled by admin. This is not full migration of seconds sale/group-buy/bargain/stacking or provider payment.
