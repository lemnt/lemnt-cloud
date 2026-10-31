# 后台订单、商品售后与物流接口

> 2026-10-28：售后标准表、数值主键、操作日志及本人取消以 [售后迁移接口](after-sale-log-api.md) 为准。旧整单申请入口已退役，以下为此前批次的接口记录。

2026-10-27。根据用户指定的芋道项目，补充当前订单与物流模型能够承接的后台管理接口，并按用户确认支持商品部分退款、发货后售后与退货退款。整单兼容入口已在后续批次退役；商品售后使用独立契约。不表示芋道所有订单功能已经逐项复制。

## 订单管理

| 接口 | 权限 | 行为 |
| --- | --- | --- |
| GET /trade/admin/orders | trade:fulfillment:list | 原有订单分页 |
| GET /trade/admin/orders/{orderNo} | trade:fulfillment:query | 原有订单详情 |
| POST /trade/app/orders/{orderNo}/cancel | 本人会员 | 本人取消未支付订单；后台手工关单按芋道范围退役，系统超时关单仍保留 |
| GET /trade/admin/orders/summary | trade:order:summary | 按可空的精确 orderNo 查询四项统计 |
| GET /trade/admin/orders/{orderNo}/remark | trade:order:query | 后台备注 |
| PUT /trade/admin/orders/{orderNo}/remark | trade:order:remark | remark 必填、最多500字，空字符串清除 |

统计返回 orderCount、orderPayPrice、afterSaleCount、afterSalePrice。前两项为订单数量和订单应付金额合计；后两项只统计 SUCCESS 支付退款笔数和金额，不把待处理申请算作退款，也不把应付金额当作净收入。人民币金额为两位小数字符串。统计范围仅支持精确订单号或全部订单，不隐含列表的其他筛选条件。

本轮不新增订单调价、自提核销或发货前地址修改：当前没有自提模型，订单快照和支付金额仍由既有下单流程确定，收件信息仍在发货时登记。用户要求的范围是后台接口，此轮没有新增 React 管理页面。

## 商品售后

| 接口 | 权限 | 行为 |
| --- | --- | --- |
| GET /trade/admin/orders/{orderNo}/after-sale-items | trade:after-sale:query | 查询真实订单商品行ID、剩余数量和剩余可退款金额 |
| POST /trade/admin/orders/{orderNo}/after-sales | trade:after-sale:create | 申请商品售后 |
| GET /trade/admin/after-sales | trade:after-sale:list | 独立售后分页 |
| GET /trade/admin/after-sales/{no} | trade:after-sale:query | 含审批与退货物流的详情 |
| POST /trade/admin/after-sales/{no}/agree | trade:after-sale:agree | 同意售后 |
| POST /trade/admin/after-sales/{no}/reject | trade:after-sale:reject | 拒绝待审核申请，reason 必填 |
| POST /trade/admin/after-sales/{no}/return | trade:after-sale:return | 登记退货承运商 carrierCode 和运单 trackingNo |
| POST /trade/admin/after-sales/{no}/receive | trade:after-sale:receive | 确认收到退货，方可退款 |
| POST /trade/admin/after-sales/{no}/refuse | trade:after-sale:refuse | 拒收，reason 必填 |
| POST /trade/admin/after-sales/{no}/refund | trade:after-sale:refund | 发起或同步原支付退款交易 |

申请体仅 requestNo、orderItemId、quantity、type、reason。type 为 REFUND_ONLY 或 RETURN_REFUND，不接受金额、会员或支付单编号。quantity 范围1..999，必须属于路径订单并在剩余可售后数量内。requestNo 在订单内幂等，重放核对商品行、数量、方式和原因，返回原记录的当前状态。

仅已支付的 PAID、SHIPPED、COMPLETED 订单可申请商品售后；RETURN_REFUND 必须在发货后。旧 `/refund-requests` 申请接口已退役，不作为当前入口。

状态流转：

- 仅退款：REQUESTED → WAIT_REFUND → REFUNDING → REFUNDED。
- 退货退款：REQUESTED → AWAITING_RETURN → RETURNED → WAIT_REFUND → REFUNDING → REFUNDED。
- 待审核可 REJECTED；已寄回可 RETURN_REJECTED。二者释放本次商品数量及金额额度，须新 requestNo 再申请。
- 同意、登记退货、确认收到、拒绝和拒收均检查当前状态。重复操作保留持久化后的时间和运单，不能改写已登记运单、跳过收货或倒退退款状态。

