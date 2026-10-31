# 订单操作日志与会员操作

2026-10-28。对照芋道TradeOrderLogDO及TradeOrderOperateTypeEnum迁移。新库/存量库在部署新服务前执行 `lemnt-server/lemnt-trade/sql/V8__order_log.sql`；不回填虚构历史。脚本已生成，未在真实数据库执行。以下示例均为构造，未部署HTTP实测。

## 1. 后台订单日志

接口地址：`/trade/admin/orders/{orderNo}/logs`；请求方式：GET；接口权限码：`trade:fulfillment:query`，与当前订单详情相同。

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 后台令牌 |
| orderNo | string | 是 | Path | 无 | 订单业务编号，最长40 |

```bash
curl 'https://<API_HOST>/trade/admin/orders/TR1/logs' -H 'Authorization: Bearer <TOKEN>'
```

```json
{"code":200,"data":[{"id":1,"userId":1,"userType":1,"orderId":7,"afterStatus":0,"operateType":1,"content":"用户下单","createTime":"2026-10-28 12:00:00"},{"id":2,"userId":1,"userType":1,"orderId":7,"beforeStatus":0,"afterStatus":10,"operateType":10,"content":"用户付款成功","createTime":"2026-10-28 12:01:00"}]}
```

| 响应参数 | 类型 | 说明 |
|---|---|---|
| code | integer | 成功200 |
| data | array | 按日志编号升序，旧订单无日志时为空数组 |
| data[].id | long/string | 日志主键，长ID客户端保持字符串 |
| data[].userId | long/string | 操作者；系统固定0，会员和后台为实际用户ID |
| data[].userType | integer | 系统0、会员1、后台2 |
| data[].orderId | long/string | 订单数值主键，区别于orderNo |
| data[].beforeStatus | integer/缺失 | 操作前状态，新建无初态 |
| data[].afterStatus | integer | 0待支付、10待发货、20待收货、30完成、40取消 |
| data[].operateType | integer | 1下单、10支付、20发货、30会员收货、31系统收货、40会员取消、41系统取消、43全额售后取消 |
| data[].content | string | 实际操作说明；发货包含承运商和运单号 |
| data[].createTime | datetime | 数据库同一时区，yyyy-MM-dd HH:mm:ss |

当前内部RESERVING、PENDING_PAYMENT都映射源项目状态0；预占成功不另写一条0到0日志。日志与订单、支付或物流状态共用本地事务，插入失败使该状态更新回滚；重复回调、重复收货、重复关单不追加第二条操作。发货操作者来自后台认证上下文，不接受请求体传userId/userType。接口只有后台读取入口，不向App暴露内部操作日志。

## 2. 本人取消与确认收货

| 接口地址 | 请求方式 | 接口权限 | 说明 |
|---|---|---|---|
| `/trade/app/orders/{orderNo}/cancel` | POST | 已认证会员本人 | 仅待支付/内部预占中可取消，支付成功后走售后 |
| `/trade/app/orders/{orderNo}/receive` | POST | 已认证会员本人 | 已发货且无待处理售后可收货 |

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
|---|---|---|---|---|---|
| Authorization | string | 是 | Header | 无 | 会员令牌 |
| orderNo | string | 是 | Path | 无 | 订单业务编号 |

请求无Body，不允许传memberId、金额、操作者类型或状态。服务端在订单行锁内核对归属，即使订单已取消/已完成，其他会员也不能通过重复入口绕过归属检查。

```bash
curl -X POST 'https://<API_HOST>/trade/app/orders/TR1/cancel' -H 'Authorization: Bearer <MEMBER_TOKEN>'
```

```json
{"code":200,"data":true}
```

响应code成功200，data=true表示服务端确认目标操作。取消本地事务提交后通过既有MQ统一补偿库存与券；不新增outbox。已存在待支付单时仍须关闭真实支付单才能关单，未接渠道时不能伪造关闭成功。

业务拒绝示例（统一异常；不暴露他人的订单状态）：

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"订单不存在","code":"BUSINESS_RULE_VIOLATION"}
```

状态不允许取消时返回统一业务失败；继承的履约状态异常仍按当前公共处理器映射，本批未另建异常Handler。缺少认证、权限按网关/统一权限处理。参数绑定或校验错误按统一422。响应丢失时先查询订单结果，再决定是否重试；已确认操作后的刷新失败应保留确认结果。

## 3. 范围收敛与剩余

参考源项目并无启用的后台手工关单操作，移除旧 `POST /trade/admin/orders/{orderNo}/close`；系统超时关闭和会员本人取消保留。菜单退役脚本移除旧按钮及对应角色按钮关联，不自动授予新权限。

后台订单详情展示日志；当前小程序订单回执可取消或确认收货。真实渠道SDK、MySQL脚本执行、浏览器和真机联调尚未验证。本批不是售后日志迁移完成：售后数值主键、完整字段与after_sale_id关联日志仍按F02继续迁移；调价、改址、评价产生的操作类型也随对应功能继续补齐。
