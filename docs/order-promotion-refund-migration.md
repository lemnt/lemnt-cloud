# 订单优惠快照与按数量部分退款迁移

2026-11-02，第164批。本文是发布操作说明，不表示已在现有数据库执行或完成真实环境验收。

## 交付行为

- trade保存订单优惠、商品行分摊及退单生成时的优惠归因；mall保留活动规则、资格、库存执行及去重职责。
- 订单明细新增`discount_amount`等金额快照和`refunded_quantity`。不保存处理中数量，也不保存退款单价。
- 生成退单的同一事务先锁订单，再按已有`trade_after_sale.order_no`查询处理中退单。同请求幂等返回；不同请求必须等当前退单结束。
- 单件现金基准=原行实付÷原购买数量，向下保留两位；非末次按基准×本次数量，末次按原行实付减历史成功退款结清。10元/6件退2、3、1件，金额为3.32、4.98、1.70元。
- 退单生成后金额和权益方案冻结。审批、渠道执行、失败重试不重算价格、优惠或积分。
- 优惠金额是归因，不额外退现金。原价100、优惠20、实付80、2件退1件，现金40、优惠归因10。
- 合法零现金份额按数量完成售后，不生成虚构支付退款流水，不向渠道发0元请求。退款响应`refundNo`为空、`channel=NONE`；仍有售后号、已退数量及权益任务。
- 积分保留累计数量比例规则，在生成退单时计算并保存；后续member只执行明确数量。赠积分回收不足保留失败任务，不悄悄缩减冻结方案。
- 支付前调价使用独立有符号差额；支付赠积分使用建单时保存的系数重算，固定活动赠礼不改。已有支付记录则拒绝调价。
- 所有商品行退完且现金总额核对一致，订单才进入REFUNDED；退完现金但尚有零现金商品未退，不提前结束。

金额单位保持元，HTTP沿用两位小数字符串。积分是数量。规格继续使用`specs/specId/specName/specValueId/specValueName`。

## 表结构

新增三张交易结果表：

| 表 | 用途 |
|---|---|
| trade_order_promotion | 原订单采用的一项优惠或权益来源，可一单多项 |
| trade_order_item_promotion | 按真实order_item_id保存行分摊，不以SKU代替订单明细 |
| trade_order_promotion_refund | 某次售后对原行优惠的冻结归因，不逐项重复保存现金退款额 |

订单和明细保留原价、优惠、原始运费、运费减免、人工调价等独立口径；明细使用/赠送积分及已退数量单独保存。历史未知字段保持NULL，订单标记LEGACY_PARTIAL。新订单由应用显式写已退数量0；不能把历史NULL默认改成0。

新库按当前初始化脚本执行V1至V18。其中V1/V5/V11包含新增字段，V18只建三张扩展表。已有库只使用：

`lemnt-server/lemnt-trade/sql/upgrade/order_promotion_snapshot.sql`

该升级脚本不是幂等脚本，需备份、核对实际SHOW CREATE TABLE、确认缺失列和约束后执行一次；不要在已有库重跑V1/V5初始化。正式执行前应在目标MySQL校验方言和锁表影响。本轮未执行现有库DDL。

mall原三张促销执行表不会跨库重命名或直接删除。存量活动仍需它们完成库存与请求去重；财务优惠结果由trade负责。旧执行表退役应另列批次，先排空旧订单及通知任务。

## 历史数据核对

先暂停新建售后及相关写入，完成备份与高水位记录，再进行以下核对。核对期间保留原已生成退单金额，不用新规则改写旧退单。

1. 查同订单多笔处理中售后，先完成排空或业务核对，不擅自取消：

```sql
SELECT order_no, COUNT(*) AS processing_count
FROM trade_after_sale
WHERE deleted=0 AND status IN ('REQUESTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','REFUNDING')
GROUP BY order_no HAVING COUNT(*)>1;
```

2. 核对售后与明细的真实归属，隔离跨订单或SKU不一致的历史行：

```sql
SELECT a.id, a.order_no, a.order_item_id, o.order_no AS item_order_no
FROM trade_after_sale a
JOIN trade_order_item i ON i.id=a.order_item_id
JOIN trade_order o ON o.id=i.order_id
WHERE a.order_no<>o.order_no OR (a.sku_id IS NOT NULL AND a.sku_id<>i.sku_id);
```

