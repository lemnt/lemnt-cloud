# 支付渠道接入与已支付链路交付

2026-11-01 第147批。补齐订单支付发起、渠道回调验签和沙箱渠道适配器，打通“下单 → 支付 → 交易 PAID → 商城活动订单 PAID / 拼团成团 / 会员权益变更”的已支付链路。真实支付宝/微信商户适配器仍未接入。

## 交易

- 新增 `POST /trade/app/orders/{orderNo}/payment`：会员本人、待支付、金额大于 0 才允许发起；金额来自订单，客户端只提交 `appKey` 与 `channel`。
- 新增 `POST /trade/payment/notify/{appKey}/{channel}`：公开回调入口，按应用解析渠道配置并验签，再提交支付结果。
- 新增 `PaymentChannelGateway` 与基础设施 `PaymentChannelAdapter` SPI：缺失或重复适配器明确失败，不模拟支付成功。
- 新增沙箱渠道 `SANDBOX`：本地 HMAC-SHA256 签名通知，5 分钟时间窗、`notifyId` 唯一、金额与签名严格校验；通过 `payment.sandbox.enabled=true` 才注册适配器，默认关闭。
- 沙箱关单适配器将待支付支付单视为本地撤销，订单取消可正常关闭支付单。
- `trade_payment` 新增 `app_key`，支付单绑定发起支付的应用；回调必须与支付单渠道、应用一致。
- 新增 `payment_app_key.sql` 升级脚本；`V2__trade_payment.sql` 已同步新库结构。

## 后台与小程序

- 支付渠道下拉增加“沙箱（仅联调）”；支付单列表可按 `SANDBOX` 筛选。
- 小程序收银台与真实渠道 SDK 未在本批接入（小程序收银台于第149批补齐，见[149-miniapp-cashier-delivery.md](149-miniapp-cashier-delivery.md)）。

## 验签格式

回调头：`X-Pay-Timestamp`（毫秒）、`X-Pay-Signature`（HMAC-SHA256 十六进制）。

签名原文按换行拼接：

```text
SANDBOX
{paymentNo}
{orderNo}
{amount，两位小数}
{channelTradeNo}
{notifyId}
{timestamp}
```

请求体：`{"paymentNo","orderNo","channelTradeNo","amount","notifyId"}`。渠道密钥取自支付渠道配置 JSON 的 `hmacSecret`，不会出现在预支付响应中。

## 验证

- trade 全模块 **240 项**测试通过，0 失败/错误/跳过。
  - `SandboxPaymentChannelAdapterTest` 4：预支付参数不泄漏密钥、有效签名通过、篡改/过期/缺密钥拒绝、关单路由。
  - `PaymentPrepayCmdExeTest` 3：本人待支付订单、第三方/0 元/非待支付拒绝、应用与渠道停用失败。
  - `PaymentNotifyReceiveCmdTest` 2：验签通过后提交、应用不匹配拒绝。
  - `AppPaymentControllerTest` 新增预支付 HTTP 契约；`ControllerMappingTest` 纳入两个新路由。
- React 292 项通过；小程序无改动。

真实环境验收：

- 砍价订单 `TR8F5743FE0EE246BF95D07485`：预支付 `PAY1D1667178F2041E1BFD70474`，签名通知后交易与商城活动订单均 `PAID`，通知任务一次成功，会员 1 经验 0→800。
- 拼团 `2116576587669000193`：团长订单 `TRABDC9A309C5946E3A83FACFD`、团员订单 `TR502B89490186464C86D166D6` 先后支付，拼团记录 `2116580217209966594` 状态成功、人数 2/2，活动 `successCount=1`。
- 秒杀待支付订单重复发起返回同一支付单，第三方发起返回“订单不存在”；取消后支付单、交易订单、商城活动订单均为 `CLOSED`，秒杀库存恢复。
- 错误签名返回 400；同一通知重放幂等；已支付订单取消被拒绝。

证据：`evidence/payment-channel-verification.json`。

## 未覆盖

- 支付宝、微信真实商户适配器与证书/密钥管理；当前只有沙箱适配器。
- 沙箱订单的渠道退款；售后退款仍需要真实或沙箱退款适配器（沙箱退款于第148批补齐，见[148-sandbox-refund-delivery.md](148-sandbox-refund-delivery.md)）。
- 小程序收银台交互与真机支付。
- 渠道对账、退款回调与生产密钥加密。
- 本批未 commit/push。
