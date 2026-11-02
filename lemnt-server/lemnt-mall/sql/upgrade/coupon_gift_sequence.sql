-- 已执行V28的旧库执行一次，与V29二选一；先迁移后发布，执行前备份。
ALTER TABLE mall_coupon ADD COLUMN gift_sequence INT NULL;
UPDATE mall_coupon SET gift_sequence=1 WHERE source_order_no IS NOT NULL;
DROP INDEX uk_mall_coupon_gift ON mall_coupon;
CREATE UNIQUE INDEX uk_mall_coupon_gift ON mall_coupon(source_order_no, template_id, member_id, gift_sequence);
