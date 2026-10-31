-- 按芋道直接管理活动，不保存发布版本、活动额度和操作请求账本。
CREATE TABLE mall_promotion_reward_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,status INT NOT NULL DEFAULT 0,
 start_time DATETIME(3) NOT NULL,end_time DATETIME(3) NOT NULL,remark VARCHAR(500) NULL,
 condition_type VARCHAR(16) NOT NULL,product_scope VARCHAR(16) NOT NULL,
 product_scope_values_json LONGTEXT NOT NULL,rules_json LONGTEXT NOT NULL,
 CONSTRAINT chk_reward_status CHECK(status IN (0,1)),
 CONSTRAINT chk_reward_time CHECK(end_time>start_time),
 CONSTRAINT chk_reward_condition CHECK(condition_type IN ('PRICE','COUNT')),
 CONSTRAINT chk_reward_scope CHECK(product_scope IN ('ALL','SPU_IDS','CATEGORY_IDS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_discount_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,status INT NOT NULL DEFAULT 0,
 start_time DATETIME(3) NOT NULL,end_time DATETIME(3) NOT NULL,remark VARCHAR(500) NULL,
 CONSTRAINT chk_discount_status CHECK(status IN (0,1)),
 CONSTRAINT chk_discount_time CHECK(end_time>start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_discount_product (
 id BIGINT NOT NULL PRIMARY KEY,activity_id BIGINT NOT NULL,spu_id BIGINT NOT NULL,sku_id BIGINT NOT NULL,
 discount_type VARCHAR(16) NOT NULL,discount_percent INT NULL,discount_price DECIMAL(19,2) NULL,
 UNIQUE KEY uk_discount_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_discount_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_discount_activity(id),
 CONSTRAINT chk_discount_product CHECK((discount_type='PERCENT' AND discount_percent BETWEEN 1 AND 9999)
 OR (discount_type='PRICE' AND discount_price>0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
