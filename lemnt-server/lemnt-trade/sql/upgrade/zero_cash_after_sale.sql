-- 0元积分兑换订单售后：无支付单，退款金额为0，积分由权益回退链路返还。
ALTER TABLE trade_after_sale MODIFY payment_no VARCHAR(40) NULL,
    DROP CHECK chk_trade_refund_amount,
    ADD CONSTRAINT chk_trade_refund_amount CHECK(amount>=0);
ALTER TABLE trade_payment_refund MODIFY payment_no VARCHAR(40) NULL,
    DROP CHECK chk_payment_refund_amount,
    ADD CONSTRAINT chk_payment_refund_amount CHECK(amount>=0);
