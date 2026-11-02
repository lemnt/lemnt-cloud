-- 秒杀时段、活动及活动商品；金额沿用项目元单位 DECIMAL(19,2)。
CREATE TABLE mall_promotion_seckill_config (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 start_time VARCHAR(8) NOT NULL COMMENT '秒杀时段开始时刻，HH:mm:ss',
 end_time VARCHAR(8) NOT NULL COMMENT '秒杀时段结束时刻，HH:mm:ss',
 slider_pic_urls LONGTEXT NOT NULL COMMENT '轮播图片地址JSON数组',
 status INT NOT NULL DEFAULT 0 COMMENT '业务状态',
 CONSTRAINT chk_seckill_config_status CHECK(status IN (0,1)),
 CONSTRAINT chk_seckill_config_time CHECK(start_time<end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀时段配置';

CREATE TABLE mall_promotion_seckill_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 remark VARCHAR(500) NULL COMMENT '备注说明',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',
 end_time DATETIME(3) NOT NULL COMMENT '结束时间',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 config_ids LONGTEXT NOT NULL COMMENT '秒杀时段编号JSON数组',
 total_limit_count INT NULL COMMENT '每会员累计购买数量上限',
 single_limit_count INT NULL COMMENT '单次购买数量上限',
 stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
 total_stock INT NOT NULL DEFAULT 0 COMMENT '活动配置总库存',
 CONSTRAINT chk_seckill_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_seckill_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_seckill_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动';

CREATE TABLE mall_promotion_seckill_product (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 config_ids LONGTEXT NOT NULL COMMENT '秒杀时段编号JSON数组',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 seckill_price DECIMAL(19,2) NOT NULL COMMENT '秒杀成交单价，单位元',
 stock INT NOT NULL COMMENT '当前可用库存',
 activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
 activity_start_time DATETIME(3) NOT NULL COMMENT '活动开始时间快照',
 activity_end_time DATETIME(3) NOT NULL COMMENT '活动结束时间快照',
 UNIQUE KEY uk_seckill_activity_sku(activity_id,sku_id),
 KEY idx_seckill_product_spu(spu_id),
 CONSTRAINT fk_seckill_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_seckill_activity(id),
 CONSTRAINT chk_seckill_product_price CHECK(seckill_price>0),
 CONSTRAINT chk_seckill_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动商品';
