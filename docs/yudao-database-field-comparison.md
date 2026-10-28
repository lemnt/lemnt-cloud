# 三模块数据库字段与本地芋道对比

日期：2026-10-28。结论：现有代码完成了部分流程收敛，数据库业务模型仍有显著差异，不能称为已复刻芋道商城。

## 目标修正与取证范围

用户最新目标：先复刻商城，保证流程闭环，再按 lemnt 的实际需求调整模块。因此，字段迁移应先保持芋道的业务含义、快照、状态及关联，不能先裁剪再用简化模型替代。真实渠道本轮仍按用户确认留待后续接入；闭环测试必须标明模拟边界。

参考目录 `/Users/wangxx/demo/yudao-cloud/`，不是之前临时克隆目录。该目录没有 .git，不能宣称对应某 Git 提交；证据 JSON 保存各参考 DO 的 SHA-256。本项目读取三个模块当前根目录 V*.sql，合并 CREATE 和 ALTER ADD，排除存量 upgrade 脚本，得到 39 张当前初始化表。

本地芋道公开 MySQL 文件未包含 product/trade/member 等商城业务表。因此本报告以业务 DO 持久化字段为主、测试建表 SQL 为辅；Java Integer 不等于已证实 MySQL int 长度/可空性。未连接实际数据库，不能据此断言线上已有列、索引、默认值或迁移执行状态。

## 主要差异与影响

| 范围 | 本项目现状 | 芋道参考字段/模型 | 影响及处理方向 |
| --- | --- | --- | --- |
| SPU | 只有编码、名称、分类品牌、描述、主图、状态 | keyword、introduction、slider_pic_urls、sort、spec_type、price、market_price、cost_price、stock、delivery_types、delivery_template_id、give_integral、sales_count 等 | 商品编辑、列表、运费和展示能力不完整；按原模型补齐并同步保存逻辑 |
| SKU | 独立 product_code/name/status/variant_key，规格拆快照表 | properties(JSON)、bar_code、market_price、cost_price、weight、volume、sales_count 等 | 不只是改名，规格存储及物流计价数据不同；应先恢复参考的商品快照含义 |
| 评价 | 单 score、content、reply 字段 | order_item_id、用户与商品快照、anonymous、scores/description_scores/benefit_scores、pic_urls、reply_status | 当前无法等价复刻评价管理和订单评价关联 |
| 券模板 | 全局固定有效期、enabled、已发数量 | description、status、take_limit_count、product_scope/values、validity_type、固定/相对有效期、take_count/use_count | 缺少限领配置、商品范围和领券后生效天数；不能以固定一会员一券替代 |
| 会员券 | 仅 template_id/member_id/status/order_no | name、take_type、valid_start/end_time、use_price、scope、折扣规则快照、use_time | 当前查询模板得到规则；后续改模板会影响已发券，与芋道持券快照不同，优先补齐 |
| 满减 | rules JSON 仅 limit/discountPrice | Rule 还含 freeDelivery、point、giveCouponTemplateCounts | 表面主表字段相近，JSON 内业务仍未对齐；需关联运费与支付后赠送 |
| 折扣 | SKU 规则，主表查询时间状态 | 商品表还冗余 activity_name/status/start/end_time | 冗余列及同步更新语义未复刻；不应直接当多余删除 |
| 订单 | total_amount/use_point/give_point/status/remark | 原价、活动优惠、运费、调价、券优惠、积分金额、VIP优惠、支付状态/时间、收货信息、退款汇总等 | 只存最终实付不足以还原价格构成、调价和售后；这是核心结构差距 |
| 订单行 | SKU 编码名称、单价数量、原价合计及实付 | SPU/SKU/图片/规格快照、分项优惠/运费/调价、售后状态、评价状态、积分等 | 当前行实付仅解决部分退款金额，不等于完整订单明细模型 |
| 售后 | trade_refund_request，无主键 id，含旧 active_order_no 和 request_no | trade_after_sale 有 id/no、申请文字/图片、商品快照、审核人时间、支付退款关联、退款时间等 | 仍沿用旧整单退款表扩展，需迁移为完整售后实体和流程 |
| 支付/退款 | 简化渠道号、金额、状态 | app/channel/order 关联、商户单号、到期/成功时间、累计退款、退款失败原因等 | 渠道 SDK 可后置，业务单关联、金额和状态模型仍需先补齐 |
| 物流 | 独立 trade_shipment，一条完整地址文本 | 订单收件区划/详细地址、配送方式/运费模板，快递公司、计费、包邮、自提模型 | 独立物流单是用户先前要求；字段及配置仍须对照参考，不能用单号表冒充完整配送能力 |
| 会员 | 缺注册/登录信息、真实姓名、地区、备注；标签拆关联表 | register_ip/terminal、login_ip/date、name、area_id、mark、tag_ids | 资料管理和登录审计有差距；标签属于存储方式差异，不应误判为没有标签功能 |
| 会员流水 | 合并 asset_record，level_record 仅少量字段 | 积分/经验独立流水；等级流水含 level/discount_percent/user_experience/remark/description | 资产表合并是本项目自定义实现，不能宣称与参考一致；先恢复原业务信息与流水语义 |
| 公共列 | 审计列只在部分表存在，多数无 deleted | 相关 DO 通常继承 BaseDO 的 create_time/update_time/creator/updater/deleted | 审计命名应遵循 system 映射，软删除影响唯一键和查询，不能仅批量加列 |

