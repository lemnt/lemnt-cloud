-- 每张赠券使用独立序号；旧索引保证存量每组至多一张，可安全回填为1。
ALTER TABLE mall_coupon ADD COLUMN gift_sequence INT NULL;
UPDATE mall_coupon SET gift_sequence=1 WHERE source_order_no IS NOT NULL;
DROP INDEX uk_mall_coupon_gift ON mall_coupon;
CREATE UNIQUE INDEX uk_mall_coupon_gift ON mall_coupon(source_order_no, template_id, member_id, gift_sequence);
