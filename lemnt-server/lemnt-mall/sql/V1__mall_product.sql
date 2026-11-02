CREATE TABLE IF NOT EXISTS mall_product_sku (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 product_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码快照',
 name VARCHAR(200) NOT NULL COMMENT '名称',
 description TEXT NULL COMMENT '描述说明',
 main_image VARCHAR(500) NULL COMMENT '商品主图地址',
 weight DECIMAL(18,6) NULL COMMENT '单件重量，千克',volume DECIMAL(18,6) NULL COMMENT '单件体积，立方米',price DECIMAL(19,2) NOT NULL COMMENT 'SKU销售单价，单位元',
 stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
 status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF' COMMENT '商品状态：ON_SHELF上架、OFF_SHELF下架',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_mall_product_code(product_code), KEY idx_mall_product_status(status),
 CONSTRAINT chk_mall_product_price CHECK(price>0), CONSTRAINT chk_mall_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品';

CREATE TABLE IF NOT EXISTS mall_product_stock_reservation (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 reservation_no VARCHAR(40) NOT NULL COMMENT '库存预占业务编号',
 source_no VARCHAR(40) NULL COMMENT '库存预占来源业务单号',
 product_id BIGINT NOT NULL COMMENT '商品SKU编号',
 quantity INT NOT NULL COMMENT '商品数量',
 status VARCHAR(20) NOT NULL DEFAULT 'RESERVED' COMMENT '库存预占状态：RESERVED已预占、CONFIRMED已确认、RELEASED已释放',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id),
 UNIQUE KEY uk_mall_stock_reservation(reservation_no, product_id),
 KEY idx_mall_stock_reservation_status(status, update_time),
 CONSTRAINT chk_mall_stock_reservation_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占幂等台账';