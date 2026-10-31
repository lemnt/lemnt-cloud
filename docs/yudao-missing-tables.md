# 商城复刻缺表清单

> 2026-10-28 最新售后批次：标准 `trade_after_sale` 与新 `trade_after_sale_log` 已接入真实流程，初始化46张表，仍缺36张业务表（另2演示不迁）。字段和完整App售后仍有缺口，见 [售后当前接口](after-sale-log-api.md)。以下各批次表数保留为历史基线。
> 2026-10-28 完整迁移批次新增 `trade_order_log` 并贯通真实订单操作，初始化表当前45张，仍无对应表为39张（37业务+2演示）；优惠券快照与规则及订单日志见 [优惠券接口](coupon-migration-api.md)、[操作日志接口](order-operation-log-api.md)。有表不代表完整功能已迁移。
> 2026-10-28 后续配送贯通已完成本地验证：商品模板/重量体积、本人地址、运费结算、订单冻结地址及部分退款见 [配送结算接口](delivery-checkout-api.md)。配送批次时仍缺38张业务表，当前剩余以本页最上方更新为准；未做真实环境和渠道验收。

> 2026-10-28 配送基础批次：V7 新增 trade_config、trade_delivery_express、trade_delivery_express_template、trade_delivery_express_template_charge、trade_delivery_express_template_free 共5张表，已配管理接口与计价服务。初始化表现为44张，参考无对应表从45降至40（38业务+2演示）。下文74/39/45是原始盘点基线；有表不代表结算链路或页面已完成。接口及当前边界见 [配送基础接口](trade-delivery-api.md)。

2026-10-28。参考本地 `/Users/wangxx/demo/yudao-cloud/` 的商品、促销、交易、支付、会员 DO，当前项目读取 mall/trade/member 根目录初始化 SQL。未查询实际运行数据库；“缺表”指当前代码/初始化结构中没有对应业务表。参考 DO 的源路径和 SHA-256 见字段证据 JSON。

## 结论

| 指标 | 数量 | 含义 |
| --- | ---: | --- |
| 参考业务及演示表 | 74 | product 10、promotion 23、trade 16、pay 14、member 11 |
| 当前初始化表 | 39 | 含辅助表和应清理的残留表 |
| 有对应模型的参考表 | 29 | 映射至当前 28 张表；只代表找到对应模型，字段/状态仍可能不完整 |
| 没有对应表 | 45 | 包含 43 张业务表、2 张支付演示表 |
| 当前未与参考一一对应的表 | 11 | 存储拆分、交易补偿及认证残留，逐项列在后文 |

因此不能用“39 对 74 相差 35 张”来计算缺表数；表改名、合并及额外辅助表会掩盖缺失。本清单取代按名称粗略比较的结果，不是功能覆盖率。

## 缺失的 45 张表

### 商品：3 张

收藏、浏览历史和商品日统计。前两项是会员商品交互能力，统计不是商品主表字段的简单替代。

- `product_favorite` — [ProductFavoriteDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/favorite/ProductFavoriteDO.java)
- `product_browse_history` — [ProductBrowseHistoryDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/history/ProductBrowseHistoryDO.java)
- `product_statistics` — [ProductStatisticsDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/dal/dataobject/product/ProductStatisticsDO.java)

### 交易基础及日志：3 张

交易/售后原因配置，订单与售后操作轨迹。属于主交易链路模型，优先补齐。

- `trade_config` — [TradeConfigDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/config/TradeConfigDO.java)
- `trade_order_log` — [TradeOrderLogDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/order/TradeOrderLogDO.java)
- `trade_after_sale_log` — [AfterSaleLogDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/aftersale/AfterSaleLogDO.java)

### 配送：5 张

快递公司、运费模板、地区计费、地区包邮及自提门店。当前 trade_shipment 仅保存发货事实，无法替代。

