# Admin cart, coupon and checkout API

当前服务归属与独立事务说明见 [商城与交易服务边界](mall-trade-boundary.md)。下述接口由 mall 提供；支付及订单履约仍在 trade。
v1 · 2026-10-25 · design v3. Source-checked contracts; examples are constructed, not live gateway/payment evidence.

## Boundary and serialization
All routes are **permission-protected admin operations on a specified target member**, not member self-service. Do not treat the admin userId as a memberId. No new consumer authentication is implemented. IDs accept positive Long-compatible values; actual Jackson returns safe Long as numbers and large Long as strings. UI normalizes IDs to strings. BigDecimal responses are exact strings; null properties are omitted. Validity uses the mall database clock/time zone and left-closed/right-open intervals. Inputs shown as strings are accepted by the actual application mapper.

R envelope: code integer (200 application success), optional msg string, data object/array/boolean. Unused null data/rows/total fields are omitted. Empty arrays are preserved. False does not mean a successful mutation; clients must check it.

## Shared response field schemas (data prefixes apply to the endpoint response)

### CartItem
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| productId | integer/string | SKU编号，沿用现有商品ID |
| quantity | integer | 商品数量 |
| selected | boolean | 是否选中 |
| version | integer/string | 购物车行版本 |
| name | string | 名称，不可售商品可省略 |
| productCode | string | SKU编码，不可售商品可省略 |
| unitPrice | decimal yuan (string on response) | 商品单价，元，JSON字符串 |
| availableStock | integer | 当前可用库存，不保证结算成功 |
| sellable | boolean | 当前SKU可售标识 |

### Coupon
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| templateId | integer/string | 优惠券模板编号 |
| memberId | integer/string | 后台授权操作的目标会员编号，不是当前后台用户ID |
| status | string | 订单RESERVING/PENDING_PAYMENT/PAID/CLOSED，优惠券AVAILABLE/LOCKED/USED |
| orderNo | string | 订单号 |

### CouponTemplate
| Field | Type | Meaning |
| --- | --- | --- |
| id | integer/string | 编号 |
| code | string | 模板编码 |
| name | string | 名称，不可售商品可省略 |
| kind | string | FIXED或PERCENT |
| threshold | decimal yuan (string on response) | 商品金额门槛，元 |
| fixedAmount | decimal yuan (string on response) | 满减金额，元 |
| discountPercent | integer | 支付百分比，80表示8折 |
| discountLimit | decimal yuan (string on response) | 折扣金额上限，元 |
| totalCount | integer | 总发放数量 |
| issuedCount | integer | 已发放数量 |
| validFrom | yyyy-MM-dd HH:mm:ss | 生效时间，yyyy-MM-dd HH:mm:ss |
| validUntil | yyyy-MM-dd HH:mm:ss | 过期时间，右开边界 |
| enabled | boolean | 是否开放新发放及兑换 |

### CheckoutItem
| Field | Type | Meaning |
| --- | --- | --- |
| productId | integer/string | SKU编号，沿用现有商品ID |
| productCode | string | SKU编码，不可售商品可省略 |
| productName | string | 商品名称快照 |
| unitPrice | decimal yuan (string on response) | 商品单价，元，JSON字符串 |
| quantity | integer | 商品数量 |
| lineAmount | decimal yuan (string on response) | 商品行原金额，元 |

### CheckoutQuote
| Field | Type | Meaning |
| --- | --- | --- |
| items | array | 商品价格和数量快照 |
| items[] | CheckoutItem | Complete nested schema above, including prices, quantities and line gross amounts |
| originalAmount | decimal yuan (string on response) | 商品原金额，元 |
| discountAmount | decimal yuan (string on response) | 优惠金额，元 |
| payableAmount | decimal yuan (string on response) | 实付金额，元 |
| couponId | integer/string | 优惠券实例编号，未选择时省略 |
| quoteToken | string | 提交时原样返回，价格或数量变化会拒绝下单 |