## 仍残留、且与已确认要求冲突的初始化表

以下 6 表仍在 member 初始化 SQL 中。全仓 Java/XML 搜索未发现这些表名的直接引用；这证明脚本残留，不等于已查询线上数据库证明为空。

- member_session：id/member_id/access_hash/refresh_hash/expires_at/refresh_expires_at。与统一 auth/网关认证的约定冲突。
- member_login_attempt：login_hash/failures/window_start。会员库残留登录保护表，应核对并移至实际认证边界或移除。
- member_sms_challenge：mobile/scene/code_hash/expires_at/attempts/last_sent_at/sent_date/daily_count。
- member_sms_budget：budget_day/requests。
- member_social_state：state_hash/provider/purpose/member_id/expires_at。
- member_social_binding：provider/subject/member_id。

这 6 张表与用户此前去掉短信/微信及会员会话认证的要求冲突，后续应清理初始化与存量归档方案。

## 不能只按同名列对齐

1. 芋道金额多为 Integer、单位分；当前 DECIMAL(19,2)、单位元。total_amount 对应最终实付，不能直接映射 total_price（原价）。迁移必须明确单位、取整、上下限及接口展示。
2. 当前多用字符串状态和规则类型，芋道多用整数枚举；同名 status 不代表同一状态机。
3. 本地芋道折扣字段注释/VO 示例写“80 表示八折”，交易计算器却传 discountPercent/100.0 给 MoneyUtils。已核对 MoneyUtils 继续除以 100，该调用链实际按万分比计算（8000 对应八折），与上述注释冲突。后续迁移需保留可执行用例，不能按注释把 8000 直接改为 80。
4. rules、properties、product_scope_values 等 JSON 字段必须对比内部结构，不能列名一致就算完成。
5. 本项目 member_tag_link、mall_product_sku_spec 代表拆表存储；参考 tag_ids/properties 是单列 JSON，属于模型差异。
6. stock_reservation、inventory_confirmation 是当前同步下单与跨服务补偿实现；没有同名芋道表不代表可直接删，须随完整订单链路一起替换和验收。

## 下一阶段执行顺序与验收

