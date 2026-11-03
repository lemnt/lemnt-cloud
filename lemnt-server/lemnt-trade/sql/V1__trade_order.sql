CREATE TABLE IF NOT EXISTS trade_order (
 original_amount DECIMAL(19,2) NULL COMMENT '下单时商品原价合计，单位元，历史未知为NULL',
 discount_amount DECIMAL(19,2) NULL COMMENT '商品优惠总额，含实际积分抵现金额，单位元，历史未知为NULL',
 delivery_original_price DECIMAL(19,2) NULL COMMENT '运费减免前应收运费，单位元，历史未知为NULL',
 delivery_discount_price DECIMAL(19,2) NULL COMMENT '运费优惠金额，单位元，历史未知为NULL',
 adjustment_amount DECIMAL(19,2) NULL COMMENT '人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
 pricing_snapshot_status VARCHAR(20) NOT NULL DEFAULT 'LEGACY_PARTIAL' COMMENT '金额快照状态：COMPLETE完整、LEGACY_PARTIAL历史不完整',
 pricing_snapshot_version INT NOT NULL DEFAULT 0 COMMENT '金额快照版本，历史未记录为0，支付单生成前调价递增',
 CONSTRAINT chk_trade_order_pricing_status CHECK(pricing_snapshot_status IN ('COMPLETE','LEGACY_PARTIAL')),
 CONSTRAINT chk_trade_order_discount CHECK(discount_amount IS NULL OR discount_amount>=0),
 CONSTRAINT chk_trade_order_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price)),
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',member_id BIGINT NOT NULL COMMENT '会员编号',total_amount DECIMAL(19,2) NOT NULL COMMENT '订单应付总金额，单位元',use_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单使用积分数',give_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单赠送积分数',address_id BIGINT NULL COMMENT '会员收货地址编号',delivery_type INT NULL COMMENT '配送方式：1快递、2自提',receiver_name VARCHAR(100) NULL COMMENT '收件人姓名快照',receiver_mobile VARCHAR(32) NULL COMMENT '收件手机号码快照',receiver_area_id BIGINT NULL COMMENT '收件地区编号快照',receiver_detail_address VARCHAR(500) NULL COMMENT '收件详细地址快照',delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '运费金额，单位元',status VARCHAR(32) NOT NULL COMMENT '订单状态：RESERVING预占中、PENDING_PAYMENT待支付、PAID已支付、SHIPPED已发货、COMPLETED已完成、CLOSED已关闭、REFUNDED已退款',comment_status BOOLEAN NULL COMMENT '是否已评价，历史未知可为空',finish_time DATETIME NULL COMMENT '订单完成时间',receive_time DATETIME NULL COMMENT '订单确认收货时间',remark VARCHAR(500) NULL COMMENT '备注说明',create_by VARCHAR(64) NULL COMMENT '创建人',create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',update_by VARCHAR(64) NULL COMMENT '修改人',update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',PRIMARY KEY(id),UNIQUE KEY uk_trade_order_no(order_no),KEY idx_trade_order_member_created(member_id,create_time),KEY idx_trade_order_status_created(status,create_time),CONSTRAINT chk_trade_order_total CHECK(total_amount>=0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单';
CREATE TABLE IF NOT EXISTS trade_order_item (
 discount_amount DECIMAL(19,2) NULL COMMENT '本行商品优惠合计，单位元，历史未知为NULL',
 delivery_original_price DECIMAL(19,2) NULL COMMENT '分摊至本行的原始应收运费，单位元，历史未知为NULL',
 delivery_discount_price DECIMAL(19,2) NULL COMMENT '分摊至本行的运费优惠，单位元，历史未知为NULL',
 adjustment_amount DECIMAL(19,2) NULL COMMENT '本行人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
 use_point BIGINT NULL COMMENT '分摊至本行的使用积分数量，历史未知为NULL',
 give_point BIGINT NULL COMMENT '分摊至本行的赠送积分数量，历史未知为NULL',
 refunded_quantity INT NULL COMMENT '已成功退款数量，售后首次成功同事务增加，历史未知为NULL',
 CONSTRAINT chk_trade_item_discount CHECK(discount_amount IS NULL OR (discount_amount>=0 AND discount_amount<=line_amount)),
 CONSTRAINT chk_trade_item_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price)),
 CONSTRAINT chk_trade_item_points CHECK((use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0)),
 CONSTRAINT chk_trade_item_refund_quantity CHECK(
   refunded_quantity IS NULL OR (refunded_quantity>=0 AND refunded_quantity<=quantity)),
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',order_id BIGINT NOT NULL COMMENT '交易订单主键编号',comment_status BOOLEAN NULL COMMENT '是否已评价，历史未知可为空',sku_id BIGINT NOT NULL COMMENT '商品SKU编号',spu_id BIGINT NULL COMMENT '商品SPU编号',spu_name VARCHAR(200) NULL COMMENT '商品SPU名称快照',specs LONGTEXT NULL COMMENT '下单时规格JSON快照；未知为NULL、已知无规格为[]；元素specId/specName/specValueId/specValueName',pic_url VARCHAR(1024) NULL COMMENT '图片地址',product_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码快照',product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照',unit_price DECIMAL(19,2) NOT NULL COMMENT '商品单价，单位元',quantity INT NOT NULL COMMENT '商品数量',line_amount DECIMAL(19,2) NOT NULL COMMENT '商品行原价合计，单位元',pay_amount DECIMAL(19,2) NULL COMMENT '商品行实付金额，单位元',delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '分摊至商品行的运费，单位元',create_by VARCHAR(64) NULL COMMENT '创建人',create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',update_by VARCHAR(64) NULL COMMENT '修改人',update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',PRIMARY KEY(id),KEY idx_trade_order_item_order(order_id),CONSTRAINT fk_trade_order_item_order FOREIGN KEY(order_id) REFERENCES trade_order(id),CONSTRAINT chk_trade_order_item_quantity CHECK(quantity>0)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单明细';