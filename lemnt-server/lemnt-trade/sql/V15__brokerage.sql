-- 分销用户、佣金记录与提现；金额沿用项目元单位 DECIMAL(19,2)。
CREATE TABLE IF NOT EXISTS trade_brokerage_user (
 id BIGINT NOT NULL PRIMARY KEY,
 bind_user_id BIGINT NULL,
 bind_user_time DATETIME(3) NULL,
 brokerage_enabled TINYINT(1) NOT NULL DEFAULT 0,
 brokerage_time DATETIME(3) NULL,
 brokerage_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 frozen_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 KEY idx_brokerage_bind_user(bind_user_id),
 CONSTRAINT chk_brokerage_user_price CHECK(brokerage_price>=0 AND frozen_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_brokerage_record (
 id BIGINT NOT NULL PRIMARY KEY,
 user_id BIGINT NOT NULL,
 biz_id VARCHAR(64) NOT NULL,
 biz_type INT NOT NULL,
 title VARCHAR(100) NOT NULL,
 description VARCHAR(255) NULL,
 price DECIMAL(19,2) NOT NULL,
 total_price DECIMAL(19,2) NOT NULL,
 status INT NOT NULL,
 frozen_days INT NULL,
 unfreeze_time DATETIME(3) NULL,
 source_user_level INT NULL,
 source_user_id BIGINT NULL,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_brokerage_record_biz(user_id,biz_type,biz_id),
 KEY idx_brokerage_record_user(user_id,id),
 KEY idx_brokerage_record_unfreeze(status,unfreeze_time),
 CONSTRAINT chk_brokerage_record_status CHECK(status IN (0,1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_brokerage_withdraw (
 id BIGINT NOT NULL PRIMARY KEY,
 user_id BIGINT NOT NULL,
 price DECIMAL(19,2) NOT NULL,
 fee_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 total_price DECIMAL(19,2) NOT NULL,
 type INT NOT NULL,
 user_name VARCHAR(64) NOT NULL,
 user_account VARCHAR(128) NOT NULL,
 qr_code_url VARCHAR(500) NULL,
 bank_name VARCHAR(100) NULL,
 bank_address VARCHAR(255) NULL,
 status INT NOT NULL,
 audit_reason VARCHAR(255) NULL,
 audit_time DATETIME(3) NULL,
 remark VARCHAR(255) NULL,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_brokerage_withdraw_user(user_id,id),
 KEY idx_brokerage_withdraw_status(status,id),
 CONSTRAINT chk_brokerage_withdraw_price CHECK(price>0 AND fee_price>=0 AND fee_price<=price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
