-- 会员钱包间转账；同步转出转入，request_no 幂等，金额单位为元。
CREATE TABLE IF NOT EXISTS trade_member_wallet_transfer (
 id BIGINT NOT NULL AUTO_INCREMENT,
 transfer_no VARCHAR(40) NOT NULL,
 request_no VARCHAR(64) NOT NULL,
 from_wallet_id BIGINT NOT NULL,
 from_member_id BIGINT NOT NULL,
 to_wallet_id BIGINT NOT NULL,
 to_member_id BIGINT NOT NULL,
 amount DECIMAL(19,2) NOT NULL,
 remark VARCHAR(128) NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_wallet_transfer_no(transfer_no),
 UNIQUE KEY uk_wallet_transfer_request(request_no),
 KEY idx_wallet_transfer_from(from_member_id,id), KEY idx_wallet_transfer_to(to_member_id,id),
 CONSTRAINT fk_wallet_transfer_from FOREIGN KEY(from_wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT fk_wallet_transfer_to FOREIGN KEY(to_wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_transfer_amount CHECK(amount>0),
 CONSTRAINT chk_wallet_transfer_distinct CHECK(from_member_id<>to_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包转账';
