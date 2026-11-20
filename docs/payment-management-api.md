# 后台支付渠道、支付单和支付退单

> 第166批更新：member/mall/trade的旧V*及upgrade脚本已整合移除，本文原执行步骤只作历史说明。新库统一使用[最终初始化文件](database-initialization.md)；已有库另行核对结构差异，system SQL不变。

2026-10-27。范围按用户定义：支付配置展示支持渠道；支付退单是已有订单售后单触发的逆向支付交易。参考芋道支付退款和售后分工，按本项目 Adapter → Application → Domain / Infrastructure 结构实现。支付与退款创建仍由订单、售后流程发起，后台提供只读管理页面。

## 接口

| 方法与路径 | 权限 | 功能 |
| --- | --- | --- |
| GET /trade/admin/payment-channels | trade:payment-channel:list | 展示支持的渠道编码和名称 |
| GET /trade/admin/payments | trade:payment:list | 支付单分页 |
| GET /trade/admin/payments/{no} | trade:payment:query | 支付单详情 |
| GET /trade/admin/payment-refunds | trade:payment-refund:list | 支付退单分页 |
| GET /trade/admin/payment-refunds/{no} | trade:payment-refund:query | 支付退单详情 |
| POST /trade/admin/refund-requests/{afterSaleNo}/payment-refund | trade:payment-refund:create | 基于售后单发起退款或查询同步原退款结果 |
| POST /trade/app/orders/{orderNo}/payment | 会员登录 | 发起本人待支付订单支付，金额由订单确定 |
| POST /trade/payment/notify/{appKey}/{channel} | 渠道验签 | 接收支付渠道异步通知并提交支付结果 |
| POST /trade/payment/notify/{appKey}/{channel}/refund | 渠道验签 | 接收微信退款通知，资金提交后返回 HTTP 204 |
| GET /trade/app/payment-channels?appKey= | 会员登录 | 查询支付应用下已启用的渠道 |
| POST /trade/app/payments/{paymentNo}/sandbox-pay | 会员登录 | 确认本人沙箱支付单，仅沙箱开关开启时可用 |

支付配置与支付创建复用 PaymentChannel：ALIPAY（支付宝）、WECHAT（微信支付）、SANDBOX（沙箱，仅联调）。SANDBOX 适配器仅在 `payment.sandbox.enabled=true` 时注册，生产默认关闭；缺失或重复渠道适配器明确失败，不模拟支付成功。仅表明系统渠道类型，不表示已配置商户或实时可用；不新增支付配置表、不提供密钥、商户配置或修改接口。

分页统一 R.page 的 rows/total。页码默认1，范围1..1000000；每页默认20，最大100。支付单支持精确 paymentNo/orderNo/channel/status 筛选，状态 WAITING/SUCCESS/CLOSED。支付退单支持精确 afterSaleNo/paymentNo/status 筛选，状态 WAITING/SUCCESS/FAILED。查询详情不存在时返回统一业务失败。

支付单响应：id/paymentNo/orderNo/channel/channelTradeNo/amount/status，不暴露内部 notifyId。支付退单响应：refundNo/afterSaleNo/paymentNo/orderNo/channel/amount/status/channelRefundNo。金额使用项目统一两位小数字符串，单位元。各接口 Req/Res 独立，领域模型不进入 Controller。

发起接口只接收路径售后编号；不接受客户端指定支付单、渠道或退款金额。商品售后现已支持部分退款、发货后售后与退货退款；须先同意，退货退款须确认收到退货。原成功支付必须唯一，金额、会员与订单一致，售后金额不能超过剩余支付额度，并且支付后库存/核券处理已完成。详见 order-after-sale-logistics-api.md。

## 状态与幂等