1. 先建立逐表字段映射：保留参考业务信息，标明单位、枚举、JSON、关联及快照；同时还原购物车、券、价格计算所需字段。
2. 商品与会员基础数据 → 活动/领券 → 结算及订单价格构成 → 支付状态及权益 → 发货收货 → 商品售后、退款、库存和权益返还。
3. 每一步同时调整 SQL、DO、Mapper/Gateway、领域/应用逻辑、接口和页面，禁止只加列不使用或只改接口不落库。
4. 闭环须覆盖正常交易、库存不足、券不可用、重复提交、支付失败/超时关单、部分退款、退货退款、失败重试和重复回调。构建通过仅证明编译及本地测试，不证明商城功能已经复刻。
5. 闭环验收后再执行 lemnt 的模块边界、表合并和功能裁剪；目前已有自定义差异应逐项回到基线，而不是默认保留。

本文只做对比和目标修正，未修改业务代码或数据库。

## 逐表字段附录

下列列出 28 组对应关系的全部直接业务字段；公共 BaseDO 审计列另列说明。顺序不代表一一匹配；金额和重命名关系以上文语义为准。

### `mall_product_category`

本项目：`id`, `parent_id`, `name`, `pic_url`, `sort`, `status`。

芋道 `product_category`：`id`, `parent_id`, `name`, `pic_url`, `sort`, `status`。

来源：[ProductCategoryDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/category/ProductCategoryDO.java)；继承 `BaseDO`。

### `mall_product_brand`

本项目：`id`, `name`, `pic_url`, `description`, `sort`, `status`。

芋道 `product_brand`：`id`, `name`, `pic_url`, `sort`, `description`, `status`。

来源：[ProductBrandDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/brand/ProductBrandDO.java)；继承 `BaseDO`。

### `mall_product_spu`

本项目：`id`, `code`, `name`, `category_id`, `brand_id`, `description`, `image`, `status`。

芋道 `product_spu`：`id`, `name`, `keyword`, `introduction`, `description`, `category_id`, `brand_id`, `pic_url`, `slider_pic_urls`, `sort`, `status`, `spec_type`, `price`, `market_price`, `cost_price`, `stock`, `delivery_types`, `delivery_template_id`, `give_integral`, `sub_commission_type`, `sales_count`, `virtual_sales_count`, `browse_count`。

来源：[ProductSpuDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/spu/ProductSpuDO.java)；继承 `BaseDO`。

### `mall_product_sku`

本项目：`id`, `product_code`, `name`, `description`, `main_image`, `price`, `stock`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `spu_id`, `variant_key`。

芋道 `product_sku`：`id`, `spu_id`, `properties`, `price`, `market_price`, `cost_price`, `bar_code`, `pic_url`, `stock`, `weight`, `volume`, `first_brokerage_price`, `second_brokerage_price`, `sales_count`。

来源：[ProductSkuDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/sku/ProductSkuDO.java)；继承 `BaseDO`。

### `mall_product_spec`

本项目：`id`, `name`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `product_property`：`id`, `name`, `remark`。

来源：[ProductPropertyDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/property/ProductPropertyDO.java)；继承 `BaseDO`。

### `mall_product_spec_value`

本项目：`id`, `spec_id`, `name`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `product_property_value`：`id`, `property_id`, `name`, `remark`。

来源：[ProductPropertyValueDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/property/ProductPropertyValueDO.java)；继承 `BaseDO`。

### `mall_product_comment`

本项目：`id`, `spu_id`, `sku_id`, `order_no`, `member_id`, `score`, `content`, `visible`, `reply_content`, `reply_user_id`, `reply_time`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `product_comment`：`id`, `user_id`, `user_nickname`, `user_avatar`, `anonymous`, `order_id`, `order_item_id`, `spu_id`, `spu_name`, `sku_id`, `sku_pic_url`, `sku_properties`, `visible`, `scores`, `description_scores`, `benefit_scores`, `content`, `pic_urls`, `reply_status`, `reply_user_id`, `reply_content`, `reply_time`。

来源：[ProductCommentDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-product-server/src/main/java/cn/iocoder/yudao/module/product/dal/dataobject/comment/ProductCommentDO.java)；继承 `BaseDO`。

