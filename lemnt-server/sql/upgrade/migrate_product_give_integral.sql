-- 只在商品库升级一次。保留历史未配置值，历史订单用原赠积分快照。
ALTER TABLE product_spu
    ADD COLUMN give_integral INT NULL COMMENT '每个订单行赠积分，历史未配置为NULL',
    ADD CONSTRAINT chk_spu_give_integral CHECK (give_integral >= 0);