1. 按订单 → 售后顺序加锁，为售后单独立提交唯一 trade_payment_refund，并将售后 REQUESTED → REFUNDING。支付退单编号独立于售后编号，after_sale_no 唯一。
2. 退出本地事务后调用原支付渠道。先用原退款单号查询渠道结果；未查到且尚未受理才使用同一个退款单号提交。适配器必须保证该单号幂等并验签、核验商户；查询异常不能当作未退款。已记录渠道受理编号而查询暂不可见时保留 WAITING，不重新提交。
3. 超时、空响应或绑定错误保留 WAITING。不得改为退款成功，也不能删掉原单后生成新编号。重试同一路径继续查原单。
4. 渠道确认 SUCCESS 后，支付库事务核对退款编号、支付编号、渠道、金额和渠道退款编号，写入退款成功与通知任务。任务在提交后通知订单服务完成售后。累计全额退款才将订单标记 REFUNDED，部分退款保留履约阶段。订单处理失败时由任务重试。
5. 渠道明确 FAILED 时仅记录支付退单失败，订单仍保留原 PAID 事实，售后仍 REFUNDING，需继续核对原渠道交易；异常不是明确失败。再次请求继续核验原单，不生成第二张退款单。
6. REFUNDING 禁止驳回和发货；REFUNDED 订单不可继续发货或再次发起售后。原支付单保留 SUCCESS 支付事实，支付退单单独记逆向交易。退款后重放原支付通知不再次确认库存和核券。

售后相关接口见 order-after-sale-logistics-api.md。库存回补和退券按订单侧售后流程处理。

## 支付发起与渠道通知

1. 会员调用 `POST /trade/app/orders/{orderNo}/payment`，请求体只含 `appKey`、`channel`。服务端校验订单归属、`PENDING_PAYMENT` 状态和金额大于 0，按应用与渠道配置生成预支付参数；金额、支付单号由服务端确定，微信付款人 openid 由服务端按会员编号查询，客户端不上传。
2. 支付尝试以 `(order_no, channel, attempt_no)` 唯一，绑定原 `app_key` 和应付金额。预支付响应返回该尝试的调起参数。
3. 渠道回调 `POST /trade/payment/notify/{appKey}/{channel}` 携带验签材料：沙箱使用 `X-Pay-Timestamp`（毫秒）和 `X-Pay-Signature`（HMAC-SHA256 十六进制），签名原文按换行拼接：渠道、支付单号、订单号、两位小数金额、渠道交易号、通知编号、时间戳；微信支付使用原生 `Wechatpay-*` 请求头。
4. 通知必须在 5 分钟时间窗内，`notifyId` 全局唯一，支付单号、渠道、应用和金额必须与本地一致；验签通过后提交支付成功、订单 PAID 并派发库存确认与权益任务。重复通知幂等，不同渠道交易号的重放被拒绝。
5. 关单会先关闭待支付渠道交易；沙箱渠道在本地直接视为撤销。
6. 退款沿用售后 `WAIT_REFUND → REFUNDING → REFUNDED` 状态机；沙箱退款适配器在 `payment.sandbox.enabled=true` 时本地返回成功，渠道退款编号为 `SBR-{refundNo}`，重复请求保持同一编号。
7. 小程序收银台先按 `appKey` 查询已启用渠道，再发起支付；沙箱渠道由会员本人调用 `sandbox-pay` 本地确认，关闭沙箱开关时该接口不可用。

### 微信支付（APIv3）

