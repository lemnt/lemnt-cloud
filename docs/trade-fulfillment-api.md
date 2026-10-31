# Trade fulfillment API

v3 · 2026-10-27 · actual Req/Res + Java 17 implementation. Examples are constructed contract examples, not deployed HTTP captures.

Admin authentication/permissions use the existing security stack. Paths retain `/trade` through the gateway. This is administrative fulfillment, not a member self-service identity boundary. No role grants are included.

`R` wraps success in `code=200`, `msg` and `data`; null properties can be omitted. BigDecimal response amounts are **two-decimal yuan strings**. Long IDs can be numbers or strings; the UI normalizes to strings without losing precision. Dates use existing `yyyy-MM-dd HH:mm:ss` serialization and database/session time, not a new UTC wire convention. Order create/expiry and fulfillment timestamps share the trade DB clock; deployment must explicitly align DB session assumptions.

## States and guarantees

`RESERVING -> PENDING_PAYMENT -> PAID -> SHIPPED -> COMPLETED`. Existing reservation/payment close paths end in CLOSED. Successful full-order payment refunds move PAID to REFUNDED; see payment-management-api.md. All stock confirmation calls must return successfully before the consumer persists a confirmation marker. Shipment requires PAID + marker + no active refund. Payment post-processing is synchronous and idempotent for PAID/SHIPPED/COMPLETED; replay after REFUNDED preserves the payment fact without confirming stock or coupons again. No Outbox is used.

Shipping, receipt, refund registration and rejection serialize on the same order row and join one local trade transaction. A shipment is one immutable whole-order record, not split shipments or editable tracking. There is no carrier API/tracking-event integration or automatic receipt. Shipping recipient fields are captured by the admin at shipment time; they are not an authenticated member address book or an order-creation address snapshot.

The legacy refund-registration endpoint supports **pre-shipment full-order requests**. New item-based partial refunds and post-shipment return/refund flows are documented in order-after-sale-logistics-api.md. Exactly one matching successful payment must exist. Server copies member/payment/payable amount; callers cannot choose the amount. REQUESTED/REFUNDING block shipping; REJECTED releases that block. REFUNDED is completed only after a verified payment refund. Registration/rejection never calls a provider, marks a payment refunded, replenishes stock, or returns coupons. The payment-refund orchestration is documented in payment-management-api.md; real channel adapters remain unconnected. Do not treat a request as a completed refund or use rejection to represent an external refund.

## Endpoints

| Method/path | Permission | Request | Response `data` |
| --- | --- | --- | --- |
| GET /trade/admin/orders | trade:fulfillment:list | OrderPageReq query | OrderPageRes |
| GET /trade/admin/orders/{orderNo} | trade:fulfillment:query | orderNo path | OrderDetailRes, includes recipient PII |
| POST /trade/admin/orders/{orderNo}/shipment | trade:fulfillment:ship | ShipmentCreateReq JSON | ShipmentRes |
| POST /trade/admin/orders/{orderNo}/receive | trade:fulfillment:receive | no body | ShipmentRes |
| POST /trade/admin/orders/{orderNo}/refund-requests | trade:refund:request | RefundRequestReq JSON | RefundRequestRes |
| GET /trade/admin/refund-requests | trade:refund:list | RefundPageReq query | RefundPageRes |
| GET /trade/admin/refund-requests/{refundNo} | trade:refund:query | refundNo path | RefundRequestRes |
| POST /trade/admin/refund-requests/{refundNo}/reject | trade:refund:reject | RefundRejectReq JSON | RefundRequestRes |

Order/refund identifiers are trimmed nonblank text, maximum 40 characters. Pagination defaults to pageNum=1/pageSize=20, accepts 1..1,000,000 and 1..100 respectively. orderNo is an exact filter (blank means all). Order status also accepts REFUNDED; after-sale status also accepts AWAITING_RETURN/RETURNED/WAIT_REFUND/RETURN_REJECTED for the item-based workflow. Unknown status rejects. Missing detail returns application failure, not a fabricated empty order. Order list excludes recipient name, phone and address; separately permitted detail loads them on demand. Apply PII retention, audit and access rules through the deployment's existing admin controls; no new retention automation is implemented.

## Request schemas

All command strings are required, trimmed and nonblank. Limits: requestNo 64; carrierCode 40; trackingNo 100; recipientName 100; phone 32 (5..32 characters `[+0-9() -]`); address/reason 500. No client amount, status, timestamp or member ID command field exists.

### OrderPageReq

| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | String | 精确订单编号，可空 |
| status | String | 状态筛选，可空 |
| pageNum | Integer | 页码，从1开始，默认1 |
| pageSize | Integer | 每页数量，默认20，最大100 |

### RefundPageReq

| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | String | 精确订单编号，可空 |
| status | String | 状态筛选，可空 |
| pageNum | Integer | 页码，从1开始，默认1 |
| pageSize | Integer | 每页数量，默认20，最大100 |

### RefundRejectReq

| Field | Type | Meaning |
| --- | --- | --- |
| reason | String | 驳回原因 |

### RefundRequestReq

| Field | Type | Meaning |
| --- | --- | --- |
| requestNo | String | 幂等请求编号 |
| reason | String | 申请原因 |

### ShipmentCreateReq

| Field | Type | Meaning |
| --- | --- | --- |
| requestNo | String | 幂等请求编号 |
| carrierCode | String | 承运商编码 |
| trackingNo | String | 运单编号 |
| recipientName | String | 收件人姓名 |
| phone | String | 收件联系电话 |
| address | String | 收件详细地址 |

## Response schemas

### OrderDetailRes

| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | String | 订单编号 |
| memberId | Long | 会员ID |
| totalAmount | java.math.BigDecimal | 应付金额，人民币元 |
| status | String | 订单状态：RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED |
| inventoryConfirmed | boolean | 全部订单库存是否已确认 |
| activeRefund | boolean | 是否有待处理退款申请 |
| items | java.util.List<CheckoutItemRes> | 订单商品明细 |
| shipment | ShipmentRes | 物流记录，包含收件人信息，仅详情权限可见 |

### OrderPageRes

| Field | Type | Meaning |
| --- | --- | --- |
| records | java.util.List<OrderSummaryRes> | 分页记录 |
| total | long | 总记录数 |

### OrderSummaryRes

| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | String | 订单编号 |
| memberId | Long | 会员ID |
| totalAmount | java.math.BigDecimal | 应付金额，人民币元 |
| status | String | 订单状态：RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED |
| createTime | java.time.LocalDateTime | 订单创建时间 |
| inventoryConfirmed | boolean | 全部订单库存是否已确认 |
| activeRefund | boolean | 是否有待处理退款申请 |
| carrierCode | String | 承运商编码 |
| trackingNo | String | 运单编号 |
| shippedAt | java.time.LocalDateTime | 发货时间，数据库时间 |
| receivedAt | java.time.LocalDateTime | 收货时间，未收货为空 |

### RefundPageRes

| Field | Type | Meaning |
| --- | --- | --- |
| records | java.util.List<RefundRequestRes> | 分页记录 |
| total | long | 总记录数 |

### RefundRequestRes

| Field | Type | Meaning |
| --- | --- | --- |
| refundNo | String | 退款申请编号 |
| orderNo | String | 订单编号 |
| requestNo | String | 幂等请求编号 |
| memberId | Long | 会员ID |
| paymentNo | String | 成功支付编号 |
| amount | java.math.BigDecimal | 整单申请金额，人民币元；不是已退款金额 |
| reason | String | 申请原因 |
| status | String | 申请状态：REQUESTED待处理、REJECTED已驳回、REFUNDING退款中、REFUNDED已退款 |
| decisionReason | String | 驳回原因 |
| requestedAt | java.time.LocalDateTime | 申请时间 |
| decidedAt | java.time.LocalDateTime | 驳回时间 |

### ShipmentRes

| Field | Type | Meaning |
| --- | --- | --- |
| orderNo | String | 订单编号 |
| requestNo | String | 幂等请求编号 |
| carrierCode | String | 承运商编码 |
| trackingNo | String | 运单编号 |
| recipientName | String | 收件人姓名 |
| phone | String | 收件联系电话 |
| address | String | 收件详细地址 |
| shippedAt | java.time.LocalDateTime | 发货时间，数据库时间 |
| receivedAt | java.time.LocalDateTime | 收货时间，未收货为空 |

## Constructed examples

Shipping request, same requestNo and identical normalized payload retained on retry:

```http
POST /trade/admin/orders/TR1/shipment
Content-Type: application/json

{"requestNo":"SHIP-1","carrierCode":"SF","trackingNo":"T1","recipientName":"测试收件人","phone":"13800000000","address":"测试地址"}
```

Success (null receivedAt omitted):

```json
{"code":200,"data":{"orderNo":"TR1","requestNo":"SHIP-1","carrierCode":"SF","trackingNo":"T1","recipientName":"测试收件人","phone":"13800000000","address":"测试地址","shippedAt":"2026-10-25 10:00:00"}}
```

An existing shipment replays only for the same requestNo and fingerprint. A new key or changed payload rejects, even after receipt. Receipt replays preserve original receivedAt. Query the server before opening a new form after an unknown outcome; current UI preserves the request key while the modal stays open and does not persist PII across reloads.

Refund request and rejection:

```http
POST /trade/admin/orders/TR1/refund-requests
Content-Type: application/json

{"requestNo":"REFUND-1","reason":"不再需要"}
```

```json
{"code":200,"data":{"refundNo":"RF1","orderNo":"TR1","requestNo":"REFUND-1","memberId":1,"paymentNo":"PAY1","amount":"17.00","reason":"不再需要","status":"REQUESTED","requestedAt":"2026-10-25 10:00:00"}}
```

```http
POST /trade/admin/refund-requests/RF1/reject
Content-Type: application/json

{"reason":"核验后驳回，已与申请人确认"}
```

Same order/requestNo + same reason returns the original request, including its current REJECTED state. Changed reason with the same key rejects. Only one active request per order; following a rejection, an explicit new key may register a new request. Rejection retries require the same decision reason and preserve original decidedAt. The separate permission-protected payment-refund endpoint initiates or synchronizes the reverse payment transaction; see payment-management-api.md.

## Setup and compatibility

- Stop old trade consumers; apply additive `lemnt-trade/sql/V5__order_fulfillment.sql` after V1..V4 before deploying the new consumer. No live migration was executed. V5 backfills markers only for historical PAID orders with a DELIVERED paid event; review stock integrity beforehand. Orders without trustworthy delivered confirmation remain blocked and require reconciliation/replay rather than hand-marking shipped.
- All trade workers must understand SHIPPED/COMPLETED paid-event retries; old workers reject those states. Existing clients/checkout decoders must accept the two added statuses. Do not roll back to an old consumer while such orders exist without draining/reconciling its work.
- Apply the reviewed additive `lemnt-server/sql/V20261025__trade_fulfillment_menu.sql` serially and assign permissions explicitly; it is repeatable, grants no roles and does not overwrite existing menus. UI component keys: trade/order/index and trade/refund/index. Existing trade gateway route remains pass-through.
- No new npm dependency. Target MySQL migration/isolation, live Dubbo/Nacos/gateway roles, real payment signatures/refunds and browser interaction remain unverified. H2 transaction tests are local evidence, not production readiness.
