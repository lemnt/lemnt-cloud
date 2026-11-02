-- 按芋道直接管理活动，不保存发布版本、活动额度和操作请求账本。
CREATE TABLE mall_promotion_reward_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',end_time DATETIME(3) NOT NULL COMMENT '结束时间',remark VARCHAR(500) NULL COMMENT '备注说明',
 condition_type VARCHAR(16) NOT NULL COMMENT '满减门槛类型：PRICE金额、COUNT件数',product_scope VARCHAR(16) NOT NULL COMMENT '商品适用范围：ALL全部、SPU_IDS指定SPU、CATEGORY_IDS指定分类',
 product_scope_values_json LONGTEXT NOT NULL COMMENT '适用商品或分类编号JSON数组',rules_json LONGTEXT NOT NULL COMMENT '满减阶梯及包邮、赠积分、赠券规则JSON',
 CONSTRAINT chk_reward_status CHECK(status IN (0,1)),
 CONSTRAINT chk_reward_time CHECK(end_time>start_time),
 CONSTRAINT chk_reward_condition CHECK(condition_type IN ('PRICE','COUNT')),
 CONSTRAINT chk_reward_scope CHECK(product_scope IN ('ALL','SPU_IDS','CATEGORY_IDS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '满减赠送活动';
CREATE TABLE mall_promotion_discount_activity (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 start_time DATETIME(3) NOT NULL COMMENT '开始时间',end_time DATETIME(3) NOT NULL COMMENT '结束时间',remark VARCHAR(500) NULL COMMENT '备注说明',
 CONSTRAINT chk_discount_status CHECK(status IN (0,1)),
 CONSTRAINT chk_discount_time CHECK(end_time>start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动';
CREATE TABLE mall_promotion_discount_product (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',activity_id BIGINT NOT NULL COMMENT '活动编号',spu_id BIGINT NOT NULL COMMENT '商品SPU编号',sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 discount_type VARCHAR(16) NOT NULL COMMENT '优惠方式：PERCENT比例折扣、PRICE固定减价',discount_percent INT NULL COMMENT '支付比例万分比，8000表示支付原价的80%',discount_price DECIMAL(19,2) NULL COMMENT '优惠减免金额，单位元',
 UNIQUE KEY uk_discount_activity_sku(activity_id,sku_id),
 CONSTRAINT fk_discount_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_discount_activity(id),
 CONSTRAINT chk_discount_product CHECK((discount_type='PERCENT' AND discount_percent BETWEEN 1 AND 9999)
 OR (discount_type='PRICE' AND discount_price>0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动商品';
