-- 订单快照增加营销满减赠券模板与数量，支付成功后由商城按其发券。
ALTER TABLE trade_order ADD COLUMN give_coupon_template_counts LONGTEXT NULL COMMENT '下单时赠券模板编号与数量JSON映射';
