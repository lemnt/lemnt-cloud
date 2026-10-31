-- 支付退款交易独立于售后申请；每个售后单只允许一个固定的渠道退款幂等编号。
CREATE TABLE trade_payment_refund (
 refund_no VARCHAR(40) NOT NULL PRIMARY KEY,
 after_sale_no VARCHAR(40) NOT NULL,
 payment_no VARCHAR(40) NULL,
 order_no VARCHAR(40) NOT NULL,
 channel VARCHAR(32) NOT NULL,
 amount DECIMAL(19,2) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
 channel_refund_no VARCHAR(128) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 UNIQUE KEY uk_payment_refund_after_sale(after_sale_no),
 KEY idx_payment_refund_payment(payment_no),
 KEY idx_payment_refund_status_time(status,create_time),
 CONSTRAINT fk_payment_refund_after_sale FOREIGN KEY(after_sale_no) REFERENCES trade_after_sale(refund_no),
 CONSTRAINT fk_payment_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
 CONSTRAINT fk_payment_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
 CONSTRAINT chk_payment_refund_amount CHECK(amount>=0),
 CONSTRAINT chk_payment_refund_status CHECK(status IN ('WAITING','SUCCESS','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后原路支付退款交易';
