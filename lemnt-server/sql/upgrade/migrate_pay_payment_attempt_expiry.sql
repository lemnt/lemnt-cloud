-- 暂停旧支付写入和关闭调度后，在已有支付库执行一次。历史交易编号不变，原尝试不补造有效期。
ALTER TABLE pay_payment
    ADD COLUMN attempt_no INT NOT NULL DEFAULT 1 COMMENT '同一业务单和渠道的支付尝试序号',
    ADD COLUMN expires_at_epoch_second BIGINT NULL COMMENT '尝试到期时间，Unix秒，历史记录为空',
    DROP INDEX uk_trade_payment_order_channel,
    ADD UNIQUE KEY uk_trade_payment_order_attempt (order_no, channel, attempt_no),
    ADD KEY idx_pay_payment_expiry (channel, status, expires_at_epoch_second);