### `mall_cart`

本项目：`id`, `member_id`, `product_id`, `quantity`, `selected`, `version`。

芋道 `trade_cart`：`id`, `user_id`, `spu_id`, `sku_id`, `count`, `selected`。

来源：[CartDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/cart/CartDO.java)；继承 `BaseDO`。

### `mall_coupon_template`

本项目：`id`, `code`, `name`, `kind`, `threshold`, `fixed_amount`, `discount_percent`, `discount_limit`, `total_count`, `issued_count`, `valid_from`, `valid_until`, `enabled`, `take_type`。

芋道 `promotion_coupon_template`：`id`, `name`, `description`, `status`, `total_count`, `take_limit_count`, `take_type`, `use_price`, `product_scope`, `product_scope_values`, `validity_type`, `valid_start_time`, `valid_end_time`, `fixed_start_term`, `fixed_end_term`, `discount_type`, `discount_percent`, `discount_price`, `discount_limit_price`, `take_count`, `use_count`。

来源：[CouponTemplateDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/coupon/CouponTemplateDO.java)；继承 `BaseDO`。

### `mall_coupon`

本项目：`id`, `template_id`, `member_id`, `status`, `order_no`。

芋道 `promotion_coupon`：`id`, `template_id`, `name`, `status`, `user_id`, `take_type`, `use_price`, `valid_start_time`, `valid_end_time`, `product_scope`, `product_scope_values`, `discount_type`, `discount_percent`, `discount_price`, `discount_limit_price`, `use_order_id`, `use_time`。

来源：[CouponDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/coupon/CouponDO.java)；继承 `BaseDO`。

### `mall_promotion_reward_activity`

本项目：`id`, `name`, `status`, `start_time`, `end_time`, `remark`, `condition_type`, `product_scope`, `product_scope_values_json`, `rules_json`。

芋道 `promotion_reward_activity`：`id`, `name`, `status`, `start_time`, `end_time`, `remark`, `condition_type`, `product_scope`, `product_scope_values`, `rules`。

来源：[RewardActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/reward/RewardActivityDO.java)；继承 `BaseDO`。

### `mall_promotion_discount_activity`

本项目：`id`, `name`, `status`, `start_time`, `end_time`, `remark`。

芋道 `promotion_discount_activity`：`id`, `name`, `status`, `start_time`, `end_time`, `remark`。

来源：[DiscountActivityDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/discount/DiscountActivityDO.java)；继承 `BaseDO`。

### `mall_promotion_discount_product`

本项目：`id`, `activity_id`, `spu_id`, `sku_id`, `discount_type`, `discount_percent`, `discount_price`。

芋道 `promotion_discount_product`：`id`, `activity_id`, `spu_id`, `sku_id`, `discount_type`, `discount_percent`, `discount_price`, `activity_name`, `activity_status`, `activity_start_time`, `activity_end_time`。

来源：[DiscountProductDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-promotion-server/src/main/java/cn/iocoder/yudao/module/promotion/dal/dataobject/discount/DiscountProductDO.java)；继承 `BaseDO`。

### `trade_order`

本项目：`id`, `order_no`, `member_id`, `total_amount`, `use_point`, `give_point`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `trade_order`：`id`, `no`, `type`, `terminal`, `user_id`, `user_ip`, `user_remark`, `status`, `product_count`, `finish_time`, `cancel_time`, `cancel_type`, `remark`, `comment_status`, `brokerage_user_id`, `pay_order_id`, `pay_status`, `pay_time`, `pay_channel_code`, `total_price`, `discount_price`, `delivery_price`, `adjust_price`, `pay_price`, `delivery_type`, `logistics_id`, `logistics_no`, `delivery_time`, `receive_time`, `receiver_name`, `receiver_mobile`, `receiver_area_id`, `receiver_detail_address`, `pick_up_store_id`, `pick_up_verify_code`, `refund_status`, `refund_price`, `coupon_id`, `coupon_price`, `use_point`, `point_price`, `give_point`, `refund_point`, `vip_price`, `give_coupon_template_counts`, `give_coupon_ids`, `seckill_activity_id`, `bargain_activity_id`, `bargain_record_id`, `combination_activity_id`, `combination_head_id`, `combination_record_id`, `point_activity_id`。

