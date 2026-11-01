-- 营销满减赠券来源订单，唯一索引保证支付回调重试不重复发券。
ALTER TABLE mall_coupon ADD COLUMN source_order_no VARCHAR(40) NULL;
CREATE UNIQUE INDEX uk_mall_coupon_gift ON mall_coupon(source_order_no, template_id, member_id);
