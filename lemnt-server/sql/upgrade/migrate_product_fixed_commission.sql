-- 仅在商品库执行一次，历史未配置的分销方式与金额保留空值。
ALTER TABLE product_spu ADD COLUMN sub_commission_type BOOLEAN NULL
    COMMENT '是否采用SKU固定分佣，历史未配置为NULL';
ALTER TABLE product_sku
    ADD COLUMN first_brokerage_price DECIMAL(19,2) NULL COMMENT '每件一级固定佣金，单位元',
    ADD COLUMN second_brokerage_price DECIMAL(19,2) NULL COMMENT '每件二级固定佣金，单位元',
    ADD CONSTRAINT chk_product_sku_fixed_commission CHECK(first_brokerage_price>=0 AND second_brokerage_price>=0);
