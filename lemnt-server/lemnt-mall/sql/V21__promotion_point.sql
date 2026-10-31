-- 积分商城活动及活动商品；金额沿用项目元单位，积分为整数。
CREATE TABLE mall_promotion_point_activity (
 id BIGINT NOT NULL PRIMARY KEY,
 spu_id BIGINT NOT NULL,
 status INT NOT NULL DEFAULT 0,
 remark VARCHAR(500) NULL,
 sort INT NOT NULL DEFAULT 0,
 stock INT NOT NULL DEFAULT 0,
 total_stock INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_point_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_point_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mall_promotion_point_product (
 id BIGINT NOT NULL PRIMARY KEY,
 activity_id BIGINT NOT NULL,
 spu_id BIGINT NOT NULL,
 sku_id BIGINT NOT NULL,
 exchange_count INT NOT NULL,
 point INT NOT NULL,
 price DECIMAL(19,2) NOT NULL,
 stock INT NOT NULL,
 activity_status INT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_point_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_point_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_point_activity(id),
 CONSTRAINT chk_point_product_count CHECK(exchange_count>0),
 CONSTRAINT chk_point_product_point CHECK(point>=0),
 CONSTRAINT chk_point_product_price CHECK(price>=0),
 CONSTRAINT chk_point_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
