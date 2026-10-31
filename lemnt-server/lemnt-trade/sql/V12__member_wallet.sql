-- 会员钱包账户与余额流水；金额单位为元，DECIMAL(19,2)。
CREATE TABLE IF NOT EXISTS trade_member_wallet (
 id BIGINT NOT NULL AUTO_INCREMENT,
 member_id BIGINT NOT NULL,
 balance DECIMAL(19,2) NOT NULL DEFAULT 0,
 freeze_price DECIMAL(19,2) NOT NULL DEFAULT 0,
 total_expense DECIMAL(19,2) NOT NULL DEFAULT 0,
 total_recharge DECIMAL(19,2) NOT NULL DEFAULT 0,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_member_wallet(member_id),
 CONSTRAINT chk_wallet_balance CHECK(balance>=0),
 CONSTRAINT chk_wallet_freeze CHECK(freeze_price>=0),
 CONSTRAINT chk_wallet_expense CHECK(total_expense>=0),
 CONSTRAINT chk_wallet_recharge CHECK(total_recharge>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包账户';

CREATE TABLE IF NOT EXISTS trade_member_wallet_transaction (
 id BIGINT NOT NULL AUTO_INCREMENT,
 transaction_no VARCHAR(64) NOT NULL,
 wallet_id BIGINT NOT NULL,
 biz_type VARCHAR(32) NOT NULL,
 biz_no VARCHAR(64) NOT NULL,
 title VARCHAR(128) NOT NULL,
 amount DECIMAL(19,2) NOT NULL,
 balance_after DECIMAL(19,2) NOT NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_wallet_tx_biz(wallet_id,biz_type,biz_no),
 KEY idx_wallet_tx_wallet(wallet_id,id),
 CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_tx_amount CHECK(amount<>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包余额流水';
