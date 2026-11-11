# Trade outbox compensation API

Version v1 · 2026-10-25. Static contract checked against source; examples constructed, not live HTTP evidence.

## 1. Replay exhausted event

Address: `/trade/outbox/{eventId}/replay` (service path; external gateway routing must be verified).
Method: `POST`. Permission: `trade:outbox:replay` through the existing authenticated admin security mechanism.

| Parameter | Type | Required | Location | Default | Meaning |
| --- | --- | --- | --- | --- | --- |
| eventId | string | yes | path | none | Existing event identifier, nonblank, maximum 64 characters |

| Response field | Type | Meaning |
| --- | --- | --- |
| code | integer | Application result, 200 for accepted request |
| msg | string/null | Existing R message; null for R.ok |
| data | boolean | true only if DEAD was atomically reset to NEW; false for unknown/non-DEAD event |
| rows | array/null | Unused; default null (may be omitted by deployment serializer configuration) |
| total | integer/null | Unused; default null (may be omitted by deployment serializer configuration) |

Constructed success with default R fields:

```json
{"code":200,"msg":null,"data":true,"rows":null,"total":null}
```

Constructed unchanged/non-DEAD result:

```json
{"code":200,"msg":null,"data":false,"rows":null,"total":null}
```

```bash
curl -X POST 'https://<API_HOST>/trade/outbox/<EVENT_ID>/replay' \
  -H 'Authorization: Bearer <ADMIN_ACCESS_TOKEN>'
```

Authentication/permission failures use the existing security handler; exact deployed gateway responses have not been exercised. Invalid identifiers raise IllegalArgumentException; this new endpoint does not introduce a separate error envelope. No public payment callback or payment-channel signature protocol is provided by this API.

## Recovery procedure

Stop legacy workers, apply `V3__outbox_delivery_lease.sql`, deploy new workers, then enable the existing scheduler only after reviewing payment handling. Inspect DEAD records with read-only SQL:

```sql
SELECT event_id, aggregate_no, event_type, retry_count, status
FROM trade_outbox WHERE status='DEAD' ORDER BY id LIMIT 100;
```

Fix the failing RPC/data invariant, inspect order and reservation state, then invoke replay using a narrowly granted admin permission. The next scheduler pass claims a new lease; replay is not immediate delivery. Inventory RPCs remain at-least-once. Partial item completion is retried using the same order/product reservation keys. Do not change DELIVERED rows to NEW manually.

Retries: 10 total claims per cycle including crashed workers, five-minute leases, failed attempts wait 30s doubling to a one-hour cap. Failed tenth attempts become DEAD immediately; crashed tenth attempts become DEAD after lease expiry on the next scan. Repeated manual replay can produce further attempts and requires operator judgment. Monitor DEAD counts and RPC errors; alerting infrastructure is not included.
