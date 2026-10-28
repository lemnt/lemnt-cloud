-- 人工核对版本后执行一次；执行前备份，停止旧版SKU规格写入者。
ALTER TABLE mall_product_sku_spec
    ADD COLUMN property_id BIGINT NULL,
    ADD COLUMN value_id BIGINT NULL,
    ADD INDEX idx_mall_sku_spec_identity(property_id,value_id);
-- 不按同名自动关联历史字典，旧行保持NULL，后台编辑时人工选取真实编号。
