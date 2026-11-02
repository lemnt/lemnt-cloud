-- 分销用户、佣金记录与提现；金额沿用项目元单位 DECIMAL(19,2)。
CREATE TABLE IF NOT EXISTS trade_brokerage_user (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '分销会员编号，关联会员账户',
 bind_user_id BIGINT NULL COMMENT '绑定的上级分销会员编号',
 bind_user_time DATETIME(3) NULL COMMENT '上级关系绑定时间',
 brokerage_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用分销资格',
 brokerage_time DATETIME(3) NULL COMMENT '分销资格开通时间',
 brokerage_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '可提现佣金余额，单位元',
 frozen_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '冻结佣金余额，单位元',
 KEY idx_brokerage_bind_user(bind_user_id),
 CONSTRAINT chk_brokerage_user_price CHECK(brokerage_price>=0 AND frozen_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销会员及佣金账户';

CREATE TABLE IF NOT EXISTS trade_brokerage_record (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '会员编号',
 biz_id VARCHAR(64) NOT NULL COMMENT '业务关联编号',
 biz_type INT NOT NULL COMMENT '佣金业务类型：1订单、2提现、3提现驳回、4订单退款扣回',
 title VARCHAR(100) NOT NULL COMMENT '标题',
 description VARCHAR(255) NULL COMMENT '描述说明',
 price DECIMAL(19,2) NOT NULL COMMENT '本次佣金变更金额，扣减为负，单位元',
 total_price DECIMAL(19,2) NOT NULL COMMENT '变更后可用与冻结佣金合计，单位元',
 status INT NOT NULL COMMENT '佣金状态：0待结算、1已结算、2已取消',
 frozen_days INT NULL COMMENT '佣金冻结天数',
 unfreeze_time DATETIME(3) NULL COMMENT '佣金计划解冻时间',
 source_user_level INT NULL COMMENT '来源分销层级，1一级、2二级',
 source_user_id BIGINT NULL COMMENT '来源会员编号',
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 UNIQUE KEY uk_brokerage_record_biz(user_id,biz_type,biz_id),
 KEY idx_brokerage_record_user(user_id,id),
 KEY idx_brokerage_record_unfreeze(status,unfreeze_time),
 CONSTRAINT chk_brokerage_record_status CHECK(status IN (0,1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销佣金变更记录';

CREATE TABLE IF NOT EXISTS trade_brokerage_withdraw (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '会员编号',
 price DECIMAL(19,2) NOT NULL COMMENT '申请提现佣金，单位元',
 fee_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '手续费金额，单位元',
 total_price DECIMAL(19,2) NOT NULL COMMENT '扣除手续费后的实际到账金额，单位元',
 type INT NOT NULL COMMENT '提现方式：1钱包、2银行、3微信收款码、4支付宝收款码',
 user_name VARCHAR(64) NOT NULL COMMENT '提现收款人姓名',
 user_account VARCHAR(128) NOT NULL COMMENT '提现收款账号',
 qr_code_url VARCHAR(500) NULL COMMENT '收款二维码地址',
 bank_name VARCHAR(100) NULL COMMENT '开户银行名称',
 bank_address VARCHAR(255) NULL COMMENT '开户行地址',
 status INT NOT NULL COMMENT '提现状态：0待审核、10审核通过、11已到账、20审核拒绝',
 audit_reason VARCHAR(255) NULL COMMENT '审核说明',
 audit_time DATETIME(3) NULL COMMENT '审核时间',
 remark VARCHAR(255) NULL COMMENT '备注说明',
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 KEY idx_brokerage_withdraw_user(user_id,id),
 KEY idx_brokerage_withdraw_status(status,id),
 CONSTRAINT chk_brokerage_withdraw_price CHECK(price>0 AND fee_price>=0 AND fee_price<=price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销佣金提现申请';
