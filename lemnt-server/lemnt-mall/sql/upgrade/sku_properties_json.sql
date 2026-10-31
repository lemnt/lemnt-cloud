-- MySQL 8：执行前备份、停旧写者，并先完成product_sku_spec_identity.sql。
-- 若旧库没有property_id/value_id列，先核对前序版本；本脚本不会按名称猜测ID。
ALTER TABLE mall_product_sku ADD COLUMN properties LONGTEXT NULL;
UPDATE mall_product_sku s
LEFT JOIN (
    SELECT product_id, JSON_ARRAYAGG(JSON_OBJECT(
        'propertyId',property_id,'propertyName',property_name,
        'valueId',value_id,'valueName',property_value)) AS payload
    FROM mall_product_sku_spec GROUP BY product_id
) p ON p.product_id=s.id
SET s.properties=COALESCE(p.payload,JSON_ARRAY());
-- 旧编号必须成对，部分缺号不可当作已完整迁移，结果必须为0。
SELECT COUNT(*) AS partial_identifiers FROM mall_product_sku_spec
WHERE (property_id IS NULL AND value_id IS NOT NULL) OR (property_id IS NOT NULL AND value_id IS NULL);
-- 以下核对必须全部通过；不要在核对前执行retire_sku_spec_table.sql。
SELECT COUNT(*) AS invalid_json FROM mall_product_sku WHERE properties IS NULL OR NOT JSON_VALID(properties);
SELECT (SELECT COUNT(*) FROM mall_product_sku_spec) AS old_rows,
       (SELECT COALESCE(SUM(JSON_LENGTH(properties)),0) FROM mall_product_sku) AS copied_properties;
-- 每个原始规格必须在同一个SKU的JSON内完整存在，结果必须为0。
SELECT COUNT(*) AS unmatched_rows FROM mall_product_sku_spec p
LEFT JOIN mall_product_sku s ON s.id=p.product_id
WHERE s.id IS NULL OR NOT JSON_CONTAINS(s.properties,JSON_OBJECT(
    'propertyId',p.property_id,'propertyName',p.property_name,
    'valueId',p.value_id,'valueName',p.property_value));
-- 本脚本只复制，不删除旧表。部署新服务并复核后再单独审核删除脚本。
