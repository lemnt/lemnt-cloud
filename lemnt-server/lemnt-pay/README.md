# 支付服务

支付、原路退款、钱包与充值独立存储；order拥有订单/售后状态。唯一初始化SQL为sql/lemnt_pay.sql，只用于空库。端口默认9170，通过PAY_PORT覆盖。配置由环境提供，不存储凭据。

使用 `mvn -f lemnt-server/pom.xml -pl lemnt-pay/lemnt-pay-start -am test` 构建和测试。

支付成功和现金退款成功在资金事务中保存通知任务。提交后异步调用订单服务，订单事务完成后任务标记成功。失败时按原退避规则重试，到期扫描由 `PAY_NOTIFY_ENABLED` 控制，默认开启。支付配置位于 `docs/config/lemnt-pay.yaml`。

支付应用可分别设置支付和退款结果的 HTTP 地址。任务按原单地址投递，接收端须配置在 Nacos 授权范围内。 [应用通知配置与接入](../../docs/payment-management-api.md#支付应用结果通知)

微信渠道使用公钥模式，现有渠道 JSON 需配置 `publicKey` 和 `publicKeyId`。待支付与待退款单由 `PAYMENT_RECOVERY_ENABLED` 控制补查，默认开启。微信退款请求携带已配置的 `refundNotifyUrl`，回调与补查共用资金状态更新和通知任务。见[微信支付配置](../../docs/payment-management-api.md#微信支付apiv3)。

商城应用启用 `WALLET` 后可使用余额支付及原路退款。订单、支付服务需连接同一 Redis 库以协调关单与扣款。见[钱包余额支付](../../docs/payment-management-api.md#钱包余额支付)。

存量切流使用[通知任务迁移脚本](../sql/upgrade/migrate_pay_outbox_to_notify_task.sql)。先上线兼容旧消息和新通知的订单服务，再暂停旧支付写入方、中继及通知调度，核对旧记录后切换支付服务。旧 `AFTER_SALE` 派发任务单独审核迁往订单服务。
