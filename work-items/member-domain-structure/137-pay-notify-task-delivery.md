# 支付通知任务、日志与重试交付与验证

2026-10-31 第137批。按设计交付支付/退款结果下游派发任务、尝试日志、CAS 抢占与定时重试，并接入现有支付通知与退款完成链路。没有引入真实渠道 SDK，没有新增对外路由变更以外的东西。

## 后端

新增 `trade_pay_notify_task`、`trade_pay_notify_log` 两表（`V11__pay_notify_task.sql`），唯一键 `(type,biz_no)`，到期索引 `(status,next_notify_time)`。

- 领域：`PayNotifyType`、`PayNotifyTaskStatus`、`PayNotifyTask`、`PayNotifyLog`、`PayNotifyTaskFilter`、`PayNotifyTaskGateway`。
- 基础设施：DO/Mapper/Converter/GatewayImpl；`claim`、`finishSuccess`、`finishFailed`、`resetForRetry`、`findDue` 均使用条件更新/数据库时间。
- 应用：`PayNotifyTaskService`（幂等入队、CAS 尝试、失败退避、批量到期处理、人工重发）、`PayNotifyDispatcher`（PAY→`OrderRemoteService.paid`，REFUND→重新加载成功退款后 `RefundBenefitsService.complete`）。
- 接入：
  - `PaymentNotifyCmdExe`：支付状态提交后入队并立即派发；失败仍向上抛出（保持“调用方收到错误、渠道可重试”契约），同时保留失败任务。
  - `PaymentRefundCreateCmdExe`：仅退款成功时入队并立即派发权益回退；非成功退款保持原有 no-op。
- 调度：`PayNotifyScheduler` 在 `lemnt.trade.pay-notify.enabled=true` 时按 `lemnt.trade.pay-notify.delay-ms` 扫描到期任务，逐条隔离失败。
- 重试间隔：15/15/30/180/1800/1800/1800/3600 秒；最多 8 次；超限保持 FAILED，等待人工重发。
- 后台接口（`/trade/admin/pay-notify-tasks`）：分页、详情+日志、重发；权限 `trade:pay-notify:list/query/retry`；菜单种子 `V20261031__trade_pay_notify_menu.sql`，可重复执行、不分配角色。

## 后台 UI

- `payNotifyApi.ts`：严格解码大编号、类型、状态、尝试次数、日志。
- `PayNotifyTaskPage.tsx`：任务列表、筛选、详情与尝试日志抽屉、失败任务重发；按权限控制。
- 路由 `trade/pay-notify/index`，菜单 component 与之一致。

## 验证

- 后端 trade 全模块 **196 项通过**、0 失败/错误/跳过：domain 2、application 36、infrastructure 10、adapter 25、start 123。
  - 新增 `PayNotifyTaskServiceTest` 7：入队幂等/类型校验、成功、失败退避、已成功/超限不再派发、CAS 丢失、批量到期、人工重发。
  - 新增 `PayNotifyTaskPersistenceTest` 3：生产 SQL/DO/Mapper/Gateway + H2，唯一键、CAS 单赢家、到期扫描、失败日志与重置。
  - 新增 `PayNotifyMenuSeedTest` 1；`ControllerMappingTest` 收录新路由。
  - `CheckoutFlowIntegrationTest` 100 项全通过：支付成功/下游失败抛错、失败后重试、退款成功/失败/等待、退款权益派发均保持既有契约。
- 前端 **274 项通过**（新增 `payNotifyApi.test.ts` 3），`typecheck`、`lint`、`format:check` 通过。
- 证据：`evidence/pay-notify-task-verification.json`。

## 说明与限制

- 调度器默认关闭，需在部署配置中显式开启 `lemnt.trade.pay-notify.enabled=true`；本批未修改 Nacos/部署。
- 未执行真实 MySQL/Nacos/Redis/Dubbo/浏览器联调；H2 与单测不代表真实环境验收。
- 真实支付渠道回调 SDK 仍按约定后置；任务派发为本地下游，不发送外网 HTTP。
- 本批未主动 commit/push。
