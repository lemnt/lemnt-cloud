# 后台支付渠道、支付单和支付退单

2026-10-27。范围按用户定义：支付配置展示支持渠道；支付退单是已有订单售后单触发的逆向支付交易。参考芋道支付退款和售后分工，按本项目 Adapter → Application → Domain / Infrastructure 结构实现。消费流程不增加后台页面。

## 接口

| 方法与路径 | 权限 | 功能 |
| --- | --- | --- |
| GET /trade/admin/payment-channels | trade:payment-channel:list | 展示支持的渠道编码和名称 |
| GET /trade/admin/payments | trade:payment:list | 支付单分页 |
| GET /trade/admin/payments/{no} | trade:payment:query | 支付单详情 |
| GET /trade/admin/payment-refunds | trade:payment-refund:list | 支付退单分页 |
| GET /trade/admin/payment-refunds/{no} | trade:payment-refund:query | 支付退单详情 |
| POST /trade/admin/refund-requests/{afterSaleNo}/payment-refund | trade:payment-refund:create | 基于售后单发起退款或查询同步原退款结果 |

支付配置与支付创建复用 PaymentChannel：ALIPAY（支付宝）、WECHAT（微信支付）。仅表明系统渠道类型，不表示已配置商户或实时可用；不新增支付配置表、不提供密钥、商户配置或修改接口。

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
