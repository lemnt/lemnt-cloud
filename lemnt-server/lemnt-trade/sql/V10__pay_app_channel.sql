-- 支付应用与支付渠道配置；渠道配置以 JSON 文本保存，不存明文密钥之外的结构扩展。
-- 状态口径沿用项目：0 启用、1 停用。
CREATE TABLE IF NOT EXISTS trade_pay_app (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 app_key VARCHAR(64) NOT NULL COMMENT '支付应用标识',
 name VARCHAR(64) NOT NULL COMMENT '名称',
 status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 remark VARCHAR(255) NULL COMMENT '备注说明',
 order_notify_url VARCHAR(255) NULL COMMENT '支付成功业务通知地址',
 refund_notify_url VARCHAR(255) NULL COMMENT '退款业务通知地址',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_app_key(app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付应用';

CREATE TABLE IF NOT EXISTS trade_pay_channel (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 app_id BIGINT NOT NULL COMMENT '支付应用编号',
 code VARCHAR(32) NOT NULL COMMENT '业务编码',
 status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 fee_rate DECIMAL(6,2) NULL COMMENT '渠道手续费百分比',
 remark VARCHAR(255) NULL COMMENT '备注说明',
 config TEXT NULL COMMENT '支付渠道内部JSON配置，包含密钥且不对外明文返回',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_channel(app_id,code), KEY idx_trade_pay_channel_app(app_id),
 CONSTRAINT fk_trade_pay_channel_app FOREIGN KEY(app_id) REFERENCES trade_pay_app(id),
 CONSTRAINT chk_trade_pay_channel_fee CHECK(fee_rate IS NULL OR (fee_rate>=0 AND fee_rate<=100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道配置';
