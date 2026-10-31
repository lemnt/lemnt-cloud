-- 已有商城库升级脚本，只执行一次；新库使用V2。无历史关键字/简介/轮播图时保持NULL。
ALTER TABLE mall_product_spu ADD COLUMN keyword VARCHAR(255) NULL;
ALTER TABLE mall_product_spu ADD COLUMN introduction VARCHAR(128) NULL;
ALTER TABLE mall_product_spu ADD COLUMN slider_pic_urls LONGTEXT NULL;
ALTER TABLE mall_product_spu ADD COLUMN sort INT NOT NULL DEFAULT 0;
