CREATE TABLE mall_product_favorite (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL, spu_id BIGINT NOT NULL,
 deleted BOOLEAN NOT NULL DEFAULT FALSE,
 
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 KEY idx_favorite_user_spu(user_id,spu_id,deleted),
 KEY idx_favorite_user_latest(user_id,deleted,id),
 KEY idx_favorite_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_product_browse_history (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL, spu_id BIGINT NOT NULL,
 deleted BOOLEAN NOT NULL DEFAULT FALSE,
 user_deleted BOOLEAN NOT NULL DEFAULT FALSE,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 KEY idx_history_user_spu(user_id,spu_id,deleted),
 KEY idx_history_user_latest(user_id,deleted,id),
 KEY idx_history_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
