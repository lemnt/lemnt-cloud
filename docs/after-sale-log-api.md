# 售后标准表、操作日志及本人取消

2026-10-28。对照本地芋道AfterSaleDO/AfterSaleLogDO、售后状态及操作类型迁移。真实渠道SDK仍后置；以下curl/JSON为构造示例，未做部署HTTP、浏览器或真机端到端验收。

## 1. 售后身份与新增返回字段

当前表为 `trade_after_sale`，数值主键id与唯一业务单号refund_no分别保存，原 `trade_payment_refund.after_sale_no` 仍关联唯一业务单号。接口地址沿用 `/trade/admin/after-sales`，不把支付退单与售后申请混成一个模型。

现有分页、详情、申请、同意、驳回、登记退货、确认收货、拒收的独立Res新增以下字段。权限仍分别使用 `trade:after-sale:list/query/create/agree/reject/return/receive/refuse`。

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code | integer | 成功200 |
| data/rows | object/array | 单条data，分页顶层rows/total |
| id | long/string | 真实售后数值主键，区别于refundNo |
| orderId | long/string/缺失 | 订单数值主键；历史通过orderNo唯一关联回填 |
| auditUserId | long/string/缺失 | 实际后台审批人员；历史无依据时缺失 |
| refundTime | datetime/缺失 | 数据库记录的退款成功确认时间，FAILED/WAITING不得伪造 |

保留字段：refundNo/orderNo/requestNo/memberId/paymentNo/orderItemId/quantity/type/amount/reason/status/decisionReason/returnCarrierCode/returnTrackingNo/requestedAt/decidedAt/returnShippedAt/returnReceivedAt。金额amount仍是精确元金额字符串，不暗改为分；日期为数据库时区`yyyy-MM-dd HH:mm:ss`。long客户端按字符串保存。

详情GET `/trade/admin/after-sales/{no}`，权限`trade:after-sale:query`。Authorization为必填Header，no为必填Path、业务单号最长40。请求无Body：

```bash
curl 'https://<API_HOST>/trade/admin/after-sales/RF1' -H 'Authorization: Bearer <TOKEN>'
```

```json
{"code":200,"data":{"refundNo":"RF1","orderNo":"TR1","requestNo":"AFTER1","memberId":1,"paymentNo":"PAY1","orderItemId":4,"quantity":1,"type":"RETURN_REFUND","amount":"8.50","reason":"商品问题","status":"REFUNDED","returnCarrierCode":"TEST","returnTrackingNo":"BACK1","requestedAt":"2026-10-28 12:00:00","decidedAt":"2026-10-28 12:10:00","returnShippedAt":"2026-10-28 13:00:00","returnReceivedAt":"2026-10-28 14:00:00","id":7,"orderId":2,"refundTime":"2026-10-28 14:10:00","auditUserId":55}}
```

后台审批和退款接口的操作者只取已认证上下文，不接收Body/Query的auditUserId、userType；系统回调记系统，会员取消记本人。独立Req/Res保持原接口隔离，不直接暴露DO。

## 2. 后台售后日志

接口地址：`/trade/admin/after-sales/{no}/logs`；请求方式：GET；接口权限码：`trade:after-sale:query`。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 后台令牌 |
| no | string | 是 | Path | 无 | 售后业务单号，最长40 |

```bash
curl 'https://<API_HOST>/trade/admin/after-sales/RF1/logs' -H 'Authorization: Bearer <TOKEN>'
```

```json
{"code":200,"data":[{"id":1,"userId":1,"userType":1,"afterSaleId":7,"afterStatus":10,"operateType":10,"content":"申请商品售后","createTime":"2026-10-28 12:00:00"},{"id":2,"userId":55,"userType":2,"afterSaleId":7,"beforeStatus":10,"afterStatus":20,"operateType":11,"content":"商家同意退款","createTime":"2026-10-28 12:10:00"}]}
```

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code | integer | 成功200 |
| data | array | 按日志编号升序；没有历史日志时为空数组 |
| data[].id | long/string | 日志主键 |
| data[].afterSaleId | long/string | 关联trade_after_sale.id，不是refundNo |
| data[].userId | long/string | 系统0，会员/后台为实际用户编号 |
| data[].userType | integer | 系统0、会员1、后台2 |
| data[].beforeStatus | integer/缺失 | 创建申请没有前态 |
| data[].afterStatus | integer | 下表源项目状态编号 |
| data[].operateType | integer | 下表操作类型 |
| data[].content | string | 真实操作说明，退货包含运单，拒绝/拒收包含原因 |
| data[].createTime | datetime | 数据库时区发生时间 |

