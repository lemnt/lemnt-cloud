# 履约细节第一批：自动收货与退货快递选择

2026-11-01 第154批。补齐超期自动确认收货和小程序退货快递公司选择；自提门店核销、订单改址/改价仍留待后续。

## 自动收货

- 新增 `TradeOrderAutoReceiveScheduler`，配置前缀 `lemnt.trade.auto-receive`：`enabled`、`expire`（默认 7d）、`delay-ms`（默认 60000）。
- 查询未收货且发货时间早于截止时间的物流单，逐单调用既有 `OrderFulfillmentService.receive(orderNo, system)`；复用订单行锁、退款中校验、物流收货时间、订单状态和操作日志。
- 单条失败只记录日志，不影响其余订单；使用数据库时间判断超期。
- `ShipmentGateway` 增加 `shippedBefore(cutoff, limit)` 查询。

## 退货快递选择

- 新增 `GET /trade/app/delivery/expresses`：返回启用状态快递公司的编号、编码和名称。
- 小程序售后详情登记退货物流时改用快递公司下拉选择，不再手工输入编码；提交仍使用服务端返回的编码。
- 后台快递公司、运费模板、交易配置页面沿用既有 129/130 批交付，本批未重复实现。

## 验证

- trade 全模块 **257 项**测试通过，0 失败/错误/跳过；新增 `AutoReceiveSchedulerTest` 2，`CheckoutFlowIntegrationTest` 100 项回归通过。
- 小程序 **173 项**、React **308 项**、网关 **35 项**测试通过。
- 真实环境：
  - 顺丰快递公司创建后，会员接口返回 `SF/顺丰速运`。
  - 订单 `TR1DBB531265B446E797201167` 发货后将 `shipped_at` 调整为 8 天前，重启交易服务并开启自动收货；调度器将订单从 `SHIPPED` 置为 `COMPLETED`，写入收货时间，操作日志记录系统自动确认收货。

证据：`evidence/fulfillment-auto-receive-verification.json`。

## 未覆盖

- 自提门店、店员与核销流程（T13）。
- 订单改价、改址及完整订单经营字段（T01 剩余）。
- 真实物流轨迹渠道、自动收货期限的后台配置页面。
- 浏览器后台与小程序真机联调。
- 本批未 commit/push。
