-- 充值套餐与充值单；金额单位为元。到账确认与退款由后台命令触发，真实渠道SDK后置。
CREATE TABLE IF NOT EXISTS trade_pay_recharge_package (
 id BIGINT NOT NULL AUTO_INCREMENT,
 name VARCHAR(64) NOT NULL,
 pay_price DECIMAL(19,2) NOT NULL,
 bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 status TINYINT NOT NULL DEFAULT 0,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 deleted TINYINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_recharge_package_name(name),
 CONSTRAINT chk_recharge_package_pay CHECK(pay_price>0),
 CONSTRAINT chk_recharge_package_bonus CHECK(bonus_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值套餐';

CREATE TABLE IF NOT EXISTS trade_pay_wallet_recharge (
 id BIGINT NOT NULL AUTO_INCREMENT,
 recharge_no VARCHAR(40) NOT NULL,
 request_no VARCHAR(64) NOT NULL,
 wallet_id BIGINT NOT NULL,
 member_id BIGINT NOT NULL,
 package_id BIGINT NULL,
 pay_price DECIMAL(19,2) NOT NULL,
 bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 total_price DECIMAL(19,2) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 channel_code VARCHAR(32) NULL,
 paid_time DATETIME NULL,
 refunded_time DATETIME NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_wallet_recharge_no(recharge_no),
 UNIQUE KEY uk_trade_wallet_recharge_request(request_no),
 KEY idx_trade_wallet_recharge_member(member_id,id),
 KEY idx_trade_wallet_recharge_status(status),
 CONSTRAINT fk_wallet_recharge_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_recharge_total CHECK(total_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包充值单';