来源：[TradeOrderDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/order/TradeOrderDO.java)；继承 `BaseDO`。

### `trade_order_item`

本项目：`id`, `order_id`, `product_id`, `product_code`, `product_name`, `unit_price`, `quantity`, `line_amount`, `pay_amount`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `trade_order_item`：`id`, `user_id`, `order_id`, `cart_id`, `spu_id`, `spu_name`, `sku_id`, `properties`, `pic_url`, `count`, `comment_status`, `price`, `discount_price`, `delivery_price`, `adjust_price`, `pay_price`, `coupon_price`, `point_price`, `use_point`, `give_point`, `vip_price`, `after_sale_id`, `after_sale_status`。

来源：[TradeOrderItemDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/order/TradeOrderItemDO.java)；继承 `BaseDO`。

### `trade_refund_request`

本项目：`refund_no`, `order_no`, `request_no`, `member_id`, `payment_no`, `amount`, `reason`, `status`, `active_order_no`, `decision_reason`, `order_item_id`, `quantity`, `type`, `return_carrier_code`, `return_tracking_no`, `return_shipped_at`, `return_received_at`, `requested_at`, `decided_at`。

芋道 `trade_after_sale`：`id`, `no`, `status`, `way`, `type`, `user_id`, `apply_reason`, `apply_description`, `apply_pic_urls`, `order_id`, `order_no`, `order_item_id`, `spu_id`, `spu_name`, `sku_id`, `properties`, `pic_url`, `count`, `audit_time`, `audit_user_id`, `audit_reason`, `refund_price`, `pay_refund_id`, `refund_time`, `logistics_id`, `logistics_no`, `delivery_time`, `receive_time`, `receive_reason`。

来源：[AfterSaleDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-mall/yudao-module-trade-server/src/main/java/cn/iocoder/yudao/module/trade/dal/dataobject/aftersale/AfterSaleDO.java)；继承 `BaseDO`。

### `trade_payment`

本项目：`id`, `payment_no`, `order_no`, `channel`, `channel_trade_no`, `amount`, `status`, `notify_id`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `pay_order`：`id`, `app_id`, `channel_id`, `channel_code`, `user_id`, `user_type`, `merchant_order_id`, `subject`, `body`, `notify_url`, `price`, `channel_fee_rate`, `channel_fee_price`, `status`, `user_ip`, `expire_time`, `success_time`, `extension_id`, `no`, `refund_price`, `channel_user_id`, `channel_order_no`。

来源：[PayOrderDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/order/PayOrderDO.java)；继承 `BaseDO`。

### `trade_payment_refund`

本项目：`refund_no`, `after_sale_no`, `payment_no`, `order_no`, `channel`, `amount`, `status`, `channel_refund_no`, `create_by`, `create_time`, `update_by`, `update_time`。

芋道 `pay_refund`：`id`, `no`, `app_id`, `channel_id`, `channel_code`, `order_id`, `order_no`, `user_id`, `user_type`, `merchant_order_id`, `merchant_refund_id`, `notify_url`, `status`, `pay_price`, `refund_price`, `reason`, `user_ip`, `channel_order_no`, `channel_refund_no`, `success_time`, `channel_error_code`, `channel_error_msg`, `channel_notify_data`。

来源：[PayRefundDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-pay/yudao-module-pay-server/src/main/java/cn/iocoder/yudao/module/pay/dal/dataobject/refund/PayRefundDO.java)；继承 `BaseDO`。

### `member_user`

本项目：`id`, `nickname`, `mobile`, `password`, `email`, `avatar`, `status`, `points`, `create_by`, `create_time`, `update_by`, `update_time`, `experience`, `level_id`, `group_id`, `sex`, `birthday`。

