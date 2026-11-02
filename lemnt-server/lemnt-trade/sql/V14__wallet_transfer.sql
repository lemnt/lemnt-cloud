-- 会员钱包间转账；同步转出转入，request_no 幂等，金额单位为元。
CREATE TABLE IF NOT EXISTS trade_member_wallet_transfer (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 transfer_no VARCHAR(40) NOT NULL COMMENT '转账业务单号',
 request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
 from_wallet_id BIGINT NOT NULL COMMENT '转出钱包编号',
 from_member_id BIGINT NOT NULL COMMENT '转出会员编号',
 to_wallet_id BIGINT NOT NULL COMMENT '转入钱包编号',
 to_member_id BIGINT NOT NULL COMMENT '转入会员编号',
 amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
 remark VARCHAR(128) NULL COMMENT '备注说明',
 status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '转账状态：SUCCESS成功，同步转出转入',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_wallet_transfer_no(transfer_no),
 UNIQUE KEY uk_wallet_transfer_request(request_no),
 KEY idx_wallet_transfer_from(from_member_id,id), KEY idx_wallet_transfer_to(to_member_id,id),
 CONSTRAINT fk_wallet_transfer_from FOREIGN KEY(from_wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT fk_wallet_transfer_to FOREIGN KEY(to_wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_transfer_amount CHECK(amount>0),
 CONSTRAINT chk_wallet_transfer_distinct CHECK(from_member_id<>to_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包转账';
