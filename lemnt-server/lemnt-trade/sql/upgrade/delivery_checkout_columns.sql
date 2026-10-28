-- 存量库：停写备份并核对字段未存在后执行。历史收件快照保留NULL，运费为0。
ALTER TABLE trade_order ADD COLUMN address_id BIGINT NULL, ADD COLUMN delivery_type INT NULL,
 ADD COLUMN receiver_name VARCHAR(100) NULL, ADD COLUMN receiver_mobile VARCHAR(32) NULL,
 ADD COLUMN receiver_area_id BIGINT NULL, ADD COLUMN receiver_detail_address VARCHAR(500) NULL,
 ADD COLUMN delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0;
ALTER TABLE trade_order_item ADD COLUMN delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0;
