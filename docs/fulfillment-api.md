# 履约：自动收货与快递选择

2026-11-01。

## 自动收货

交易服务按配置扫描物流单：

| 配置 | 默认 | 说明 |
| --- | --- | --- |
| lemnt.trade.auto-receive.enabled | false | 是否启用 |
| lemnt.trade.auto-receive.expire | 7d | 发货后超过该时长仍未收货 |
| lemnt.trade.auto-receive.delay-ms | 60000 | 调度间隔 |

处理条件：`trade_shipment.received_at IS NULL` 且 `shipped_at <= 数据库当前时间 - expire`。对每个订单调用既有收货逻辑，校验订单 `SHIPPED`、无处理中售后，写入收货时间和 `COMPLETED`，并记录系统自动确认收货日志。单条异常只记录日志。

## 退货快递

| 方法与路径 | 身份 | 功能 |
| --- | --- | --- |
| GET /trade/app/delivery/expresses | 会员 | 返回启用快递公司 id/code/name |

小程序售后详情使用该列表选择快递公司，提交退货物流时传 `carrierCode` 与 `trackingNo`。后台快递公司、运费模板和交易配置页面沿用既有交付。