- 微信支付采用公钥模式。在后台「支付渠道配置」的微信渠道 JSON 中填写 `publicKey`（微信支付公钥 PEM 全文）与 `publicKeyId`（商户平台对应的 `PUB_KEY_ID_…`）。已有渠道需要补齐这两个字段。保留 `appId`、`mchId`、`serialNo`（商户 API 证书序列号）、`privateKey`（商户 `apiclient_key.pem` 全文）、32 位 `apiV3key` 和 `payNotifyUrl`。`appId` 与会员服务 `member.wechat.app-id` 一致，才能使用对应的 openid。
- 预支付按会员是否已绑定 openid 分流：已绑定时走 JSAPI 下单并返回 `wx.requestPayment` 参数（`appId`、`timeStamp`、`nonceStr`、`package`、`signType`、`paySign`）；未绑定时走 Native 下单，返回 `params.codeUrl` 供渲染二维码。两种方式 `payUrl` 均为空，且不返回渠道密钥。openid 由会员服务在 `POST /member/app/wechat/login` 用 `wx.login` 的 code 换取并绑定到会员，支付服务按会员编号经 Dubbo 查询，不接收客户端上传的 openid。
- 微信 API 应答及回调使用配置中的公钥验签。回调 `Wechatpay-Serial`（或 `X-Pay-Serial`）对应 `publicKeyId`，签名类型省略时由官方 SDK 使用默认 RSA 类型。支付回调解密后核对商户号、应用、币种和交易状态，金额由分换算为元。订单号由预支付的 `attach` 回传。
- 关单只有微信返回成功、`ORDER_CLOSED` 或订单不存在时才算关闭，其余结果保持待支付并由关单事件重试。
- 退款以本地退款单号作为 `out_refund_no` 幂等键，金额按分提交并核对渠道返回金额。`SUCCESS` 记为成功，`PROCESSING` 保持待退款，`CLOSED` 与 `ABNORMAL` 记为失败。渠道配置 JSON 中的 `refundNotifyUrl` 填写外部 HTTPS 地址 `/trade/payment/notify/{appKey}/WECHAT/refund`，网关沿用 `/trade/payment/notify/**` 回调白名单。
- 定时任务按原单号查询待支付及待退款的微信订单，查询到终态后复用回调的资金事务。渠道暂无退款记录时保留待确认状态。使用 `PAYMENT_RECOVERY_ENABLED` 控制扫描，默认开启，每分钟最多扫描各 50 条。

### 微信支付尝试到期

