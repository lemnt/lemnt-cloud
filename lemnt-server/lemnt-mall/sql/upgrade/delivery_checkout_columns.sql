-- 存量库：停写备份并核对字段未存在后在演练库执行。旧商品不自动分配运费模板。
ALTER TABLE mall_product_spu ADD COLUMN delivery_types LONGTEXT NULL,
 ADD COLUMN delivery_template_id BIGINT NULL;
ALTER TABLE mall_product_sku ADD COLUMN weight DECIMAL(18,6) NULL, ADD COLUMN volume DECIMAL(18,6) NULL;