### CheckoutReceipt
| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | string | 订单号 |
| memberId | integer/string | 后台授权操作的目标会员编号，不是当前后台用户ID |
| status | string | 订单RESERVING/PENDING_PAYMENT/PAID/CLOSED，优惠券AVAILABLE/LOCKED/USED |
| originalAmount | decimal yuan (string on response) | 商品原金额，元 |
| discountAmount | decimal yuan (string on response) | 优惠金额，元 |
| payableAmount | decimal yuan (string on response) | 实付金额，元 |
| couponId | integer/string | 优惠券实例编号，未选择时省略 |
| items | array | 商品价格和数量快照 |
| items[] | CheckoutItem | Complete nested schema above, including prices, quantities and line gross amounts |

## 1. Read member cart
Address: `/mall/admin/members/{memberId}/cart`. Method: `GET`. Permission: `mall:cart:query`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data[] | CartItem | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "productId": 5,
      "quantity": 2,
      "selected": true,
      "version": 0,
      "name": "SKU样例",
      "productCode": "SKU1",
      "unitPrice": "10.00",
      "availableStock": 10,
      "sellable": true
    }
  ]
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X GET 'https://<API_HOST>/mall/admin/members/1/cart' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 2. Set cart item
Address: `/mall/admin/members/{memberId}/cart/{productId}`. Method: `PUT`. Permission: `mall:cart:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| productId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| quantity | integer | yes | body | none | 购物数量，1至999; @NotNull @Min(1) @Max(999) |
| selected | boolean | yes | body | none | 是否选中; @NotNull |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | CartItem | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "productId": 5,
    "quantity": 2,
    "selected": true,
    "version": 0,
    "name": "SKU样例",
    "productCode": "SKU1",
    "unitPrice": "10.00",
    "availableStock": 10,
    "sellable": true
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X PUT 'https://<API_HOST>/mall/admin/members/1/cart/5' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"quantity":2,"selected":true}'
```

## 3. Delete cart item
Address: `/mall/admin/members/{memberId}/cart/{productId}`. Method: `DELETE`. Permission: `mall:cart:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| productId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | Boolean | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X DELETE 'https://<API_HOST>/mall/admin/members/1/cart/5' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 4. List coupon templates
Address: `/mall/admin/coupon-templates`. Method: `GET`. Permission: `mall:coupon-template:list`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| none | — | — | — | — | No request parameters |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data[] | CouponTemplate | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": [
    {
      "id": 2,
      "code": "C1",
      "name": "满20减3",
      "kind": "FIXED",
      "threshold": "20.00",
      "fixedAmount": "3.00",
      "totalCount": 100,
      "issuedCount": 1,
      "validFrom": "2026-10-25 00:00:00",
      "validUntil": "2026-11-25 00:00:00",
      "enabled": true
    }
  ]
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X GET 'https://<API_HOST>/mall/admin/coupon-templates' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 5. Create coupon template
Address: `/mall/admin/coupon-templates`. Method: `POST`. Permission: `mall:coupon-template:add`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| code | string | yes | body | none | 模板编码; @NotBlank @Size(max=64) |
| name | string | yes | body | none | 优惠券名称; @NotBlank @Size(max=100) |
| kind | string | yes | body | none | FIXED满减或PERCENT折扣; @NotBlank @Pattern(regexp="FIXED or PERCENT") |
| threshold | decimal yuan (string on response) | yes | body | none | 最低商品金额，元; @NotNull @DecimalMin("0") @Digits(integer=17,fraction=2) |
| fixedAmount | decimal yuan (string on response) | no | body | none | 满减金额，元，仅FIXED填写; @DecimalMin("0.01") @Digits(integer=17,fraction=2) |
| discountPercent | integer | no | body | none | 支付百分比，80表示8折，仅PERCENT填写; @Min(1) @Max(99) |
| discountLimit | decimal yuan (string on response) | no | body | none | 折扣金额上限，仅PERCENT可填写; @DecimalMin("0.01") @Digits(integer=17,fraction=2) |
| totalCount | integer | yes | body | none | 总发放数量，每人每模板限一张; @NotNull @Min(1) @Max(1000000) |
| validFrom | yyyy-MM-dd HH:mm:ss | yes | body | none | 数据库同一时区，yyyy-MM-dd HH:mm:ss; @NotNull |
| validUntil | yyyy-MM-dd HH:mm:ss | yes | body | none | 有效期右开边界，yyyy-MM-dd HH:mm:ss; @NotNull |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | CouponTemplate | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "code": "C1",
    "name": "满20减3",
    "kind": "FIXED",
    "threshold": "20.00",
    "fixedAmount": "3.00",
    "totalCount": 100,
    "issuedCount": 1,
    "validFrom": "2026-10-25 00:00:00",
    "validUntil": "2026-11-25 00:00:00",
    "enabled": true
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X POST 'https://<API_HOST>/mall/admin/coupon-templates' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"code":"C1","name":"满20减3","kind":"FIXED","threshold":"20.00","fixedAmount":"3.00","totalCount":100,"validFrom":"2026-10-25 00:00:00","validUntil":"2026-11-25 00:00:00"}'
```