3. 按REFUNDED售后事实汇总成功数量，核对支付退款成功记录和金额；整单历史售后缺少行关联、逻辑删除的成功售后、缺失历史记录均不能视为0。存在整单售后或无法证明历史完整的订单，明细计数保持NULL，禁止新建售后直至核清。
4. 经人工或可审计程序确认的逐行结果，再按主键回填`refunded_quantity`；核对不得超过原购买数量。新代码会阻止对NULL未知计数发起新售后，也会拒绝缓存计数与成功售后不一致的行。
5. `active_order_no`原唯一键作为辅助防线，先核清多笔处理中记录，再为所有类型的处理中售后填入order_no，结束态清空。业务判断仍为订单锁内的状态查询。
6. 只有原订单内SKU唯一匹配时，才能为旧mall行映射trade明细；多义映射隔离。不得按名称、当前商品价、当前活动名或当前积分配置补造历史。
7. 原优惠、原始运费、调价差额无法证明时保持未知。`line_amount+delivery_price-pay_amount`只是未拆分差额，不能直接断言全为优惠。已返积分以账户流水核对，不按当前配置猜测。

需要回填的候选清单、异常行、审核依据、执行前后数量和金额校验和应归档。本文未生成针对运行中数据库的真实回填清单，也未自动更新业务数据。

## RPC与发布顺序

完整报价和冻结权益使用独立入口，避免旧服务忽略新字段后执行旧规则：

| 入口 | 用途 |
|---|---|
| TradeOrderClient.createPriced(OrderCreateBo) | 完整报价建单，包含金额与实际生效优惠分摊 |
| CheckoutLifecycleClient.refunded(RefundBenefitsBo) | trade向mall传递真实明细、数量和冻结权益 |
| MemberGrowthClient.applyFrozenRefund(MemberTradeAssetBo) | mall向member执行冻结积分结果 |

旧入口和旧构造继续兼容存量调用；未升级消费者遇到新入口必须拒绝，保留重试，不回落旧计算。旧POINT支付退款记录继续读取并使用原REFUND/PR任务，不因重试另建AFTER_SALE任务。

建议顺序：

1. 核对DDL、备份与维护窗口，先安装trade增量结构。
2. 部署支持新冻结入口的member服务。
3. 暂停并停止所有旧trade通知执行器，再部署新trade。任务新增lease_until与单调execute_version，新旧领取实现不能混跑；租约采用数据库时间，当前120秒覆盖已有5秒RPC与本次任务步骤预算，跨服务执行仍须业务幂等。
4. 部署新mall消费者及报价生产者。旧mall调用新trade旧入口仍可兼容；新mall使用createPriced，旧trade会拒绝。
5. 部署管理端和小程序。后台优惠查询复用原权限，会员查询同时验证订单与售后归属。
6. 核对历史数量、恢复写入，按清单做真实环境验收；扫描失败任务并执行有审计的补偿。

退款成功、已退数量和权益任务同一trade事务提交；远程执行在提交之后。任务被其他执行者领取或权益失败，不改变现金成功事实；权益状态单独返回PENDING/FAILED/SUCCESS。人工重置也增加执行版本，旧结果不能覆盖新执行。会员退款幂等号关联原权益流水主键，兼容旧幂等号，避免40位订单号与RF组合超过64位。

## 查询与页面

新增GET：

- `/trade/admin/orders/{orderNo}/promotions`，权限`trade:fulfillment:query`。
- `/trade/admin/after-sales/{no}/promotions`，权限`trade:after-sale:query`。
- `/trade/app/orders/{orderNo}/promotions`，只读本人订单。
- `/trade/app/after-sales/{no}/promotions`，只读本人售后。

保留原R包装和既有详情字段。可售后商品列表保留数组，追加refundedQuantity；有处理中退单时各行可申请数量为0。管理端订单详情展示已退/可申请数量，售后处理列表展示本次数量；小程序申请只提交数量与原因。优惠归因与现金退款分开，应返积分必须结合权益处理状态展示。

原后台订单统计接口仍统计成功支付退款交易，零现金售后不会增加支付退款笔数；商品已退数量以订单明细和成功售后事实为准。

## 回退与验收

回退应用路由，保留已写快照、退单、支付退款及账户流水。不得删除新表、清空任务或重算已支付价格来回退；旧版不能正确处理新增AFTER_SALE任务，切回旧执行器前须排空或暂停并保留这些任务。

真实验收至少覆盖：10元6件多次部分退、1分钱3件零现金份额、多商品有未退零现金行、重复通知、现金成功而权益暂失败、会员余额不足回收、调价时当前积分配置变化、旧POINT记录和历史NULL。逐笔对齐订单/明细、售后/渠道退款、普通及活动库存、券和积分流水。

本地测试与真实MySQL锁/DDL、真实HTTP网关、Dubbo、Redis、MQ、支付渠道及浏览器/微信真机是不同证据层；未运行的层在交付报告标为未验证。
