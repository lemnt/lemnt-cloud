-- 在 after_sale_identity_and_log.sql 升级完成后执行一次；执行前备份并核对版本。
-- 仅迁移原订单中已经保存的事实，不回查当前商品资料补写历史快照。
ALTER TABLE trade_order_item CHANGE COLUMN product_id sku_id BIGINT NOT NULL;
ALTER TABLE trade_order_item
    ADD COLUMN spu_id BIGINT NULL,
    ADD COLUMN spu_name VARCHAR(200) NULL,
    ADD COLUMN properties LONGTEXT NULL,
    ADD COLUMN pic_url VARCHAR(1024) NULL;
ALTER TABLE trade_after_sale
    ADD COLUMN sku_id BIGINT NULL,
    ADD COLUMN spu_id BIGINT NULL,
    ADD COLUMN spu_name VARCHAR(200) NULL,
    ADD COLUMN properties LONGTEXT NULL,
    ADD COLUMN pic_url VARCHAR(1024) NULL;
UPDATE trade_after_sale a
JOIN trade_order_item i ON i.id = a.order_item_id AND i.order_id = a.order_id
SET a.sku_id = i.sku_id;
-- 存量 SPU、规格、图片保持 NULL；NULL 与已知无规格的 {} 不同。
