-- 仅对已有支付库执行一次。历史手续费没有原始快照，留空。
ALTER TABLE pay_payment
    ADD COLUMN channel_fee_rate DECIMAL(6,2) NULL COMMENT '支付成功时渠道费率，百分比',
    ADD COLUMN channel_fee_amount DECIMAL(19,2) NULL COMMENT '按费率计算的渠道手续费，单位元';