## 6. Enable or disable template
Address: `/mall/admin/coupon-templates/{id}/enabled`. Method: `PUT`. Permission: `mall:coupon-template:edit`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| id | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| enabled | boolean | yes | body | none | 启用新发放及新兑换，已锁定订单不受影响; @NotNull |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | Boolean | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": true
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X PUT 'https://<API_HOST>/mall/admin/coupon-templates/2/enabled' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"enabled":false}'
```

## 7. Issue coupon to member
Address: `/mall/admin/members/{memberId}/coupons/{templateId}`. Method: `POST`. Permission: `mall:coupon:issue`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| templateId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | Coupon | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "id": 8,
    "templateId": 2,
    "memberId": 1,
    "status": "AVAILABLE"
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X POST 'https://<API_HOST>/mall/admin/members/1/coupons/2' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 8. List member coupons
Address: `/mall/admin/members/{memberId}/coupons`. Method: `GET`. Permission: `mall:coupon:list`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data[] | Coupon | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": [
    {
      "id": 8,
      "templateId": 2,
      "memberId": 1,
      "status": "AVAILABLE"
    }
  ]
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X GET 'https://<API_HOST>/mall/admin/members/1/coupons' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## 9. Preview checkout
Address: `/mall/admin/members/{memberId}/checkout/quote`. Method: `POST`. Permission: `mall:checkout:quote`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| productIds | array of positive IDs | yes | body | none | 该会员购物车中已选中的SKU编号，1至100项; @NotEmpty @Size(max=100) |
| couponId | integer/string | no | body | none | 该会员的优惠券实例编号，可不传; @Positive |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | CheckoutQuote | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "productId": 5,
        "productCode": "SKU1",
        "productName": "SKU样例",
        "unitPrice": "10.00",
        "quantity": 2,
        "lineAmount": "20.00"
      }
    ],
    "originalAmount": "20.00",
    "discountAmount": "3.00",
    "payableAmount": "17.00",
    "couponId": 8,
    "quoteToken": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X POST 'https://<API_HOST>/mall/admin/members/1/checkout/quote' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"productIds":["5"],"couponId":"8"}'
```

