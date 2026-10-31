-- 仅用于尚无以下字段的存量库；执行前检查 information_schema，禁止重复执行。
-- 历史行实付金额保留 NULL，由历史比例算法退款；不可伪造为商品原价。
ALTER TABLE trade_order_item ADD COLUMN pay_amount DECIMAL(19,2) NULL COMMENT '商品行实付金额';
ALTER TABLE trade_order ADD COLUMN use_point BIGINT NOT NULL DEFAULT 0 COMMENT '下单使用积分',
 ADD COLUMN give_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单赠送积分';
