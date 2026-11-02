-- 支付退款交易独立于售后申请；每个售后单只允许一个固定的渠道退款幂等编号。
CREATE TABLE trade_payment_refund (
 refund_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '退款业务单号',
 after_sale_no VARCHAR(40) NOT NULL COMMENT '售后业务单号',
 payment_no VARCHAR(40) NULL COMMENT '支付业务单号',
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 channel VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
 amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
 status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '退款状态：WAITING待退款、SUCCESS成功、FAILED失败',
 channel_refund_no VARCHAR(128) NULL COMMENT '渠道退款流水号',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 UNIQUE KEY uk_payment_refund_after_sale(after_sale_no),
 KEY idx_payment_refund_payment(payment_no),
 KEY idx_payment_refund_status_time(status,create_time),
 CONSTRAINT fk_payment_refund_after_sale FOREIGN KEY(after_sale_no) REFERENCES trade_after_sale(refund_no),
 CONSTRAINT fk_payment_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
 CONSTRAINT fk_payment_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
 CONSTRAINT chk_payment_refund_amount CHECK(amount>=0),
 CONSTRAINT chk_payment_refund_status CHECK(status IN ('WAITING','SUCCESS','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后原路支付退款交易';
