-- 充值套餐与充值单；金额单位为元。到账确认与退款由后台命令触发，真实渠道SDK后置。
CREATE TABLE IF NOT EXISTS trade_pay_recharge_package (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 name VARCHAR(64) NOT NULL COMMENT '名称',
 pay_price DECIMAL(19,2) NOT NULL COMMENT '实付金额，单位元',
 bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '充值赠送金额，单位元',
 status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_recharge_package_name(name),
 CONSTRAINT chk_recharge_package_pay CHECK(pay_price>0),
 CONSTRAINT chk_recharge_package_bonus CHECK(bonus_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值套餐';

CREATE TABLE IF NOT EXISTS trade_pay_wallet_recharge (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 recharge_no VARCHAR(40) NOT NULL COMMENT '充值业务单号',
 request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
 wallet_id BIGINT NOT NULL COMMENT '会员钱包编号',
 member_id BIGINT NOT NULL COMMENT '会员编号',
 package_id BIGINT NULL COMMENT '充值套餐编号',
 pay_price DECIMAL(19,2) NOT NULL COMMENT '实付金额，单位元',
 bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '充值赠送金额，单位元',
 total_price DECIMAL(19,2) NOT NULL COMMENT '实付与赠送合计入账金额，单位元',
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '充值状态：PENDING待支付、PAID已到账、REFUNDED已退款',
 channel_code VARCHAR(32) NULL COMMENT '支付渠道编码',
 paid_time DATETIME NULL COMMENT '支付到账时间',
 refunded_time DATETIME NULL COMMENT '退款完成时间',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_wallet_recharge_no(recharge_no),
 UNIQUE KEY uk_trade_wallet_recharge_request(request_no),
 KEY idx_trade_wallet_recharge_member(member_id,id),
 KEY idx_trade_wallet_recharge_status(status),
 CONSTRAINT fk_wallet_recharge_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
 CONSTRAINT chk_wallet_recharge_total CHECK(total_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包充值单';