## 10. Submit checkout
Address: `/mall/admin/members/{memberId}/checkout`. Method: `POST`. Permission: `mall:checkout:create`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| memberId | positive integer/string | yes | path | none | Explicit target identifier; memberId is not the current admin account |
| requestNo | string | yes | body | none | 该会员内唯一幂等请求编号，同编号必须用于相同请求; @NotBlank @Size(max=64) |
| productIds | array of positive IDs | yes | body | none | 该会员购物车中已选中的SKU编号，1至100项; @NotEmpty @Size(max=100) |
| couponId | integer/string | no | body | none | 该会员的优惠券实例编号，可不传; @Positive |
| quoteToken | string | yes | body | none | 预览返回的报价令牌，提交时必须原样带回; @NotBlank @Pattern(regexp="[a-f0-9]{64}") |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | CheckoutReceipt | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "productId": 5,
        "productCode": "SKU1",
        "productName": "SKU样例",
        "unitPrice": "10.00",
        "quantity": 2,
        "lineAmount": "20.00"
      }
    ],
    "originalAmount": "20.00",
    "discountAmount": "3.00",
    "payableAmount": "17.00",
    "couponId": 8,
    "orderNo": "TR_SAMPLE",
    "memberId": 1,
    "status": "RESERVING"
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X POST 'https://<API_HOST>/mall/admin/members/1/checkout' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' --data '{"requestNo":"checkout-example-001","productIds":["5"],"couponId":"8","quoteToken":"<ACTUAL_QUOTE_TOKEN_FROM_PREVIEW>"}'
```

## 11. Read checkout receipt
Address: `/mall/admin/checkouts/{orderNo}`. Method: `GET`. Permission: `mall:checkout:query`.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| orderNo | string | yes | path | none | Explicit target identifier; memberId is not the current admin account |

Response parameters:
| Field | Type | Meaning |
| --- | --- | --- |
| code | integer | 200 on application success |
| msg | string/omitted | Existing R message |
| data | CheckoutReceipt | Shared complete schema above; nested items[] use CheckoutItem |

Constructed response:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "productId": 5,
        "productCode": "SKU1",
        "productName": "SKU样例",
        "unitPrice": "10.00",
        "quantity": 2,
        "lineAmount": "20.00"
      }
    ],
    "originalAmount": "20.00",
    "discountAmount": "3.00",
    "payableAmount": "17.00",
    "couponId": 8,
    "orderNo": "TR_SAMPLE",
    "memberId": 1,
    "status": "RESERVING"
  }
}
```

