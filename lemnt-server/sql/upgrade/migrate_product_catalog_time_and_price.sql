-- 只在商品库中执行一次。先添加可空列，再设置新记录的默认值，既有行保留NULL。
ALTER TABLE product_spu ADD COLUMN create_time DATETIME NULL COMMENT 'SPU创建时间，历史未知为NULL';
ALTER TABLE product_spu MODIFY COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT 'SPU创建时间，历史未知为NULL';
ALTER TABLE product_spu
    ADD COLUMN browse_count BIGINT NOT NULL DEFAULT 0 COMMENT '本系统记录的有效商品详情浏览次数',
    ADD COLUMN spec_type BOOLEAN NULL COMMENT 'false单规格、true多规格，历史未知NULL',
    ADD COLUMN virtual_sales_count INT NULL COMMENT '虚拟展示销量，历史未知为NULL',
    ADD CONSTRAINT chk_spu_virtual_sales CHECK (virtual_sales_count >= 0);

-- 销售单价允许0元，负价格仍由数据库拒绝。
ALTER TABLE product_sku
    ADD COLUMN market_price DECIMAL(19,2) NULL COMMENT 'SKU划线价，单位元',
    ADD COLUMN cost_price DECIMAL(19,2) NULL COMMENT 'SKU成本价，单位元',
    ADD COLUMN bar_code VARCHAR(64) NULL COMMENT '商品条码，与SKU编码独立',
    ADD COLUMN sales_count BIGINT NOT NULL DEFAULT 0 COMMENT '切换后SKU支付确认累计件数，历史无SKU口径，不做回填',
    DROP CHECK chk_mall_product_price,
    ADD CONSTRAINT chk_mall_product_price CHECK (price >= 0);