- 新建的微信支付尝试有效期为 30 分钟，发给微信的 `time_expire` 与支付库中的截止时间一致，覆盖商城支付和钱包充值。
- 到期先查原支付单。渠道已收款时补记支付结果，查单异常或关闭未确认时继续保留原尝试。确认渠道关单后，仍待支付的原业务单可以使用新的支付尝试。退款绑定实际收款的支付编号。[微信支付开发指引](https://pay.wechatpay.cn/doc/v3/merchant/4012791911)
- 历史支付单沿用原状态及编号。到期处理由 `PAYMENT_RECOVERY_ENABLED` 统一控制。

### 支付主单

- 每个商城订单或钱包充值单对应一个独立支付主单。渠道尝试按原业务单号关联主单，尝试关闭或重建时沿用主单编号。已收款主单绑定首次确认的支付尝试。
- 后台支付详情显示主单号和状态。已关闭业务收到真实款项，或同一业务出现第二笔实际收款，主单标记待对账并保留原资金流水。待对账主单暂停自动确认商城支付、充值入账及新发起充值退款。

### 钱包余额支付

- 在后台「支付渠道配置」为商城支付应用启用 `WALLET`，渠道 JSON 可留空。正式收银台提供已启用的微信和钱包余额。联调环境可通过 `payment.sandbox.enabled` 装配沙箱渠道。
- 小程序仍调用 `POST /trade/app/orders/{orderNo}/payment`。`channel` 为 `WALLET` 时，支付服务依据原订单金额扣减本人余额，扣款、支付单和通知任务一次提交。小程序收到应答后刷新支付状态。余额不足时整笔回滚。
- 订单与支付使用同一 Redis 库中的订单锁协调关单和扣款。已有其他渠道支付尝试处于未关闭状态时，钱包扣款要求先确认原支付结果。钱包订单的原路退款将金额返还原会员钱包，以原退款单号记流水。

### 钱包微信充值与原路退款

- `POST /trade/app/wallet/recharges` 接收 `requestNo`，并在 `packageId` 和 `amount` 之间二选一。`amount` 以元为单位，精确到分。自定义充值的到账金额等于实付金额，同一请求编号重试沿用原金额。
- 小程序创建充值单后，通过 `POST /trade/app/wallet/recharges/{rechargeNo}/payment` 发起微信支付，请求体只传 `appKey`。实付金额取充值单快照，登录会员须为充值单本人。页面从 `paymentAppKey` 取得商城应用配置。
- 微信支付回调或成功查单在资金事务内一次写入实付和赠送余额。新建微信充值由渠道结果确认到账，历史人工充值沿用原操作。
- 后台退款先冻结实付与赠送的合计余额，微信原路退回实付金额。成功后扣回冻结余额，失败后释放冻结；渠道处理中显示「退款中」，回调、定时查询和后台重试均使用同一退款单号。原始充值和订单支付分别统计。

### 支付单手续费

第一次确认支付成功时，保存渠道费率和按费率计算的手续费。计算单位为元，四舍五入到分。钱包支付费率与手续费为零。历史交易或费率未知的交易留空。后台支付详情可查看这两个字段。

## 表与部署

支付库初始化使用 [lemnt_pay.sql](../lemnt-server/lemnt-pay/sql/lemnt_pay.sql)。微信公钥和回调地址保存在原渠道 JSON，补查复用 `pay_payment` 和 `pay_payment_refund`。存量支付库如仍有待发送的 Outbox 记录，按 [切流说明](module-split-cutover.md) 迁入通知任务。

已有支付库在部署手续费快照前，先执行 [手续费字段升级](../lemnt-server/sql/upgrade/migrate_pay_payment_channel_fee.sql)。

部署微信尝试有效期前，暂停旧支付实例的写入和调度，再执行 [支付尝试升级](../lemnt-server/sql/upgrade/migrate_pay_payment_attempt_expiry.sql)。历史支付单保留序号 1，有效期为空。

部署支付主单前，在同一停写窗口执行 [主单迁移](../lemnt-server/sql/upgrade/migrate_pay_payment_order.sql)。按历史尝试分组核对金额、会员归属与实收笔数。金额或归属缺失时保留空值。仅有关闭尝试的历史业务保留待支付主单，是否允许创建新尝试仍由原业务状态决定。

## 验证与接入边界

真实生产 Mapper、H2 MySQL 模式、事务代理覆盖退款成功、重复提交、并发唯一、未知结果、渠道受理后查询暂不可见、结果金额错误、拒绝售后、写入回滚、外层事务回滚、分页过滤；HTTP 测试验证分页、校验、金额精度和服务端原单绑定。测试渠道替身不代表支付宝/微信退款验收。

微信支付退款适配器已接入官方 SDK。真实商户需验收支付查单、退款回调签名、回调超时重试、同编号幂等及 MySQL 和网关联调。

## 参考源码

- [芋道 PayRefundServiceImpl](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/service/refund/PayRefundServiceImpl.java)：原退款编号、未知结果保留、渠道查询、独立提交。
- [芋道 AfterSaleServiceImpl](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/aftersale/AfterSaleServiceImpl.java)：售后发起退款、核对退款结果后完成售后。
- [芋道 PayChannelController](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/channel/PayChannelController.java)：原项目有完整渠道管理；本项目按用户要求仅展示支持渠道，未搬入商户应用和渠道配置 CRUD。

参考源码通过 Gitee raw 下载核对；下载内容的 SHA256 留在工作项证据中。引用分支可能更新，本轮未全量复制原工程。

## 2026-10-31 后台页面

已注册trade/payment/index、trade/payment-refund/index、trade/payment-channel/index三页。支付单可按支付单号/订单号/渠道/状态查询，支付退单按售后单/原支付单/状态查询，详情权限与列表权限独立。支持渠道只展示编码名称，按已确认范围不新增密钥或启用配置表单，不表示渠道SDK已接通。页面保留trade前缀，实际路径与上表Controller一致。

执行V20261031__trade_payment_menu.sql新增菜单后按既有权限流程授权；脚本可重入且不自动授予角色。本次只在H2测试，未执行真实库。新增前端合同/权限测试和已有4项HTTP契约、1项菜单测试通过；浏览器联调和部署验收仍待完成。
