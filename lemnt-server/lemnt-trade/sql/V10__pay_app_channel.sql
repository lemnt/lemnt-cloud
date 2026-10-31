-- 支付应用与支付渠道配置；渠道配置以 JSON 文本保存，不存明文密钥之外的结构扩展。
-- 状态口径沿用项目：0 启用、1 停用。
CREATE TABLE IF NOT EXISTS trade_pay_app (
 id BIGINT NOT NULL AUTO_INCREMENT,
 app_key VARCHAR(64) NOT NULL,
 name VARCHAR(64) NOT NULL,
 status TINYINT NOT NULL DEFAULT 0,
 remark VARCHAR(255) NULL,
 order_notify_url VARCHAR(255) NULL,
 refund_notify_url VARCHAR(255) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 deleted TINYINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_app_key(app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付应用';

CREATE TABLE IF NOT EXISTS trade_pay_channel (
 id BIGINT NOT NULL AUTO_INCREMENT,
 app_id BIGINT NOT NULL,
 code VARCHAR(32) NOT NULL,
 status TINYINT NOT NULL DEFAULT 0,
 fee_rate DECIMAL(6,2) NULL,
 remark VARCHAR(255) NULL,
 config TEXT NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 deleted TINYINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_channel(app_id,code), KEY idx_trade_pay_channel_app(app_id),
 CONSTRAINT fk_trade_pay_channel_app FOREIGN KEY(app_id) REFERENCES trade_pay_app(id),
 CONSTRAINT chk_trade_pay_channel_fee CHECK(fee_rate IS NULL OR (fee_rate>=0 AND fee_rate<=100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道配置';
