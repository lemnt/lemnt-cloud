-- Additive migration. Back up and test on MySQL before production use.
-- Existing mall_product IDs remain the SKU IDs referenced by orders/reservations.
CREATE TABLE mall_category (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 parent_id BIGINT NULL,
 name VARCHAR(100) NOT NULL,
 pic_url VARCHAR(500) NULL,
 sort INT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
 CONSTRAINT fk_mall_category_parent FOREIGN KEY(parent_id) REFERENCES mall_category(id),
 CONSTRAINT chk_mall_category_status CHECK(status IN ('ENABLED','DISABLED')),
 KEY idx_mall_category_parent(parent_id,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_brand (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 pic_url VARCHAR(500) NULL,
 description VARCHAR(2000) NULL,
 sort INT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
 CONSTRAINT chk_mall_brand_status CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_spu (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 code VARCHAR(64) NOT NULL,
 name VARCHAR(200) NOT NULL,
 category_id BIGINT NOT NULL,
 brand_id BIGINT NOT NULL,
 description TEXT NULL,
 image VARCHAR(500) NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF',
 UNIQUE KEY uk_mall_spu_code(code),
 KEY idx_mall_spu_status(status,id),
 CONSTRAINT fk_mall_spu_category FOREIGN KEY(category_id) REFERENCES mall_category(id),
 CONSTRAINT fk_mall_spu_brand FOREIGN KEY(brand_id) REFERENCES mall_brand(id),
 CONSTRAINT chk_mall_spu_status CHECK(status IN ('ON_SHELF','OFF_SHELF'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE mall_product ADD COLUMN spu_id BIGINT NULL;
ALTER TABLE mall_product ADD COLUMN variant_key VARCHAR(64) NULL;
ALTER TABLE mall_product ADD CONSTRAINT fk_mall_product_spu FOREIGN KEY(spu_id) REFERENCES mall_spu(id);
ALTER TABLE mall_product ADD CONSTRAINT uk_mall_sku_variant UNIQUE(spu_id,variant_key);
CREATE TABLE mall_sku_property (
 product_id BIGINT NOT NULL,
 property_name VARCHAR(64) NOT NULL,
 property_value VARCHAR(100) NOT NULL,
 PRIMARY KEY(product_id,property_name),
 CONSTRAINT fk_mall_sku_property FOREIGN KEY(product_id) REFERENCES mall_product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_stock_adjustment (
 request_no VARCHAR(64) NOT NULL PRIMARY KEY,
 product_id BIGINT NOT NULL,
 delta INT NOT NULL,
 reason VARCHAR(200) NOT NULL,
 resulting_stock INT NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_mall_stock_adjustment_product FOREIGN KEY(product_id) REFERENCES mall_product(id),
 CONSTRAINT chk_mall_stock_adjustment_delta CHECK(delta<>0),
 CONSTRAINT chk_mall_stock_adjustment_stock CHECK(resulting_stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
