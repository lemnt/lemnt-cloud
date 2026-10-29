-- 存量MySQL专用；分服务升级，不跨库猜测回填订单项ID。先备份并核对列/索引。
ALTER TABLE mall_product_comment ADD COLUMN order_item_id BIGINT NULL;
ALTER TABLE mall_product_comment ADD UNIQUE KEY uk_mall_comment_order_item(order_item_id);
ALTER TABLE mall_product_comment DROP INDEX uk_mall_comment_order_sku;
