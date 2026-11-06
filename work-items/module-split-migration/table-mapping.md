# 数据库表归属与改名预览

保留原主键、业务编号、金额、状态、历史NULL和审计字段；此文件不是可执行迁移SQL。每服务只交付一个 CREATE/必要基础数据 INSERT 初始化文件。system 不调整。

| 源库 | 原表 | 目标库 | 新表 | 源SQL行 |
|---|---|---|---|---|
| `lemnt_mall` | `mall_product_stock_reservation` | `lemnt_product` | `product_stock_reservation` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L4` |
| `lemnt_mall` | `mall_product_category` | `lemnt_product` | `product_category` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L21` |
| `lemnt_mall` | `mall_product_brand` | `lemnt_product` | `product_brand` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L33` |
| `lemnt_mall` | `mall_cart` | `lemnt_promotion` | `promotion_cart` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L43` |
| `lemnt_mall` | `mall_coupon_template` | `lemnt_promotion` | `promotion_coupon_template` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L54` |
| `lemnt_mall` | `mall_promotion_reward_activity` | `lemnt_promotion` | `promotion_reward_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L89` |
| `lemnt_mall` | `mall_promotion_discount_activity` | `lemnt_promotion` | `promotion_discount_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L106` |
| `lemnt_mall` | `mall_product_spec` | `lemnt_product` | `product_spec` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L117` |
| `lemnt_mall` | `mall_product_favorite` | `lemnt_product` | `product_favorite` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L128` |
| `lemnt_mall` | `mall_product_browse_history` | `lemnt_product` | `product_browse_history` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L142` |
| `lemnt_mall` | `mall_promotion_seckill_config` | `lemnt_promotion` | `promotion_seckill_config` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L157` |
| `lemnt_mall` | `mall_promotion_seckill_activity` | `lemnt_promotion` | `promotion_seckill_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L168` |
| `lemnt_mall` | `mall_promotion_combination_activity` | `lemnt_promotion` | `promotion_combination_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L187` |
| `lemnt_mall` | `mall_promotion_combination_record` | `lemnt_promotion` | `promotion_combination_record` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L205` |
| `lemnt_mall` | `mall_promotion_bargain_activity` | `lemnt_promotion` | `promotion_bargain_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L233` |
| `lemnt_mall` | `mall_promotion_point_activity` | `lemnt_promotion` | `promotion_point_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L257` |
| `lemnt_mall` | `mall_promotion_order` | `lemnt_order` | `order_activity` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L269` |
| `lemnt_mall` | `mall_promotion_order_refund` | `lemnt_order` | `order_activity_refund` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L297` |
| `lemnt_mall` | `mall_promotion_banner` | `lemnt_promotion` | `promotion_banner` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L308` |
| `lemnt_mall` | `mall_promotion_kefu_conversation` | `lemnt_promotion` | `promotion_kefu_conversation` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L324` |
| `lemnt_mall` | `mall_promotion_kefu_message` | `lemnt_promotion` | `promotion_kefu_message` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L337` |
| `lemnt_mall` | `mall_promotion_diy_template` | `lemnt_promotion` | `promotion_diy_template` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L352` |
| `lemnt_mall` | `mall_promotion_diy_page` | `lemnt_promotion` | `promotion_diy_page` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L364` |
| `lemnt_mall` | `mall_product_spu` | `lemnt_product` | `product_spu` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L374` |
| `lemnt_mall` | `mall_coupon` | `lemnt_promotion` | `promotion_coupon` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L396` |
| `lemnt_mall` | `mall_promotion_discount_product` | `lemnt_promotion` | `promotion_discount_product` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L431` |
| `lemnt_mall` | `mall_product_spec_value` | `lemnt_product` | `product_spec_value` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L445` |
| `lemnt_mall` | `mall_promotion_seckill_product` | `lemnt_promotion` | `promotion_seckill_product` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L458` |
| `lemnt_mall` | `mall_promotion_combination_product` | `lemnt_promotion` | `promotion_combination_product` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L476` |
| `lemnt_mall` | `mall_promotion_bargain_record` | `lemnt_promotion` | `promotion_bargain_record` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L490` |
| `lemnt_mall` | `mall_promotion_point_product` | `lemnt_promotion` | `promotion_point_product` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L507` |
| `lemnt_mall` | `mall_promotion_order_item` | `lemnt_order` | `order_activity_item` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L525` |
| `lemnt_mall` | `mall_product_sku` | `lemnt_product` | `product_sku` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L542` |
| `lemnt_mall` | `mall_promotion_bargain_help` | `lemnt_promotion` | `promotion_bargain_help` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L569` |
| `lemnt_mall` | `mall_product_comment` | `lemnt_product` | `product_comment` | `lemnt-server/lemnt-mall/sql/lemnt_mall.sql:L581` |
| `lemnt_trade` | `trade_order` | `lemnt_order` | `order_info` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L4` |
| `lemnt_trade` | `trade_payment` | `lemnt_pay` | `pay_payment` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L49` |
| `lemnt_trade` | `trade_delivery_express` | `lemnt_order` | `order_delivery_express` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L72` |
| `lemnt_trade` | `trade_delivery_express_template` | `lemnt_order` | `order_delivery_express_template` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L88` |
| `lemnt_trade` | `trade_pay_app` | `lemnt_pay` | `pay_app` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L102` |
| `lemnt_trade` | `trade_pay_notify_task` | `lemnt_pay` | `pay_notify_task` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L119` |
| `lemnt_trade` | `trade_member_wallet` | `lemnt_pay` | `pay_wallet` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L141` |
| `lemnt_trade` | `trade_pay_recharge_package` | `lemnt_pay` | `pay_recharge_package` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L160` |
| `lemnt_trade` | `trade_brokerage_user` | `lemnt_order` | `order_brokerage_user` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L177` |
| `lemnt_trade` | `trade_brokerage_record` | `lemnt_order` | `order_brokerage_record` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L189` |
| `lemnt_trade` | `trade_brokerage_withdraw` | `lemnt_order` | `order_brokerage_withdraw` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L210` |
| `lemnt_trade` | `trade_delivery_pick_up_store` | `lemnt_order` | `order_delivery_pick_up_store` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L232` |
| `lemnt_trade` | `trade_order_item` | `lemnt_order` | `order_item` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L250` |
| `lemnt_trade` | `trade_inventory_confirmation` | `lemnt_order` | `order_inventory_confirmation` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L289` |
| `lemnt_trade` | `trade_shipment` | `lemnt_order` | `order_shipment` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L295` |
| `lemnt_trade` | `trade_delivery_express_template_charge` | `lemnt_order` | `order_delivery_express_template_charge` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L309` |
| `lemnt_trade` | `trade_delivery_express_template_free` | `lemnt_order` | `order_delivery_express_template_free` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L328` |
| `lemnt_trade` | `trade_order_log` | `lemnt_order` | `order_log` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L344` |
| `lemnt_trade` | `trade_pay_channel` | `lemnt_pay` | `pay_channel` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L362` |
| `lemnt_trade` | `trade_pay_notify_log` | `lemnt_pay` | `pay_notify_log` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L382` |
| `lemnt_trade` | `trade_member_wallet_transaction` | `lemnt_pay` | `pay_wallet_transaction` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L397` |
| `lemnt_trade` | `trade_pay_wallet_recharge` | `lemnt_pay` | `pay_wallet_recharge` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L417` |
| `lemnt_trade` | `trade_member_wallet_transfer` | `lemnt_pay` | `pay_wallet_transfer` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L444` |
| `lemnt_trade` | `trade_order_promotion` | `lemnt_order` | `order_promotion` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L470` |
| `lemnt_trade` | `trade_after_sale` | `lemnt_order` | `order_after_sale` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L497` |
| `lemnt_trade` | `trade_order_item_promotion` | `lemnt_order` | `order_item_promotion` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L555` |
| `lemnt_trade` | `trade_payment_refund` | `lemnt_pay` | `pay_payment_refund` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L578` |
| `lemnt_trade` | `trade_after_sale_log` | `lemnt_order` | `order_after_sale_log` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L601` |
| `lemnt_trade` | `trade_order_promotion_refund` | `lemnt_order` | `order_promotion_refund` | `lemnt-server/lemnt-trade/sql/lemnt_trade.sql:L619` |

## 跨库外键

| 原约束 | 原表 | 目标表 | 处理 |
|---|---|---|---|
| `fk_trade_refund_payment` | `order_after_sale` | `pay_payment` | 删除跨库物理约束，保留字段并由RPC校验 |
| `fk_payment_refund_after_sale` | `pay_payment_refund` | `order_after_sale` | 删除跨库物理约束，保留字段并由RPC校验 |
| `fk_payment_refund_order` | `pay_payment_refund` | `order_info` | 删除跨库物理约束，保留字段并由RPC校验 |