- `trade_delivery_express` — [DeliveryExpressDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/delivery/DeliveryExpressDO.java)
- `trade_delivery_express_template` — [DeliveryExpressTemplateDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/delivery/DeliveryExpressTemplateDO.java)
- `trade_delivery_express_template_charge` — [DeliveryExpressTemplateChargeDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/delivery/DeliveryExpressTemplateChargeDO.java)
- `trade_delivery_express_template_free` — [DeliveryExpressTemplateFreeDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/delivery/DeliveryExpressTemplateFreeDO.java)
- `trade_delivery_pick_up_store` — [DeliveryPickUpStoreDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/delivery/DeliveryPickUpStoreDO.java)

### 交易统计：1 张

交易日统计。现有订单查询不等于统计落表和统计任务。

- `trade_statistics` — [TradeStatisticsDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-statistics-server/src/main/java/cn/iocoder/yudao/module/statistics/dal/dataobject/trade/TradeStatisticsDO.java)

### 分销：3 张

推广关系、佣金流水和提现流程，整组尚未迁移。

- `trade_brokerage_user` — [BrokerageUserDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/brokerage/BrokerageUserDO.java)
- `trade_brokerage_record` — [BrokerageRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/brokerage/BrokerageRecordDO.java)
- `trade_brokerage_withdraw` — [BrokerageWithdrawDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/brokerage/BrokerageWithdrawDO.java)

### 商城内容、装修、客服：7 张

文章/分类、轮播、装修模板/页面、客服会话/消息，整组尚未迁移。

- `promotion_article` — [ArticleDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/article/ArticleDO.java)
- `promotion_article_category` — [ArticleCategoryDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/article/ArticleCategoryDO.java)
- `promotion_banner` — [BannerDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/banner/BannerDO.java)
- `promotion_diy_template` — [DiyTemplateDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/diy/DiyTemplateDO.java)
- `promotion_diy_page` — [DiyPageDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/diy/DiyPageDO.java)
- `promotion_kefu_conversation` — [KeFuConversationDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/kefu/KeFuConversationDO.java)
- `promotion_kefu_message` — [KeFuMessageDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/kefu/KeFuMessageDO.java)

### 活动：11 张

秒杀场次与商品、拼团与参团记录、砍价与助力、积分商城活动与商品。满减/折扣主表不能替代这些活动。

- `promotion_seckill_activity` — [SeckillActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/seckill/SeckillActivityDO.java)
- `promotion_seckill_config` — [SeckillConfigDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/seckill/SeckillConfigDO.java)
- `promotion_seckill_product` — [SeckillProductDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/seckill/SeckillProductDO.java)
- `promotion_combination_activity` — [CombinationActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/combination/CombinationActivityDO.java)
- `promotion_combination_product` — [CombinationProductDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/combination/CombinationProductDO.java)
- `promotion_combination_record` — [CombinationRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/combination/CombinationRecordDO.java)
- `promotion_bargain_activity` — [BargainActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/bargain/BargainActivityDO.java)
- `promotion_bargain_record` — [BargainRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/bargain/BargainRecordDO.java)
- `promotion_bargain_help` — [BargainHelpDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/bargain/BargainHelpDO.java)
- `promotion_point_activity` — [PointActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/point/PointActivityDO.java)
- `promotion_point_product` — [PointProductDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/point/PointProductDO.java)

### 支付基础与通知：5 张

应用、渠道配置、支付尝试、业务通知任务和日志。渠道 SDK 可后置，但这些业务状态与可靠通知模型仍然缺失；现有 MQ 补偿不应直接当成参考通知任务已迁移。

- `pay_app` — [PayAppDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/app/PayAppDO.java)
- `pay_channel` — [PayChannelDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/channel/PayChannelDO.java)
- `pay_order_extension` — [PayOrderExtensionDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/order/PayOrderExtensionDO.java)
- `pay_notify_task` — [PayNotifyTaskDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/notify/PayNotifyTaskDO.java)
- `pay_notify_log` — [PayNotifyLogDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/notify/PayNotifyLogDO.java)

### 钱包及转账：5 张

转账、余额账户、账户流水、充值及充值套餐，整组尚未迁移。

