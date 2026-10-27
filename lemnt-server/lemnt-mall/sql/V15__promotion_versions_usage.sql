-- Additive internal T2 storage. No live execution, route or worker activation.
CREATE TABLE mall_promotion_root (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 state VARCHAR(16) NOT NULL DEFAULT 'DRAFT',revision BIGINT NOT NULL DEFAULT 0,
 active_version INT NULL,last_version INT NOT NULL DEFAULT 0,draft_json LONGTEXT NOT NULL,
 created_by BIGINT NOT NULL,updated_by BIGINT NOT NULL,
 UNIQUE KEY uk_promotion_code(code),
 CONSTRAINT chk_promotion_root_state CHECK(state IN ('DRAFT','PUBLISHED','PAUSED','ARCHIVED')),
 CONSTRAINT chk_promotion_root_revision CHECK(revision>=0 AND last_version>=0),
 CONSTRAINT chk_promotion_root_active CHECK((active_version IS NULL OR (active_version>0 AND active_version<=last_version)) AND (state NOT IN ('PUBLISHED','PAUSED') OR active_version IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_version (
 root_id BIGINT NOT NULL,version INT NOT NULL,draft_json LONGTEXT NOT NULL,rule_hash VARCHAR(64) NOT NULL,
 algorithm_version VARCHAR(16) NOT NULL,starts_at_epoch_ms BIGINT NOT NULL,ends_at_epoch_ms BIGINT NOT NULL,
 operator_id BIGINT NOT NULL,published_at_epoch_ms BIGINT NOT NULL,
 PRIMARY KEY(root_id,version),
 CONSTRAINT fk_promotion_version_root FOREIGN KEY(root_id) REFERENCES mall_promotion_root(id),
 CONSTRAINT chk_promotion_version CHECK(version>0 AND starts_at_epoch_ms>0 AND ends_at_epoch_ms>starts_at_epoch_ms AND algorithm_version='promo-1')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE mall_promotion_root ADD CONSTRAINT fk_promotion_active_version FOREIGN KEY(id,active_version) REFERENCES mall_promotion_version(root_id,version);
CREATE TABLE mall_promotion_scope (
 root_id BIGINT NOT NULL,version INT NOT NULL,type VARCHAR(16) NOT NULL,target_id BIGINT NOT NULL,
 PRIMARY KEY(root_id,version,type,target_id),KEY idx_promotion_scope_target(type,target_id,root_id),
 CONSTRAINT fk_promotion_scope_version FOREIGN KEY(root_id,version) REFERENCES mall_promotion_version(root_id,version),
 CONSTRAINT chk_promotion_scope CHECK(type IN ('SKU_IDS','SPU_IDS') AND target_id>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_counter (
 root_id BIGINT NOT NULL PRIMARY KEY,held_count BIGINT NOT NULL DEFAULT 0,used_count BIGINT NOT NULL DEFAULT 0,
 global_limit BIGINT NULL,member_limit BIGINT NULL,
 CONSTRAINT fk_promotion_counter_root FOREIGN KEY(root_id) REFERENCES mall_promotion_root(id),
 CONSTRAINT chk_promotion_counter CHECK(held_count>=0 AND used_count>=0 AND held_count<=9223372036854775807-used_count),
 CONSTRAINT chk_promotion_limits CHECK((global_limit IS NULL OR global_limit>0) AND (member_limit IS NULL OR member_limit>0) AND (global_limit IS NULL OR member_limit IS NULL OR member_limit<=global_limit)),
 CONSTRAINT chk_promotion_counter_limit CHECK(global_limit IS NULL OR held_count+used_count<=global_limit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_member_counter (
 root_id BIGINT NOT NULL,member_id BIGINT NOT NULL,held_count BIGINT NOT NULL DEFAULT 0,used_count BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(root_id,member_id),CONSTRAINT fk_promotion_member_root FOREIGN KEY(root_id) REFERENCES mall_promotion_root(id),
 CONSTRAINT chk_promotion_member_count CHECK(member_id>0 AND held_count>=0 AND used_count>=0 AND held_count<=9223372036854775807-used_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_usage (
 order_no VARCHAR(40) NOT NULL,root_id BIGINT NOT NULL,version INT NOT NULL,member_id BIGINT NOT NULL,
 discount_fen BIGINT NOT NULL,rule_hash VARCHAR(64) NOT NULL,state VARCHAR(16) NOT NULL,
 created_at_epoch_ms BIGINT NOT NULL,updated_at_epoch_ms BIGINT NOT NULL,
 PRIMARY KEY(order_no,root_id),KEY idx_promotion_usage_state(state,root_id,order_no),

 CONSTRAINT fk_promotion_usage_version FOREIGN KEY(root_id,version) REFERENCES mall_promotion_version(root_id,version),
 CONSTRAINT chk_promotion_usage CHECK(member_id>0 AND discount_fen>0 AND state IN ('HELD','USED','RELEASED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_usage_event (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,order_no VARCHAR(40) NOT NULL,root_id BIGINT NOT NULL,
 from_state VARCHAR(16) NULL,target_state VARCHAR(16) NOT NULL,reason VARCHAR(100) NOT NULL,occurred_at_epoch_ms BIGINT NOT NULL,
 UNIQUE KEY uk_promotion_usage_event(order_no,root_id,target_state),
 CONSTRAINT fk_promotion_event_usage FOREIGN KEY(order_no,root_id) REFERENCES mall_promotion_usage(order_no,root_id),
 CONSTRAINT chk_promotion_event CHECK((from_state IS NULL AND target_state='HELD') OR (from_state='HELD' AND target_state IN ('USED','RELEASED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_admin_request (
 operator_id BIGINT NOT NULL,request_no VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 action VARCHAR(16) NOT NULL,fingerprint VARCHAR(64) NOT NULL,created_at_epoch_ms BIGINT NOT NULL,completed_at_epoch_ms BIGINT NULL,
 result_id BIGINT NULL,result_revision BIGINT NULL,result_state VARCHAR(16) NULL,result_active_version INT NULL,result_last_version INT NULL,
 PRIMARY KEY(operator_id,request_no),CONSTRAINT fk_promotion_request_root FOREIGN KEY(result_id) REFERENCES mall_promotion_root(id),
 CONSTRAINT chk_promotion_request_operator CHECK(operator_id>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