Constructed request (replace host/token/quote token):
```bash
curl -X GET 'https://<API_HOST>/mall/admin/checkouts/TR_SAMPLE' -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

## Pricing and request semantics
- Cart holds 1..999 units per SKU, up to 200 distinct rows/member. It does not reserve stock. Saving an unavailable SKU is allowed only to deselect an existing row; adding/selecting unavailable SKUs is rejected. Server reads use the local mall catalog in a bounded batch; query failure is an error, not an empty cart.
- Checkout selects 1..100 distinct owned, selected SKU IDs. The server reads actual quantities and current catalog prices. No client price/amount is trusted. SKU rows already referenced by a RESERVING checkout cannot be submitted again with a new key.
- Quote original/discount/payable are exact yuan. FIXED discounts cap at gross; PERCENT uses pay-percent semantics (80 means 8折), discount rounds down to cents and respects its optional cap. No coupon stacking or per-product scope is implemented.
- Quote token covers stable item quantities/prices/names and coupon totals. Submit requires the exact preview token and rejects any changed quote before persisting the order. The token is not an authentication/signature mechanism; the server still computes all amounts and eligibility. It is included in the member-scoped idempotency fingerprint.
- Same member + requestNo + same product IDs/coupon/token returns the original order/pricing even after cart deletion or catalog repricing. Reused key with different contents rejects. Never replace the key after an unknown network result; retry the original payload. UI persists this payload in operator-scoped browser storage before sending. Storage failure prevents submission. Local reset does not cancel a possibly created order and requires explicit confirmation.
- A valid quote may show zero payable; placement **explicitly rejects zero-payable orders** because free-order/payment support is not implemented. It does not silently add a minimum charge.
- Immutable templates support fixed date windows, bounded quota and one instance/member/template. Dates are in the mall DB time zone, not a guessed browser zone. Enable/disable affects new issue/redemption only; existing locked orders keep their snapshot. Expired/disabled coupons are rejected during fresh checkout; AVAILABLE alone is not a guarantee of eligibility.

## Reservation and coupon state transitions
1. Mall commits the request association, optional LOCKED coupon and cart-version references. It then submits the confirmed server snapshot via TradeOrderClient; trade atomically saves RESERVING order/items and TRADE_ORDER_RESERVE. No remote stock mutation precedes the trade commit.
2. The trade Outbox reserves each SKU with stable order/product keys. Once all succeed, trade enters PENDING_PAYMENT and notifies mall to consume unchanged cart versions. Mall failures retain the event for retry, including when the order already entered PENDING_PAYMENT.
3. Definitive stock rejection closes the trade order and writes a CLOSED event. That event releases reservations and the mall coupon in independently committed, idempotent operations. Unknown results use the existing bounded retry/DEAD worker.
4. Payment notification validates the local payment/order invariants and commits PAID plus its event. Mall then validates and uses the locked coupon. Mall failure does not undo an already committed payment; the failed event remains retriable. Close callbacks likewise release only the matching order's coupon.
5. These are two local database transactions with retriable RPC delivery. They are not one cross-service transaction. See [the current service boundary](mall-trade-boundary.md).

## Legacy order creation compatibility change
Existing `POST /mall/orders` remains admin permission `mall:order:create`. Its request now optionally includes `requestNo` (<=64 characters) and owned `couponId`. It delegates to the same durable reservation flow and **returns RESERVING initially**, not immediate PENDING_PAYMENT. Clients must query order state and must not attempt payment during RESERVING. Omitting requestNo generates a legacy key and therefore cannot make independent HTTP retries idempotent. Existing order/item IDs and normal order detail shapes are preserved.

## Error / empty-state contract
Bean Validation/MVC rejects malformed request fields, missing quoteToken, invalid quantities/IDs and bad date formats before executor calls (tested). Domain rule failures, reused-key conflicts, foreign/locked/expired coupons, stock shortages and storage/RPC faults use the existing global error envelope. Deployed gateway authentication/error status behavior is NOT RUN; standalone permission-annotation tests do not prove live authentication. A missing receipt returns existing R.fail; an empty list returns data:[]. No new error envelope is invented.

## Setup and recovery
- Stop old shopping writes and workers. Apply the mall table/data migration described in mall-trade-boundary.md; do not apply a destructive down migration. New mall provider batch/cancellation RPC methods must be deployed **before** the new trade worker. Do not mix the old closed-event consumer with new RESERVING orders: the old release API cannot tombstone unattempted rows.
- Merge `docs/config/trade-gateway-route.yaml` into existing Nacos routes without StripPrefix, then verify admin authentication/permissions and real Dubbo behavior. The snippet is not applied by this PR.
- Apply `lemnt-server/sql/V20261025__mall_checkout_menu.sql` to the system DB via reviewed migration tooling. It inserts mall/coupon/index and mall/cart/index menu entries and action permissions, without granting roles or overwriting existing menus. Assign least-privilege roles explicitly.
- Enable the existing optional trade scheduler only on reviewed workers. Separate bounded RESERVING and PENDING_PAYMENT expiry scans use the database clock. A missing real payment adapter leaves pending-payment orders open; it must never infer unpaid from absent callbacks.
- After repair, replay DEAD reservation/close/paid intents through the existing permission-protected Outbox replay API. Closed reservation intents become harmless; cancellation events must still complete. Tombstones must be retained while old RPCs/retries may arrive.
- Target MySQL/live Nacos/Dubbo/real-channel/browser validation remains necessary. H2 tests use actual mall schemas and production gateways against two isolated DBs; the pre-existing V3 schema is represented with a dialect-compatible fixture, not claimed as an executed MySQL migration.
- Rollback requires stopping workers and new checkout traffic; old code does not understand RESERVING or coupon locks. Drain/recover pending intents before any binary rollback. Keep checkout/order/stock ledger rows for reconciliation.

## Deliberate remaining scope
Zero-payable orders, product-scoped/relative-validity coupons, repeated per-template claims, coupon return on refunds, member address book, carrier integration, split shipments, after-sales/refund execution, real provider prepay/query/signature adapters, and member/trade analytics remain pending. Lists above 1000 coupon instances/templates require paging rather than silent truncation. New UI monetary inputs are bounded to 99,999,999,999.99 yuan for numeric editing; server-side amounts remain exact strings.

## Fulfillment continuation

Current order states also include SHIPPED and COMPLETED. See [trade-fulfillment-api.md](trade-fulfillment-api.md) for inventory confirmation gates, admin shipment/receipt and pre-shipment refund requests. Refund requests do not mark a payment refunded or return inventory/coupons.
