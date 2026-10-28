-- Additive migration. Back up and test on MySQL before production use.
-- Existing mall_product_sku IDs remain the SKU IDs referenced by orders/reservations.
CREATE TABLE mall_product_category (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 parent_id BIGINT NULL,
 name VARCHAR(100) NOT NULL,
 pic_url VARCHAR(500) NULL,
 sort INT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
 CONSTRAINT fk_mall_category_parent FOREIGN KEY(parent_id) REFERENCES mall_product_category(id),
 CONSTRAINT chk_mall_category_status CHECK(status IN ('ENABLED','DISABLED')),
 KEY idx_mall_category_parent(parent_id,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_product_brand (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 pic_url VARCHAR(500) NULL,
 description VARCHAR(2000) NULL,
 sort INT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
 CONSTRAINT chk_mall_brand_status CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_product_spu (
 delivery_types LONGTEXT NULL,
 delivery_template_id BIGINT NULL,
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
 CONSTRAINT fk_mall_spu_category FOREIGN KEY(category_id) REFERENCES mall_product_category(id),
 CONSTRAINT fk_mall_spu_brand FOREIGN KEY(brand_id) REFERENCES mall_product_brand(id),
 CONSTRAINT chk_mall_spu_status CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE mall_product_sku ADD COLUMN spu_id BIGINT NULL;
ALTER TABLE mall_product_sku ADD COLUMN variant_key VARCHAR(64) NULL;
ALTER TABLE mall_product_sku ADD CONSTRAINT fk_mall_product_spu FOREIGN KEY(spu_id) REFERENCES mall_product_spu(id);
ALTER TABLE mall_product_sku ADD CONSTRAINT uk_mall_sku_variant UNIQUE(spu_id,variant_key);
CREATE TABLE mall_product_sku_spec (
 product_id BIGINT NOT NULL,
 property_name VARCHAR(64) NOT NULL,
 property_value VARCHAR(100) NOT NULL,
 PRIMARY KEY(product_id,property_name),
 CONSTRAINT fk_mall_sku_property FOREIGN KEY(product_id) REFERENCES mall_product_sku(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 独立库存调整台账已移除，SKU库存随SPU保存。
