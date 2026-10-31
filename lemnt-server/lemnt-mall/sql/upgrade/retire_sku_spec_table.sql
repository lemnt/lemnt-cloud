-- 仅在sku_properties_json.sql已复制、逐SKU核对通过、备份可恢复且旧写者停写后单独执行。
-- 本文件不会自动判定上述前置条件；不要与复制脚本不经审核串行执行。
DROP TABLE mall_product_sku_spec;
