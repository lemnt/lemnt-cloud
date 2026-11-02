-- 已有环境执行一次；先备份并停止旧写者。只改列名，旧JSON在读取时规范为spec字段。
ALTER TABLE mall_product_sku RENAME COLUMN properties TO specs;
