-- 砍价活动、砍价记录和助力记录；金额沿用项目元单位。
CREATE TABLE mall_promotion_bargain_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 start_time DATETIME(3) NOT NULL,
 end_time DATETIME(3) NOT NULL,
 status INT NOT NULL DEFAULT 0,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 bargain_first_price DECIMAL(19,2) NOT NULL,
 bargain_min_price DECIMAL(19,2) NOT NULL,
 stock INT NOT NULL,
 total_stock INT NOT NULL,
 help_max_count INT NOT NULL,
 bargain_count INT NOT NULL,
 total_limit_count INT NOT NULL,
 random_min_price DECIMAL(19,2) NOT NULL,
 random_max_price DECIMAL(19,2) NOT NULL,
 CONSTRAINT chk_bargain_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_bargain_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_bargain_activity_price CHECK(bargain_first_price>0 AND bargain_min_price>=0 AND bargain_min_price<bargain_first_price),
 CONSTRAINT chk_bargain_activity_random CHECK(random_min_price>0 AND random_max_price>=random_min_price),
 CONSTRAINT chk_bargain_activity_stock CHECK(stock>=0 AND total_stock>=stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_bargain_record (
 id BIGINT NOT NULL PRIMARY KEY,
 user_id BIGINT NOT NULL,
 activity_id BIGINT NOT NULL,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 bargain_first_price DECIMAL(19,2) NOT NULL,
 bargain_price DECIMAL(19,2) NOT NULL,
 status INT NOT NULL DEFAULT 1,
 end_time DATETIME(3) NULL,
 order_id BIGINT NULL,
 KEY idx_bargain_record_user(user_id,activity_id,status),
 CONSTRAINT fk_bargain_record_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_bargain_activity(id),
 CONSTRAINT chk_bargain_record_status CHECK(status IN (1,2,3)),
 CONSTRAINT chk_bargain_record_price CHECK(bargain_price>=0 AND bargain_price<=bargain_first_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_bargain_help (
 id BIGINT NOT NULL PRIMARY KEY,
 activity_id BIGINT NOT NULL,
 record_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 reduce_price DECIMAL(19,2) NOT NULL,
 UNIQUE KEY uk_bargain_help_record_user(record_id,user_id),
 KEY idx_bargain_help_user_activity(user_id,activity_id),
 CONSTRAINT fk_bargain_help_record FOREIGN KEY(record_id) REFERENCES mall_promotion_bargain_record(id),
 CONSTRAINT chk_bargain_help_price CHECK(reduce_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
