-- 积分商城活动及活动商品；金额沿用项目元单位，积分为整数。
CREATE TABLE mall_promotion_point_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 remark VARCHAR(500) NULL COMMENT '备注说明',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
 total_stock INT NOT NULL DEFAULT 0 COMMENT '活动配置总库存',
 CONSTRAINT chk_point_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_point_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动';

CREATE TABLE mall_promotion_point_product (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 exchange_count INT NOT NULL COMMENT '每份兑换包含的商品数量',
 point INT NOT NULL COMMENT '每份兑换消耗的积分数',
 price DECIMAL(19,2) NOT NULL COMMENT '每份兑换需支付的现金金额，单位元',
 stock INT NOT NULL COMMENT '当前可用库存',
 activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
 UNIQUE KEY uk_point_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_point_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_point_activity(id),
 CONSTRAINT chk_point_product_count CHECK(exchange_count>0),
 CONSTRAINT chk_point_product_point CHECK(point>=0),
 CONSTRAINT chk_point_product_price CHECK(price>=0),
 CONSTRAINT chk_point_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动商品';
