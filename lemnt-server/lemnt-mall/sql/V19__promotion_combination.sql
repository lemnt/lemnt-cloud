-- 拼团活动、活动商品和拼团记录；金额沿用项目元单位。
CREATE TABLE mall_promotion_combination_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 spu_id BIGINT NOT NULL,
 total_limit_count INT NOT NULL,
 single_limit_count INT NOT NULL,
 start_time DATETIME(3) NOT NULL,
 end_time DATETIME(3) NOT NULL,
 user_size INT NOT NULL,
 virtual_group BOOLEAN NOT NULL DEFAULT FALSE,
 status INT NOT NULL DEFAULT 0,
 limit_duration INT NOT NULL,
 CONSTRAINT chk_combination_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_combination_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_combination_activity_user_size CHECK(user_size>=2),
 CONSTRAINT chk_combination_activity_duration CHECK(limit_duration>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_combination_product (
 id BIGINT NOT NULL PRIMARY KEY,
 activity_id BIGINT NOT NULL,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 combination_price DECIMAL(19,2) NOT NULL,
 activity_status INT NOT NULL DEFAULT 0,
 activity_start_time DATETIME(3) NOT NULL,
 activity_end_time DATETIME(3) NOT NULL,
 UNIQUE KEY uk_combination_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_combination_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_combination_activity(id),
 CONSTRAINT chk_combination_product_price CHECK(combination_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_combination_record (
 id BIGINT NOT NULL PRIMARY KEY,
 activity_id BIGINT NOT NULL,
 combination_price DECIMAL(19,2) NOT NULL,
 spu_id BIGINT NOT NULL,
 spu_name VARCHAR(200) NOT NULL,
 pic_url VARCHAR(1024) NULL,
 sku_id BIGINT NOT NULL,
 count INT NOT NULL,
 user_id BIGINT NOT NULL,
 nickname VARCHAR(100) NULL,
 avatar VARCHAR(1024) NULL,
 head_id BIGINT NOT NULL,
 status INT NOT NULL DEFAULT 0,
 order_id BIGINT NULL,
 user_size INT NOT NULL,
 user_count INT NOT NULL DEFAULT 1,
 virtual_group BOOLEAN NOT NULL DEFAULT FALSE,
 expire_time DATETIME(3) NOT NULL,
 start_time DATETIME(3) NOT NULL,
 end_time DATETIME(3) NULL,
 KEY idx_combination_record_user(user_id,activity_id),
 KEY idx_combination_record_head(head_id,status),
 KEY idx_combination_record_expire(head_id,status,expire_time),
 CONSTRAINT chk_combination_record_status CHECK(status IN (0,1,2)),
 CONSTRAINT chk_combination_record_count CHECK(count>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
