-- 商品库升级。历史销量沿用旧支付消费记录核验，保留原预占行的 NULL。
ALTER TABLE product_stock_reservation
    ADD COLUMN sales_recorded BOOLEAN NULL COMMENT '本次支付销量已入账为1，历史与未入账记录为空',
    ADD CONSTRAINT chk_stock_reservation_recorded_sales CHECK (
        sales_recorded IS NULL OR (sales_recorded=TRUE AND status='CONFIRMED'));
