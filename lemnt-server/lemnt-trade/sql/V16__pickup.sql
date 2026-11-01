-- 自提门店与订单核销字段。
CREATE TABLE IF NOT EXISTS trade_delivery_pick_up_store (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 introduction VARCHAR(500) NULL,
 phone VARCHAR(32) NOT NULL,
 area_id BIGINT NOT NULL,
 detail_address VARCHAR(255) NOT NULL,
 logo VARCHAR(500) NULL,
 opening_time VARCHAR(8) NOT NULL,
 closing_time VARCHAR(8) NOT NULL,
 latitude DECIMAL(10,6) NULL,
 longitude DECIMAL(10,6) NULL,
 verify_user_ids LONGTEXT NOT NULL,
 status INT NOT NULL DEFAULT 0,
 KEY idx_pick_up_store_status(status),
 CONSTRAINT chk_pick_up_store_status CHECK(status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE trade_order ADD COLUMN pick_up_store_id BIGINT NULL;
ALTER TABLE trade_order ADD COLUMN pick_up_verify_code VARCHAR(8) NULL;
ALTER TABLE trade_order ADD UNIQUE KEY uk_trade_order_pick_up_code(pick_up_verify_code);