芋道 `member_user`：`id`, `mobile`, `email`, `password`, `status`, `register_ip`, `register_terminal`, `login_ip`, `login_date`, `nickname`, `avatar`, `name`, `sex`, `birthday`, `area_id`, `mark`, `point`, `tag_ids`, `level_id`, `experience`, `group_id`。

来源：[MemberUserDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/user/MemberUserDO.java)；继承 `TenantBaseDO`。

### `member_address`

本项目：`id`, `member_id`, `name`, `mobile`, `area_id`, `detail_address`, `default_status`。

芋道 `member_address`：`id`, `user_id`, `name`, `mobile`, `area_id`, `detail_address`, `default_status`。

来源：[MemberAddressDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/address/MemberAddressDO.java)；继承 `BaseDO`。

### `member_level`

本项目：`id`, `name`, `level`, `experience`, `discount_percent`, `icon`, `background_url`, `status`。

芋道 `member_level`：`id`, `name`, `level`, `experience`, `discount_percent`, `icon`, `background_url`, `status`。

来源：[MemberLevelDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/level/MemberLevelDO.java)；继承 `BaseDO`。

### `member_group`

本项目：`id`, `name`, `remark`, `status`。

芋道 `member_group`：`id`, `name`, `remark`, `status`。

来源：[MemberGroupDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/group/MemberGroupDO.java)；继承 `BaseDO`。

### `member_tag`

本项目：`id`, `name`, `remark`。

芋道 `member_tag`：`id`, `name`。

来源：[MemberTagDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/tag/MemberTagDO.java)；继承 `BaseDO`。

### `member_asset_record`

本项目：`id`, `member_id`, `asset_type`, `delta`, `balance`, `biz_type`, `biz_no`, `reason`, `create_time`。

芋道 `member_point_record`：`id`, `user_id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`。

来源：[MemberPointRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/point/MemberPointRecordDO.java)；继承 `BaseDO`。

芋道 `member_experience_record`：`id`, `user_id`, `biz_type`, `biz_id`, `title`, `description`, `experience`, `total_experience`。

来源：[MemberExperienceRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/level/MemberExperienceRecordDO.java)；继承 `BaseDO`。

### `member_level_record`

本项目：`id`, `member_id`, `level_id`, `experience`, `reason`, `create_time`。

芋道 `member_level_record`：`id`, `user_id`, `level_id`, `level`, `discount_percent`, `experience`, `user_experience`, `remark`, `description`。

来源：[MemberLevelRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/level/MemberLevelRecordDO.java)；继承 `BaseDO`。

### `member_sign_config`

本项目：`signin_day`, `point`, `experience`, `status`。

芋道 `member_sign_in_config`：`id`, `day`, `point`, `experience`, `status`。

来源：[MemberSignInConfigDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/signin/MemberSignInConfigDO.java)；继承 `BaseDO`。

### `member_sign_record`

本项目：`id`, `member_id`, `sign_date`, `signin_day`, `point`, `experience`。

芋道 `member_sign_in_record`：`id`, `user_id`, `day`, `point`, `experience`。

来源：[MemberSignInRecordDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/signin/MemberSignInRecordDO.java)；继承 `BaseDO`。

### `member_config`

本项目：`id`, `point_trade_deduct_enable`, `point_trade_deduct_unit_price`, `point_trade_deduct_max_price`, `point_trade_give_point`。

芋道 `member_config`：`id`, `point_trade_deduct_enable`, `point_trade_deduct_unit_price`, `point_trade_deduct_max_price`, `point_trade_give_point`。

来源：[MemberConfigDO.java](/Users/wangxx/demo/yudao-cloud/yudao-module-member/yudao-module-member-server/src/main/java/cn/iocoder/yudao/module/member/dal/dataobject/config/MemberConfigDO.java)；继承 `BaseDO`。

