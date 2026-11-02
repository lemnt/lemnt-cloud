-- 会员钱包账户与余额流水；金额单位为元，DECIMAL(19,2)。
CREATE TABLE IF NOT EXISTS trade_member_wallet (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 member_id BIGINT NOT NULL COMMENT '会员编号',
 balance DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '可用余额，单位元',
 freeze_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '冻结余额，单位元',
 total_expense DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '累计支出金额，单位元',
 total_recharge DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '累计充值金额，单位元',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_member_wallet(member_id),
 CONSTRAINT chk_wallet_balance CHECK(balance>=0),
 CONSTRAINT chk_wallet_freeze CHECK(freeze_price>=0),
 CONSTRAINT chk_wallet_expense CHECK(total_expense>=0),
 CONSTRAINT chk_wallet_recharge CHECK(total_recharge>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包账户';

CREATE TABLE IF NOT EXISTS trade_member_wallet_transaction (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 transaction_no VARCHAR(64) NOT NULL COMMENT '钱包流水业务编号',
 wallet_id BIGINT NOT NULL COMMENT '会员钱包编号',
 biz_type VARCHAR(32) NOT NULL COMMENT '钱包业务类型：RECHARGE、RECHARGE_REFUND、PAYMENT、PAYMENT_REFUND、UPDATE_BALANCE、TRANSFER、BROKERAGE_WITHDRAW',
 biz_no VARCHAR(64) NOT NULL COMMENT '业务单号',
 title VARCHAR(128) NOT NULL COMMENT '标题',
 amount DECIMAL(19,2) NOT NULL COMMENT '本次余额变更量，收入为正、支出为负，单位元',
 balance_after DECIMAL(19,2) NOT NULL COMMENT '变更后可用余额，单位元',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_wallet_tx_biz(wallet_id,biz_type,biz_no),
 KEY idx_wallet_tx_wallet(wallet_id,id),
 CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_tx_amount CHECK(amount<>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包余额流水';
