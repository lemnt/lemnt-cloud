CREATE TABLE IF NOT EXISTS trade_payment (
 id BIGINT NOT NULL AUTO_INCREMENT,
 payment_no VARCHAR(40) NOT NULL,
 order_no VARCHAR(40) NOT NULL,
 channel VARCHAR(32) NOT NULL,
 channel_trade_no VARCHAR(128) NULL,
 amount DECIMAL(19,2) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
 notify_id VARCHAR(128) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_payment_no(payment_no), UNIQUE KEY uk_trade_payment_order_channel(order_no,channel),
 UNIQUE KEY uk_trade_payment_notify(notify_id), KEY idx_trade_payment_status(status,create_time),
 CONSTRAINT chk_trade_payment_amount CHECK(amount>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