| 当前售后状态 | 日志源状态 | 含义 |
|---|---|---|
| REQUESTED | 10 | 申请中 |
| AWAITING_RETURN | 20 | 同意退货 |
| RETURNED | 30 | 买家已寄回 |
| WAIT_REFUND/REFUNDING | 40 | 等待退款；内部渠道处理中也映射40 |
| REFUNDED | 50 | 已退款 |
| CANCELLED | 61 | 会员取消 |
| REJECTED | 62 | 拒绝申请 |
| RETURN_REJECTED | 63 | 拒收退货 |

操作类型10申请、11同意、12拒绝、20寄回、21收到退货、22拒收、30发起退款、31退款成功、32退款失败、40会员取消。内部状态字符串及元单位属于已保留的项目适配，不能宣称表字段类型与源项目完全相同。

日志和售后状态同事务；发起退款与退款交易创建同事务，渠道调用在事务外。日志落库失败时不允许只提交审批/退款状态。重复同意、收货、退款成功/失败回调和取消不会重复记日志。失败退款不完成售后，不写refundTime，也不关闭订单。

## 3. 本人取消售后

接口地址：`/trade/app/after-sales/{no}/cancel`；请求方式：POST；权限：网关已认证的会员本人。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 会员令牌 |
| no | string | 是 | Path | 无 | 本人售后业务单号 |

请求无Body，不接受memberId、状态或操作者。只允许REQUESTED、AWAITING_RETURN、RETURNED取消，重复取消返回原确认结果；WAIT_REFUND、REFUNDING、REFUNDED不允许取消。即使已取消也先校验归属，不能重放别人的售后。

```bash
curl -X POST 'https://<API_HOST>/trade/app/after-sales/RF1/cancel' -H 'Authorization: Bearer <MEMBER_TOKEN>'
```

```json
{"code":200,"data":true}
```

code成功200，data为确认布尔值。取消后该售后不再占用可售后数量/金额；不退款、不释放已付款订单库存、不改写原支付。新申请仍需使用新requestNo。

统一业务拒绝构造示例：

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"当前售后状态不能取消","code":"BUSINESS_RULE_VIOLATION"}
```

归属不符/不存在统一业务拒绝，不披露他人售后状态。参数校验失败按公共处理器422；原有审批/履约状态异常仍沿用现有公共处理器，本批不新增异常Handler。认证与后台权限由统一机制处理。

## 4. 后台操作和迁移顺序

后台售后页面补详情、同意、驳回、确认退货收货、拒收、发起/核验退款和日志。权限分别控制按钮；退款WAITING提示受理、SUCCESS才提示成功、FAILED显示失败，不把操作被接收当作已退款。

新库执行V5/V6/V9初始化。已拥有上一版trade_refund_request与trade_payment_refund的旧库，停写备份后审核执行`sql/upgrade/after_sale_identity_and_log.sql`，再执行V9创建日志。不能混用新初始化与旧升级顺序。原业务单号保留唯一并重建支付退款外键；orderId精确回填，createTime取已存在的申请时刻；refundTime仅从唯一成功支付退款记录的update_time取本地确认事实，缺事实保持空；auditUserId及历史updateTime不猜测。日志不回填虚构历史。新代码和旧表不能混跑，回退需要停写恢复备份。

未执行真实库升级，未接真实渠道或完成浏览器/真机验收。完整源售后字段仍有缺口：申请描述与凭证、原订单完整SPU/规格/图片快照、pay_refund_id及logistics_id数值关联等继续迁移。本批不查询当前商品冒充历史快照，也不把数值主键/日志完成说成源项目全部字段已复刻。
