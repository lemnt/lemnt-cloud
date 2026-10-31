-- 秒杀时段、活动及活动商品；金额沿用项目元单位 DECIMAL(19,2)。
CREATE TABLE mall_promotion_seckill_config (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 start_time VARCHAR(8) NOT NULL,
 end_time VARCHAR(8) NOT NULL,
 slider_pic_urls LONGTEXT NOT NULL,
 status INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_seckill_config_status CHECK(status IN (0,1)),
 CONSTRAINT chk_seckill_config_time CHECK(start_time<end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_seckill_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 spu_id BIGINT NOT NULL,
 name VARCHAR(100) NOT NULL,
 status INT NOT NULL DEFAULT 0,
 remark VARCHAR(500) NULL,
 start_time DATETIME(3) NOT NULL,
 end_time DATETIME(3) NOT NULL,
 sort INT NOT NULL DEFAULT 0,
 config_ids LONGTEXT NOT NULL,
 total_limit_count INT NULL,
 single_limit_count INT NULL,
 stock INT NOT NULL DEFAULT 0,
 total_stock INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_seckill_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_seckill_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_seckill_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_seckill_product (
 id BIGINT NOT NULL PRIMARY KEY,
 activity_id BIGINT NOT NULL,
 config_ids LONGTEXT NOT NULL,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 seckill_price DECIMAL(19,2) NOT NULL,
 stock INT NOT NULL,
 activity_status INT NOT NULL DEFAULT 0,
 activity_start_time DATETIME(3) NOT NULL,
 activity_end_time DATETIME(3) NOT NULL,
 UNIQUE KEY uk_seckill_activity_sku(activity_id,sku_id),
 KEY idx_seckill_product_spu(spu_id),
 CONSTRAINT fk_seckill_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_seckill_activity(id),
 CONSTRAINT chk_seckill_product_price CHECK(seckill_price>0),
 CONSTRAINT chk_seckill_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
