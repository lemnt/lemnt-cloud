# 小程序收银台与售后支付交互交付

2026-11-01 第149批。补齐小程序收银台、订单去支付入口和售后退款进度刷新，后端增加小程序渠道查询与沙箱支付确认接口。

## 后端

- 新增 `GET /trade/app/payment-channels?appKey=`：只返回支付应用下已启用、编码可识别的渠道。
- 新增 `POST /trade/app/payments/{paymentNo}/sandbox-pay`：会员本人确认沙箱支付单；校验支付单渠道、订单归属、支付单与订单状态，成功后走既有支付通知与下游派发链路；成功重试幂等。
- 沙箱支付确认执行器仅在 `payment.sandbox.enabled=true` 时注册；关闭时接口返回“沙箱支付未启用”。

## 小程序

- 新增 `services/payment.js`：严格解码渠道列表与预支付响应，未知渠道、金额格式、订单号不一致均拒绝。
- 交易客户端新增 `paymentChannels`、改造 `prepay`、新增 `sandboxPay`，不再调用不存在的旧 `/prepay` 路径。
- 新增收银台 `pages/cashier/index`：选择渠道、创建支付单、沙箱模拟支付、刷新支付状态、跳转订单详情。
- 订单详情、订单列表、结算页、秒杀/拼团/砍价/积分商城详情增加“去支付”入口，仅在 `PENDING_PAYMENT` 显示。
- 售后详情增加原支付状态、退款中/已退款提示和“刷新退款进度”按钮。

## 验证

- trade 全模块 **250 项**测试通过，0 失败/错误/跳过。
  - `MemberPaymentChannelQueryExeTest` 2：启用渠道去重与未知编码忽略、停用应用拒绝。
  - `SandboxPaymentCmdExeTest` 3：本人待支付提交、成功重试幂等、他人/关闭/非沙箱拒绝。
  - `AppPaymentChannelControllerTest` 3：渠道查询、沙箱开关关闭、会话会员支付。
- 小程序 **160 项**测试通过，新增 `payment.test.cjs` 2 项、`cashier.test.cjs` 2 项，售后详情刷新 1 项。
- 真实环境：渠道接口返回 `SANDBOX`；会员 3 创建秒杀订单 `TR4858F36B71AC4CF7A588B275`，预支付 `PAY3B925DA6C9554738A5BD056D` 后调用沙箱支付，交易与商城活动订单均 `PAID`，秒杀库存 10→9，支付通知任务一次成功，会员 3 经验 0→400；重复确认幂等，其他会员确认返回“支付单不存在”。

证据：`evidence/miniapp-cashier-verification.json`。

## 未覆盖

- 浏览器与小程序真机联调；本批只验证服务端接口与页面逻辑测试。
- 支付宝/微信真实支付 SDK 与支付链接跳转。
- 售后退款仍由后台发起，小程序只展示进度。
- 本批未 commit/push。
