CREATE TABLE IF NOT EXISTS trade_payment (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 payment_no VARCHAR(40) NOT NULL COMMENT '支付业务单号',
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 channel VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
 app_key VARCHAR(64) NULL COMMENT '支付应用标识',
 channel_trade_no VARCHAR(128) NULL COMMENT '渠道支付流水号',
 amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
 status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '支付状态：WAITING待支付、SUCCESS支付成功、CLOSED已关闭',
 notify_id VARCHAR(128) NULL COMMENT '支付通知唯一标识',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_payment_no(payment_no), UNIQUE KEY uk_trade_payment_order_channel(order_no,channel),
 UNIQUE KEY uk_trade_payment_notify(notify_id), KEY idx_trade_payment_status(status,create_time),
 KEY idx_trade_payment_app(app_key,channel),
 CONSTRAINT chk_trade_payment_amount CHECK(amount>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

