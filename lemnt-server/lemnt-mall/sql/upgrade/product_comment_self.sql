-- 仅旧库执行；自评不关联订单。未执行真实数据库。
ALTER TABLE mall_product_comment MODIFY COLUMN order_no VARCHAR(40) NULL;
