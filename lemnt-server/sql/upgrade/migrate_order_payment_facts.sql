-- 仅在订单库执行。旧消费凭证仍供历史重放核验，未确认的历史资金编号不回填。
ALTER TABLE order_info
    ADD COLUMN payment_prepared_amount DECIMAL(19,2) NULL COMMENT '发起渠道支付前冻结的应付金额，元',
    ADD COLUMN confirmed_payment_no VARCHAR(40) NULL COMMENT '核验后确认支付的资金单号，历史与零元单可空',
    ADD CONSTRAINT chk_order_payment_prepared_amount CHECK(
        payment_prepared_amount IS NULL OR (payment_prepared_amount > 0 AND payment_prepared_amount = total_amount));
