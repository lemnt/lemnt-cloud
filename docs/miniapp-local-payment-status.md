# Miniapp local payment status

`GET /trade/app/orders/{orderNo}/payment` requires the independent member session. It checks order ownership before reading payments; missing/foreign orders share the same `订单不存在` application failure. Missing member session is HTTP401. Request member IDs are ignored. This does not implement provider prepay, signature verification or a channel query.

Response data:

| Field | Meaning |
| --- | --- |
| orderNo | owned order number |
| orderStatus | local RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED |
| payableAmount | exact decimal yuan string |
| localPaymentStatus | state below |
| channelVerified | fixed false; channel was not queried |

| Local state | Interpretation |
| --- | --- |
| NOT_CREATED | no local payment record for an unpaid/reserving order |
| WAITING | local waiting record; provider may already have paid with a delayed callback |
| SUCCESS | exactly one local SUCCESS record matches amount and a paid/shipped/completed order |
| CLOSED | local payment records closed, or local order closed with no conflicting waiting record |
| RECONCILE_REQUIRED | amount/state mismatch, unknown state, multiple successes or paid order without a matching success |

CLOSED is a payment-record report, not an instruction to close the order. WAITING/NOT_CREATED are **not** authenticated provider-unpaid results. This endpoint performs no writes, no inventory release and no safe-close decision. Existing atomic-close provider requirements remain unchanged. Payment transaction identifiers/merchant credentials are not returned.

Query runs through application Qry -> executor -> domain gateways -> infrastructure; DTO -> Res converter preserves boundaries. Miniapp explicitly labels local-only data, rejects unknown/malformed/cross-order responses, preserves decimal strings and clears account-specific results on session switch. It does not call wx.requestPayment or display fabricated channel success.

No schema/dependency change. Actual identity verifier and real provider adapters remain absent; readiness stays false. Tests are local mocks/H2 and native-page behavior tests, not live gateway/payment/WeChat compiler/device/visual validation.

2026-10-27 补充：订单退款成功后 orderStatus 可以是 REFUNDED；原支付单仍为 SUCCESS，逆向退款单独记录于支付退单。本地支付状态不等同于净收款状态，仍不声称已核验真实渠道。
