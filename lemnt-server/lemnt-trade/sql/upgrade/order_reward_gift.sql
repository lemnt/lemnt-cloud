-- 既有环境补列：订单营销赠券快照。
ALTER TABLE trade_order ADD COLUMN give_coupon_template_counts LONGTEXT NULL;
