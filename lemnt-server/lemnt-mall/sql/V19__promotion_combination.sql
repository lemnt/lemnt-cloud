-- 拼团活动、活动商品和拼团记录；金额沿用项目元单位。
CREATE TABLE mall_promotion_combination_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 total_limit_count INT NOT NULL COMMENT '每会员累计购买数量上限',
 single_limit_count INT NOT NULL COMMENT '单次购买数量上限',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',
 end_time DATETIME(3) NOT NULL COMMENT '结束时间',
 user_size INT NOT NULL COMMENT '拼团成团所需人数',
 virtual_group BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许虚拟成团',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 limit_duration INT NOT NULL COMMENT '拼团有效时长，单位小时',
 CONSTRAINT chk_combination_activity_status CHECK(status IN (0,1)),
 CONSTRAINT chk_combination_activity_time CHECK(end_time>start_time),
 CONSTRAINT chk_combination_activity_user_size CHECK(user_size>=2),
 CONSTRAINT chk_combination_activity_duration CHECK(limit_duration>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动';

CREATE TABLE mall_promotion_combination_product (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 combination_price DECIMAL(19,2) NOT NULL COMMENT '拼团成交单价，单位元',
 activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
 activity_start_time DATETIME(3) NOT NULL COMMENT '活动开始时间快照',
 activity_end_time DATETIME(3) NOT NULL COMMENT '活动结束时间快照',
 UNIQUE KEY uk_combination_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_combination_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_combination_activity(id),
 CONSTRAINT chk_combination_product_price CHECK(combination_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动商品';

CREATE TABLE mall_promotion_combination_record (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 activity_id BIGINT NOT NULL COMMENT '活动编号',
 combination_price DECIMAL(19,2) NOT NULL COMMENT '拼团成交单价，单位元',
 spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 spu_name VARCHAR(200) NOT NULL COMMENT '商品SPU名称快照',
 pic_url VARCHAR(1024) NULL COMMENT '图片地址',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 count INT NOT NULL COMMENT '商品数量',
 user_id BIGINT NOT NULL COMMENT '会员编号',
 nickname VARCHAR(100) NULL COMMENT '会员昵称',
 avatar VARCHAR(1024) NULL COMMENT '头像地址',
 head_id BIGINT NOT NULL COMMENT '团长拼团记录编号，0表示团长本人',
 status INT NOT NULL DEFAULT 0 COMMENT '拼团状态：0进行中、1成功、2失败',
 order_id BIGINT NULL COMMENT '交易订单主键编号',
 user_size INT NOT NULL COMMENT '拼团成团所需人数',
 user_count INT NOT NULL DEFAULT 1 COMMENT '当前参团人数',
 virtual_group BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许虚拟成团',
 expire_time DATETIME(3) NOT NULL COMMENT '到期时间',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',
 end_time DATETIME(3) NULL COMMENT '结束时间',
 KEY idx_combination_record_user(user_id,activity_id),
 KEY idx_combination_record_head(head_id,status),
 KEY idx_combination_record_expire(head_id,status,expire_time),
 CONSTRAINT chk_combination_record_status CHECK(status IN (0,1,2)),
 CONSTRAINT chk_combination_record_count CHECK(count>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团参与记录';
