CREATE TABLE mall_product_spec (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(64) NOT NULL,
 remark VARCHAR(500) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uk_mall_product_spec_name(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_product_spec_value (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 spec_id BIGINT NOT NULL,
 name VARCHAR(100) NOT NULL,
 remark VARCHAR(500) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uk_mall_product_spec_value(spec_id,name),
 CONSTRAINT fk_mall_product_spec_value FOREIGN KEY(spec_id) REFERENCES mall_product_spec(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_product_comment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 order_no VARCHAR(40) NOT NULL,
 member_id BIGINT NOT NULL,
 score INT NOT NULL,
 content VARCHAR(1000) NOT NULL,
 visible BOOLEAN NOT NULL DEFAULT TRUE,
 reply_content VARCHAR(1000) NULL,
 reply_user_id BIGINT NULL,
 reply_time DATETIME NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uk_mall_comment_order_sku(order_no,sku_id),
 KEY idx_mall_comment_spu(spu_id,id),
 CONSTRAINT fk_mall_comment_spu FOREIGN KEY(spu_id) REFERENCES mall_product_spu(id),
 CONSTRAINT fk_mall_comment_sku FOREIGN KEY(sku_id) REFERENCES mall_product_sku(id),
 CONSTRAINT chk_mall_comment_score CHECK(score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
