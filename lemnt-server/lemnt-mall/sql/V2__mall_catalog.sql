-- Additive migration. Back up and test on MySQL before production use.
-- Existing mall_product_sku IDs remain the SKU IDs referenced by orders/reservations.
CREATE TABLE mall_product_category (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 parent_id BIGINT NULL COMMENT '上级分类编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 pic_url VARCHAR(500) NULL COMMENT '图片地址',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '分类状态：ENABLED启用、DISABLED停用',
 CONSTRAINT fk_mall_category_parent FOREIGN KEY(parent_id) REFERENCES mall_product_category(id),
 CONSTRAINT chk_mall_category_status CHECK(status IN ('ENABLED','DISABLED')),
 KEY idx_mall_category_parent(parent_id,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品分类';
CREATE TABLE mall_product_brand (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 pic_url VARCHAR(500) NULL COMMENT '图片地址',
 description VARCHAR(2000) NULL COMMENT '描述说明',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '品牌状态：ENABLED启用、DISABLED停用',
 CONSTRAINT chk_mall_brand_status CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品品牌';
CREATE TABLE mall_product_spu (
 keyword VARCHAR(255) NULL COMMENT '搜索关键字',
 introduction VARCHAR(128) NULL COMMENT '简介',
 slider_pic_urls LONGTEXT NULL COMMENT '轮播图片地址JSON数组',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 delivery_types LONGTEXT NULL COMMENT '支持的配送方式JSON数组：1快递、2自提',
 delivery_template_id BIGINT NULL COMMENT '运费模板编号',
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 code VARCHAR(64) NOT NULL COMMENT '业务编码',
 name VARCHAR(200) NOT NULL COMMENT '名称',
 category_id BIGINT NOT NULL COMMENT '商品分类编号',
 brand_id BIGINT NOT NULL COMMENT '品牌编号',
 description TEXT NULL COMMENT '描述说明',
 image VARCHAR(500) NULL COMMENT '商品图片地址',
 status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF' COMMENT '商品状态：ON_SHELF上架、OFF_SHELF下架、RECYCLED回收站、DELETED删除',
 UNIQUE KEY uk_mall_spu_code(code),
 KEY idx_mall_spu_status(status,id),
 CONSTRAINT fk_mall_spu_category FOREIGN KEY(category_id) REFERENCES mall_product_category(id),
 CONSTRAINT fk_mall_spu_brand FOREIGN KEY(brand_id) REFERENCES mall_product_brand(id),
 CONSTRAINT chk_mall_spu_status CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品SPU基础信息';
ALTER TABLE mall_product_sku ADD COLUMN spu_id BIGINT NULL COMMENT '商品SPU编号';
ALTER TABLE mall_product_sku ADD COLUMN variant_key VARCHAR(64) NULL COMMENT 'SKU规格组合唯一摘要';
ALTER TABLE mall_product_sku ADD CONSTRAINT fk_mall_product_spu FOREIGN KEY(spu_id) REFERENCES mall_product_spu(id);
ALTER TABLE mall_product_sku ADD CONSTRAINT uk_mall_sku_variant UNIQUE(spu_id,variant_key);
ALTER TABLE mall_product_sku ADD COLUMN specs LONGTEXT NULL COMMENT '规格JSON数组，元素为specId/specName/specValueId/specValueName';
-- 独立库存调整台账已移除，SKU库存随SPU保存。
