-- 已有 V5/V6 的 MySQL 库升级；新库无需执行，停止售后写入后核对执行一次。
ALTER TABLE trade_refund_request
 ADD COLUMN order_item_id BIGINT NULL,
 ADD COLUMN quantity INT NULL,
 ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'REFUND_ONLY',
 ADD COLUMN return_carrier_code VARCHAR(40) NULL,
 ADD COLUMN return_tracking_no VARCHAR(100) NULL,
 ADD COLUMN return_shipped_at DATETIME NULL,
 ADD COLUMN return_received_at DATETIME NULL;
ALTER TABLE trade_refund_request DROP CHECK chk_trade_refund_status;
ALTER TABLE trade_refund_request ADD CONSTRAINT chk_trade_refund_status CHECK(status IN ('REQUESTED','REJECTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','RETURN_REJECTED','REFUNDING','REFUNDED'));
ALTER TABLE trade_refund_request ADD CONSTRAINT fk_trade_refund_item
 FOREIGN KEY(order_item_id) REFERENCES trade_order_item(id);
ALTER TABLE trade_refund_request ADD CONSTRAINT chk_trade_refund_item
 CHECK((order_item_id IS NULL AND quantity IS NULL) OR (order_item_id IS NOT NULL AND quantity IS NOT NULL AND quantity>0));
ALTER TABLE trade_refund_request ADD CONSTRAINT chk_trade_refund_type CHECK(type IN ('REFUND_ONLY','RETURN_REFUND'));
