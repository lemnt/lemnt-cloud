CREATE TABLE IF NOT EXISTS mall_product_sku (
 id BIGINT NOT NULL AUTO_INCREMENT,
 product_code VARCHAR(64) NOT NULL,
 name VARCHAR(200) NOT NULL,
 description TEXT NULL,
 main_image VARCHAR(500) NULL,
 weight DECIMAL(18,6) NULL,volume DECIMAL(18,6) NULL,price DECIMAL(19,2) NOT NULL,
 stock INT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF',
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_mall_product_code(product_code), KEY idx_mall_product_status(status),
 CONSTRAINT chk_mall_product_price CHECK(price>0), CONSTRAINT chk_mall_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品';

CREATE TABLE IF NOT EXISTS mall_product_stock_reservation (
 id BIGINT NOT NULL AUTO_INCREMENT,
 reservation_no VARCHAR(40) NOT NULL,
 source_no VARCHAR(40) NULL,
 product_id BIGINT NOT NULL,
 quantity INT NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 UNIQUE KEY uk_mall_stock_reservation(reservation_no, product_id),
 KEY idx_mall_stock_reservation_status(status, update_time),
 CONSTRAINT chk_mall_stock_reservation_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占幂等台账';