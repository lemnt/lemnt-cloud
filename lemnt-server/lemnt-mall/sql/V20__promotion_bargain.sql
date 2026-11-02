-- 砍价活动、砍价记录和助力记录；金额沿用项目元单位。
CREATE TABLE mall_promotion_bargain_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',
 end_time DATETIME(3) NOT NULL COMMENT '结束时间',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 bargain_first_price DECIMAL(19,2) NOT NULL COMMENT '砍价起始金额，单位元',
 bargain_min_price DECIMAL(19,2) NOT NULL COMMENT '砍价底价，单位元',
 stock INT NOT NULL COMMENT '当前可用库存',
 total_stock INT NOT NULL COMMENT '活动配置总库存',
 help_max_count INT NOT NULL COMMENT '砍价助力人数上限',
 bargain_count INT NOT NULL COMMENT '帮砍次数配置',
 total_limit_count INT NOT NULL COMMENT '每会员累计购买数量上限',
 random_min_price DECIMAL(19,2) NOT NULL COMMENT '单次助力随机减免金额下限，单位元',
 random_max_price DECIMAL(19,2) NOT NULL COMMENT '单次助力随机减免金额上限，单位元',
 CONSTRAINT chk_bargain_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_bargain_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_bargain_activity_price CHECK(bargain_first_price>0 AND bargain_min_price>=0 AND bargain_min_price<bargain_first_price),
 CONSTRAINT chk_bargain_activity_random CHECK(random_min_price>0 AND random_max_price>=random_min_price),
 CONSTRAINT chk_bargain_activity_stock CHECK(stock>=0 AND total_stock>=stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价活动';

CREATE TABLE mall_promotion_bargain_record (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '会员编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 bargain_first_price DECIMAL(19,2) NOT NULL COMMENT '砍价起始金额，单位元',
 bargain_price DECIMAL(19,2) NOT NULL COMMENT '当前砍价金额，单位元',
 status INT NOT NULL DEFAULT 1 COMMENT '砍价状态：1进行中、2成功、3失败',
 end_time DATETIME(3) NULL COMMENT '结束时间',
 order_id BIGINT NULL COMMENT '交易订单主键编号',
 KEY idx_bargain_record_user(user_id,activity_id,status),
 CONSTRAINT fk_bargain_record_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_bargain_activity(id),
 CONSTRAINT chk_bargain_record_status CHECK(status IN (1,2,3)),
 CONSTRAINT chk_bargain_record_price CHECK(bargain_price>=0 AND bargain_price<=bargain_first_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员砍价参与记录';

CREATE TABLE mall_promotion_bargain_help (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 record_id BIGINT NOT NULL COMMENT '活动参与记录编号',
 user_id BIGINT NOT NULL COMMENT '会员编号',
 reduce_price DECIMAL(19,2) NOT NULL COMMENT '本次助力减免金额，单位元',
 UNIQUE KEY uk_bargain_help_record_user(record_id,user_id),
 KEY idx_bargain_help_user_activity(user_id,activity_id),
 CONSTRAINT fk_bargain_help_record FOREIGN KEY(record_id) REFERENCES mall_promotion_bargain_record(id),
 CONSTRAINT chk_bargain_help_price CHECK(reduce_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价助力记录';
