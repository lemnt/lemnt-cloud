-- 在支付库执行。历史支付、退款的应用通知地址保持 NULL。
ALTER TABLE pay_payment ADD COLUMN notify_url VARCHAR(255) NULL COMMENT '下单时的应用支付通知地址';
ALTER TABLE pay_payment_refund ADD COLUMN notify_url VARCHAR(255) NULL COMMENT '创建时的应用退款通知地址';