- `pay_transfer` — [PayTransferDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/transfer/PayTransferDO.java)
- `pay_wallet` — [PayWalletDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/wallet/PayWalletDO.java)
- `pay_wallet_transaction` — [PayWalletTransactionDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/wallet/PayWalletTransactionDO.java)
- `pay_wallet_recharge` — [PayWalletRechargeDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/wallet/PayWalletRechargeDO.java)
- `pay_wallet_recharge_package` — [PayWalletRechargePackageDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/wallet/PayWalletRechargePackageDO.java)

### 支付演示：2 张

参考仓库演示用表，单独列出，不计为正式商城必建业务表。

- `pay_demo_order` — [PayDemoOrderDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/demo/PayDemoOrderDO.java)
- `pay_demo_withdraw` — [PayDemoWithdrawDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/demo/PayDemoWithdrawDO.java)

## 已有对应模型，但不是等价复刻

| 参考表 | 当前表 | 仍须处理 |
| --- | --- | --- |
| trade_after_sale | trade_refund_request | 旧退款表扩展，缺完整申请/审核/商品快照/退款关联 |
| pay_order、pay_refund | trade_payment、trade_payment_refund | 当前简化交易单，业务字段与支付尝试/通知模型不完整 |
| member_point_record、member_experience_record | member_asset_record | 两类流水合并；字段和业务语义须恢复，不算两张真正缺失表 |
| product_property、product_property_value | mall_product_spec、mall_product_spec_value | 用户指定规格命名差异；不要重复创建同一业务表 |
| trade_cart | mall_cart | 服务归属和命名差异，SPU/SKU 关联及字段仍有差异 |
| promotion_coupon_template、promotion_coupon | mall_coupon_template、mall_coupon | 已补完整业务规则快照；部分App查询与注册触发仍在迁移 |

完整 28 组映射和字段差异见 [字段对比](yudao-database-field-comparison.md)。

## 当前 11 张非一一对应表

| 当前表 | 性质 | 处理原则 |
| --- | --- | --- |
| mall_product_sku_spec | 规格快照拆表，参考 product_sku.properties | 随商品模型复刻统一，不能直接删丢规格 |
| member_tag_link | 标签关联拆表，参考 member_user.tag_ids | 存储方式差异，不等于缺标签功能 |
| mall_product_stock_reservation | 当前预占/退货库存台账 | 随参考库存链路一起替换，先保证重复调用和回退不丢库存 |
| trade_inventory_confirmation | 当前库存确认标记 | 随支付后处理和重试机制一起核对 |
| trade_shipment | 当前独立发货事实 | 不能替代快递公司/运费模板等 5 张配送表；此前用户要求独立物流管理 |
| member_session | 旧会员会话 | 与 auth/网关认证约定冲突的初始化残留 |
| member_login_attempt | 旧会员登录限流 | 核对认证边界后移除或归到真实认证实现 |
| member_sms_challenge、member_sms_budget | 旧短信状态 | 与去掉短信的约定冲突 |
| member_social_state、member_social_binding | 旧社交认证状态 | 与去掉微信逻辑的约定冲突 |

## 迁移顺序与验收

先保留参考整体清单，不再把先前裁剪范围视为已经完成的商城复刻。第一阶段优先补基础表和现有表缺失字段，打通正常交易及售后，再逐组迁移扩展功能。

1. 商品完整信息、会员资料、券快照及交易配置。
2. 运费模板/地区计费/包邮/快递公司，以及完整订单主子表、订单/售后日志。
3. 支付应用/渠道/尝试与通知模型，商品售后与支付退款关联；真实渠道适配仍按用户约定后置。
4. 正常交易与失败重试闭环验收，再推进收藏/足迹、内容装修、活动、分销、钱包、客服和统计等缺失功能。

不能仅补 CREATE TABLE 就宣称迁移：每张表必须同时有持久化、业务流程、接口/页面或任务消费者，并验证实际写入、状态变更和查询。支付演示表不作为正式商城验收项。

本轮只完成盘点和文档，没有创建空表，没有执行数据库操作，没有提交代码。
