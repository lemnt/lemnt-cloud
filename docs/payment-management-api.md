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
4. 渠道确认 SUCCESS 后，在一个独立本地事务核对退款编号、支付编号、渠道、金额、渠道退款编号，更新支付退单 SUCCESS、售后 REFUNDED；累计全额退款才将订单标记 REFUNDED，部分退款保留履约阶段。任一步写入失败全部回滚，下一次请求查渠道后同步原结果。
5. 渠道明确 FAILED 时仅记录支付退单失败，订单仍保留原 PAID 事实，售后仍 REFUNDING，需继续核对原渠道交易；异常不是明确失败。再次请求继续核验原单，不生成第二张退款单。
6. REFUNDING 禁止驳回和发货；REFUNDED 订单不可继续发货或再次发起售后。原支付单保留 SUCCESS 支付事实，支付退单单独记逆向交易。退款后重放原支付通知不再次确认库存和核券。

商品售后和物流管理的后续接口见 order-after-sale-logistics-api.md。金融退款不等同于库存回补或退券；这类售后资源处理尚未接入。没有新增 Outbox、轮询任务或未验签的公网退款回调。

## 支付发起与渠道通知

1. 会员调用 `POST /trade/app/orders/{orderNo}/payment`，请求体只含 `appKey`、`channel`。服务端校验订单归属、`PENDING_PAYMENT` 状态和金额大于 0，按应用与渠道配置生成预支付参数；金额、支付单号由服务端确定，微信付款人 openid 由服务端按会员编号查询，客户端不上传。
2. 支付单以 `(order_no, channel)` 唯一并绑定 `app_key`；预支付响应不包含渠道密钥。
3. 渠道回调 `POST /trade/payment/notify/{appKey}/{channel}` 携带验签材料：沙箱使用 `X-Pay-Timestamp`（毫秒）和 `X-Pay-Signature`（HMAC-SHA256 十六进制），签名原文按换行拼接：渠道、支付单号、订单号、两位小数金额、渠道交易号、通知编号、时间戳；微信支付使用原生 `Wechatpay-*` 请求头。
4. 通知必须在 5 分钟时间窗内，`notifyId` 全局唯一，支付单号、渠道、应用和金额必须与本地一致；验签通过后提交支付成功、订单 PAID 并派发库存确认与权益任务。重复通知幂等，不同渠道交易号的重放被拒绝。
5. 关单会先关闭待支付渠道交易；沙箱渠道在本地直接视为撤销。
6. 退款沿用售后 `WAIT_REFUND → REFUNDING → REFUNDED` 状态机；沙箱退款适配器在 `payment.sandbox.enabled=true` 时本地返回成功，渠道退款编号为 `SBR-{refundNo}`，重复请求保持同一编号。
7. 小程序收银台先按 `appKey` 查询已启用渠道，再发起支付；沙箱渠道由会员本人调用 `sandbox-pay` 本地确认，关闭沙箱开关时该接口不可用。

### 微信支付（APIv3）

- 渠道配置 JSON 需包含 `appId`、`mchId`、`serialNo`（商户证书序列号）、`privateKey`（apiclient_key.pem 内容）、32 位 `apiV3key`（通知解密）和 `payNotifyUrl`；缺失时发起支付直接失败，不发出无效请求。`appId` 必须与会员服务 `member.wechat.app-id` 一致，openid 才可用于本应用支付。
- 预支付按会员是否已绑定 openid 分流：已绑定时走 JSAPI 下单并返回 `wx.requestPayment` 参数（`appId`、`timeStamp`、`nonceStr`、`package`、`signType`、`paySign`）；未绑定时走 Native 下单，返回 `params.codeUrl` 供渲染二维码。两种方式 `payUrl` 均为空，且不返回渠道密钥。openid 由会员服务在 `POST /member/app/wechat/login` 用 `wx.login` 的 code 换取并绑定到会员，支付服务按会员编号经 Dubbo 查询，不接收客户端上传的 openid。
- 回调按 `Wechatpay-Serial`、`Wechatpay-Signature`、`Wechatpay-Timestamp`、`Wechatpay-Nonce`、`Wechatpay-Signature-Type` 验签（也接受同名 `X-Pay-*` 头），官方 SDK 解密后校验商户号、appId、币种与交易状态，金额由分换算为元；订单号随预支付 `attach` 回传，缺失即拒绝。
- 关单只有微信返回成功、`ORDER_CLOSED` 或订单不存在时才算关闭，其余结果保持待支付并由关单事件重试。
- 退款以本地退款单号作为 `out_refund_no` 幂等键，金额按分提交并核对渠道返回金额；`SUCCESS` 记为成功、`PROCESSING` 保持待退款、`CLOSED` 与 `ABNORMAL` 记为失败（后者需人工核对）。未配置退款通知地址，结果在下次发起退款时查询同步，查询不到才提交新退款。


## 表与部署

唯一新增业务表是支付退款交易 trade_payment_refund，DO、Gateway、Mapper 独立按表划分，Mapper 使用 MyBatis Plus BaseMapper。售后申请继续使用原 trade_refund_request，不合并成支付退单，也不新增支付配置表。

新库使用扩展售后状态的 V5 与 V6__payment_refund.sql。已有 V5 的库先核对并执行 upgrade/payment_refund.sql 扩展检查约束，再执行 V6。系统库可执行 V20261027__trade_payment_management_permissions.sql 添加功能权限，不授予角色、不注册未交付页面。以上脚本均未在实际环境执行。

## 验证与接入边界

真实生产 Mapper、H2 MySQL 模式、事务代理覆盖退款成功、重复提交、并发唯一、未知结果、渠道受理后查询暂不可见、结果金额错误、拒绝售后、写入回滚、外层事务回滚、分页过滤；HTTP 测试验证分页、校验、金额精度和服务端原单绑定。测试渠道替身不代表支付宝/微信退款验收。

真实渠道 PaymentRefundClient 仍未接入。缺失或重复适配器明确失败，不模拟退款成功。正式启用前需要接入原商户渠道、验证签名和超时及同编号幂等行为，并验收目标 MySQL、网关权限和渠道端到端流程。此文不表示九类后台管理页面全部完成。

## 参考源码

- [芋道 PayRefundServiceImpl](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/service/refund/PayRefundServiceImpl.java)：原退款编号、未知结果保留、渠道查询、独立提交。
- [芋道 AfterSaleServiceImpl](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/service/aftersale/AfterSaleServiceImpl.java)：售后发起退款、核对退款结果后完成售后。
- [芋道 PayChannelController](https://gitee.com/zhijiantianya/yudao-cloud/blob/master/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/controller/admin/channel/PayChannelController.java)：原项目有完整渠道管理；本项目按用户要求仅展示支持渠道，未搬入商户应用和渠道配置 CRUD。

参考源码通过 Gitee raw 下载核对；下载内容的 SHA256 留在工作项证据中。引用分支可能更新，本轮未全量复制原工程。

## 2026-10-31 后台页面

已注册trade/payment/index、trade/payment-refund/index、trade/payment-channel/index三页。支付单可按支付单号/订单号/渠道/状态查询，支付退单按售后单/原支付单/状态查询，详情权限与列表权限独立。支持渠道只展示编码名称，按已确认范围不新增密钥或启用配置表单，不表示渠道SDK已接通。页面保留trade前缀，实际路径与上表Controller一致。

执行V20261031__trade_payment_menu.sql新增菜单后按既有权限流程授权；脚本可重入且不自动授予角色。本次只在H2测试，未执行真实库。新增前端合同/权限测试和已有4项HTTP契约、1项菜单测试通过；浏览器联调和部署验收仍待完成。
