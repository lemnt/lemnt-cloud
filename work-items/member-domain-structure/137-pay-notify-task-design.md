# 支付通知任务、日志与重试设计

2026-10-31 第137批。对应完整迁移 F05 的“通知任务及日志”与台账 Y04。参考芋道 `PayNotifyTaskDO`/`PayNotifyLogDO` 的任务+日志模型，适配到当前单体：这里没有独立 pay 服务，因此“通知任务”指**支付结果落库后向下游（库存确认、核券、退款权益回退）的可靠派发任务**，而不是对另一套商户系统的 HTTP 回调。

## 背景与约束

- 现状：`PaymentNotifyCmdExe` 先提交支付成功与订单 PAID，再同步 `OrderRemoteService.paid`；失败向上抛出，由渠道重试。退款成功后在 `PaymentRefundCreateCmdExe` 同步 `RefundBenefitsService.complete`。
- 必须保留的契约：下游派发失败时，调用方仍收到错误（现有集成测试断言）；支付/退款状态不回滚。真实渠道 SDK 不纳入。
- 本批新增持久化任务后，即使渠道不再重试，调度器仍可继续派发，二者共同保证最终一致。

## 模型

`trade_pay_notify_task`：
- type（PAY/REFUND）、biz_no（paymentNo/refundNo）、order_no；
- status（PENDING/SUCCESS/FAILED）、notify_times、max_notify_times（默认8）、next_notify_time、last_execute_time、last_response；
- 审计列；唯一 (type, biz_no)；索引 (status, next_notify_time)。

`trade_pay_notify_log`：task_id、notify_times、status（SUCCESS/FAILED）、response、审计列。

## 流程

1. 支付成功事务提交后：`enqueue(PAY, paymentNo, orderNo)`（唯一键幂等），立即 `attempt`。
2. 退款成功事务提交后：`enqueue(REFUND, refundNo, orderNo)`，立即 `attempt`。
3. `attempt`：CAS 抢占（`notify_times = expected+1`，且未成功/未超限）→ 派发 → 成功置 SUCCESS；失败置 FAILED、按下表设置下次时间、写日志，返回 false。
4. 调用方在 attempt 返回 false 时抛出异常，保持既有契约；任务已持久化，调度器继续重试。
5. 调度器按数据库时间扫描 `status IN (PENDING, FAILED) AND notify_times < max AND next_notify_time <= now`，逐条 attempt，失败隔离。
6. 后台可查询任务/日志，并对失败任务“重发”（重置次数、立即再试）。

重试间隔（秒）：15、15、30、180、1800、1800、1800、3600；超过 8 次保持 FAILED，不再自动调度，等待人工重发。

## 派发

- PAY → `OrderRemoteService.paid(orderNo)`（幂等：确认库存、核券、履约完成）。
- REFUND → 重新加载 `PaymentRefund` 且必须 SUCCESS，调用 `RefundBenefitsService.complete(refund)`（幂等）。

## 接口

- `GET /trade/admin/pay-notify-tasks`（type/bizNo/orderNo/status/pageNum/pageSize）权限 `trade:pay-notify:list`
- `GET /trade/admin/pay-notify-tasks/{id}`（任务+日志）权限 `trade:pay-notify:query`
- `POST /trade/admin/pay-notify-tasks/{id}/retry` 权限 `trade:pay-notify:retry`

## 并发与幂等

- enqueue 依赖唯一键，重复入队返回既有任务。
- attempt 以 CAS 次数抢占，避免立即尝试与调度器重复派发。
- 派发目标本身幂等；成功任务不会被再次派发。

## 验证

- 应用层：入队幂等、成功/失败状态、失败按间隔重排、超限停止、人工重发、调度过滤。
- 持久化：生产 SQL/DO/Mapper/Gateway + H2，唯一键与 CAS。
- 集成：既有支付/退款集成测试保持通过；新增失败任务可被调度重试并成功。
- 后台：任务列表/详情/日志、重发权限与严格解码。
- 不执行真实 MySQL/Nacos/Redis/浏览器联调。
