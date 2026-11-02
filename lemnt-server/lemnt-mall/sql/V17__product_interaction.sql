CREATE TABLE mall_product_favorite (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '会员编号', spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
 
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 KEY idx_favorite_user_spu(user_id,spu_id,deleted),
 KEY idx_favorite_user_latest(user_id,deleted,id),
 KEY idx_favorite_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员商品收藏';
CREATE TABLE mall_product_browse_history (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '会员编号', spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
 deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
 user_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '会员是否删除记录',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 KEY idx_history_user_spu(user_id,spu_id,deleted),
 KEY idx_history_user_latest(user_id,deleted,id),
 KEY idx_history_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员商品浏览记录';