所有写操作先锁订单，再校验售后；不同请求争抢同一商品额度也串行。商品额度同时计入待审核、退货中、待退款、退款中和已退款记录，拒绝或拒收记录不占用。列表与详情对活动售后的判断一致；活动售后期间禁止订单发货和收货。

### 金额与原支付

当前订单行存原价快照，优惠保存在整单实付金额。因此退款额度按原价权重分摊实付金额：按持久化商品行ID排序，累计分摊后向下保留两位小数，分币尾差归后续商品，行金额合计严格等于实付。行内按剩余金额和剩余数量分配，最后剩余数量取得剩余金额；拒绝申请后重新计算剩余额度，避免拒绝重申请导致分币累计超退。单次不足一分的金额拒绝并提示合并数量。

部分退款保留原 PAID/SHIPPED/COMPLETED 履约阶段。累计真实成功退款达到整单实付时才标记订单 REFUNDED。原支付单保留 SUCCESS 支付事实，逆向交易独立记录。支付退款服务再次校验原支付、售后和累计金额；未审批或退货未收货均不能调用渠道。

实际金融退款仍需接入 PaymentRefundClient，缺失或重复适配器明确失败。未知响应保留原 WAITING 交易编号，重试先查原渠道，不重新生成退款单。退款记录与售后、订单完成状态在同一本地事务提交。仅金融退款和退货信息流程已实现，库存回补、返券和实物仓库操作不在此接口中自动执行。

## 物流管理

| 接口 | 权限 | 行为 |
| --- | --- | --- |
| GET /trade/admin/shipments | trade:shipment:list | 独立物流单分页，不含收件人信息 |
| GET /trade/admin/shipments/{orderNo} | trade:shipment:query | 物流单详情，含收件信息 |
| GET /trade/admin/shipments/{orderNo}/tracks | trade:shipment:track | 仅按本地登记运单查询真实渠道轨迹 |
| POST /trade/admin/orders/{orderNo}/shipment | trade:fulfillment:ship | 保留原有整单发货登记 |
| POST /trade/admin/orders/{orderNo}/receive | trade:fulfillment:receive | 保留原有收货登记 |

物流分页接受 orderNo、carrierCode、trackingNo、status 精确筛选。status 为 SHIPPED（未登记收货）或 RECEIVED（已登记收货），描述物流登记状态。列表仅返回订单号、承运商、运单、发货和收货时间；详情另加收件人、电话、地址。所有分页默认1/20，最大页码1000000、每页100，统一 R.page 的 rows/total。

轨迹只返回 time、description。ShipmentTrackClient 负责真实承运商鉴权、超时及响应转换，目前未接入实际承运商。无适配器、重复适配器、查询失败或畸形响应明确报错，不能冒充暂无轨迹；真实查询成功但暂无轨迹可以返回空列表。不允许调用方替换本地运单号查询任意包裹。

## 持久化与升级

不新增售后或物流表。trade_refund_request 扩展商品行、数量、方式和退货物流字段，trade_order 仅补充后台 remark。订单行 ID 复用 trade_order_item 的真实主键。Gateway/Mapper 按现有表扩展，DO 使用既有表注解；所有 Mapper 继承 BaseMapper，行锁及聚合查询使用明确 SQL。

新库初始化使用当前 V1、V2、V5、V6。已有 V5/V6 库使用 upgrade/order_management.sql、upgrade/item_after_sale.sql 各执行一次；早于 V6 的库先按支付管理文档升级。保留既有整单售后，默认类型 REFUND_ONLY、商品行和数量为空。

系统库权限脚本 V20261027__trade_fulfillment_management_permissions.sql 仅新增功能权限，不创建未交付页面或授予角色。升级脚本未在目标 MySQL 执行，未连接实际支付、物流渠道验证。

## 参考与验证

参考用户指定的 [芋道项目](https://gitee.com/zhijiantianya/yudao-cloud) 的 TradeOrderController、TradeOrderSummaryRespVO、AfterSaleController、AfterSaleServiceImpl 以及订单物流轨迹查询。DeliveryExpressController 是快递公司配置，不等同于物流单；本轮不额外新增快递公司配置页面和表。

真实 Mapper、H2 MySQL 模式和事务代理验证部分退款、尾差、退货流程、原支付绑定、并发额度、历史整单兼容、成功统计、备注清除和物流查询；HTTP 契约验证独立 Req/Res、参数校验、金额序列化与权限路由。测试渠道替身不代表真实供应商验收。完整计数及日志见 work-items/member-domain-structure/14-order-after-sale-logistics.md。
