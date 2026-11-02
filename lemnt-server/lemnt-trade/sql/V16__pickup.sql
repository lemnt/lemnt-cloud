-- 自提门店与订单核销字段。
CREATE TABLE IF NOT EXISTS trade_delivery_pick_up_store (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 introduction VARCHAR(500) NULL COMMENT '简介',
 phone VARCHAR(32) NOT NULL COMMENT '联系电话',
 area_id BIGINT NOT NULL COMMENT '行政区划编号',
 detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
 logo VARCHAR(500) NULL COMMENT '图标地址',
 opening_time VARCHAR(8) NOT NULL COMMENT '门店开门时间，HH:mm:ss；与关门相同表示全天营业',
 closing_time VARCHAR(8) NOT NULL COMMENT '门店关门时间，HH:mm:ss；早于开门表示跨夜',
 latitude DECIMAL(10,6) NULL COMMENT '门店纬度',
 longitude DECIMAL(10,6) NULL COMMENT '门店经度',
 verify_user_ids LONGTEXT NOT NULL COMMENT '允许核销的管理员编号JSON数组',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 KEY idx_pick_up_store_status(status),
 CONSTRAINT chk_pick_up_store_status CHECK(status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '自提门店及核销店员';

ALTER TABLE trade_order ADD COLUMN pick_up_store_id BIGINT NULL COMMENT '自提门店编号';
ALTER TABLE trade_order ADD COLUMN pick_up_verify_code VARCHAR(8) NULL COMMENT '8位自提核销码';
ALTER TABLE trade_order ADD UNIQUE KEY uk_trade_order_pick_up_code(pick_up_verify_code);
