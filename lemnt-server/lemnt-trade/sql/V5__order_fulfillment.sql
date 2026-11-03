-- Stop old trade workers first; review historic paid/stock integrity before applying backfill.
CREATE TABLE trade_inventory_confirmation (
 order_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '交易订单业务单号',
 confirmed_at DATETIME NOT NULL COMMENT '库存确认时间',
 CONSTRAINT fk_trade_inventory_confirmation_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '交易订单库存确认记录';

CREATE TABLE trade_shipment (
 order_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '交易订单业务单号',
 request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
 fingerprint VARCHAR(64) NOT NULL COMMENT '请求内容指纹，用于幂等核对',
 carrier_code VARCHAR(40) NOT NULL COMMENT '快递公司编码',
 tracking_no VARCHAR(100) NOT NULL COMMENT '快递运单号',
 recipient_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
 phone VARCHAR(32) NOT NULL COMMENT '联系电话',
 address VARCHAR(500) NOT NULL COMMENT '收件详细地址快照',
 shipped_at DATETIME NOT NULL COMMENT '发货时间',
 received_at DATETIME NULL COMMENT '签收时间',
 CONSTRAINT fk_trade_shipment_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '订单发货与收件快照';
CREATE TABLE trade_after_sale (
 refund_discount_amount DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次商品优惠归因，非额外退现金，单位元',
 refund_delivery_discount_price DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次运费优惠归因，非额外退现金，单位元',
 return_use_point BIGINT NULL COMMENT '生成退单时冻结的应返使用积分，实际到账以账户流水为准',
 deduct_give_point BIGINT NULL COMMENT '生成退单时冻结的应回收赠送积分，实际扣回以账户流水为准',
 refund_rule_version INT NULL COMMENT '退款分摊规则版本，1为现金单件基准末次尾差及积分累计比例，历史未知为NULL',
 CONSTRAINT chk_trade_after_sale_promotion_amount CHECK(
   (refund_discount_amount IS NULL OR refund_discount_amount>=0) AND
   (refund_delivery_discount_price IS NULL OR refund_delivery_discount_price>=0)),
 CONSTRAINT chk_trade_after_sale_points CHECK(
   (return_use_point IS NULL OR return_use_point>=0) AND (deduct_give_point IS NULL OR deduct_give_point>=0)),

 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 refund_no VARCHAR(40) NOT NULL COMMENT '售后业务单号',
 order_id BIGINT NULL COMMENT '交易订单主键编号',
 sku_id BIGINT NULL COMMENT '商品SKU编号',
 spu_id BIGINT NULL COMMENT '商品SPU编号',
 spu_name VARCHAR(200) NULL COMMENT '商品SPU名称快照',
 specs LONGTEXT NULL COMMENT '申请时原订单规格JSON快照，历史未知为NULL，不读取当前商品回填',
 pic_url VARCHAR(1024) NULL COMMENT '图片地址',
 refund_time DATETIME NULL COMMENT '退款完成时间',
 audit_user_id BIGINT NULL COMMENT '审核管理员编号',
 create_by VARCHAR(64) NULL COMMENT '创建人',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人',
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
 UNIQUE KEY uk_after_sale_no(refund_no),
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
 member_id BIGINT NOT NULL COMMENT '会员编号',
 payment_no VARCHAR(40) NULL COMMENT '支付业务单号',
 amount DECIMAL(19,2) NOT NULL COMMENT '申请退款金额，单位元',
 reason VARCHAR(500) NOT NULL COMMENT '业务原因说明',
 status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '售后状态：REQUESTED待审核、REJECTED拒绝、AWAITING_RETURN待寄回、RETURNED已寄回、WAIT_REFUND待退款、RETURN_REJECTED拒收、REFUNDING退款中、REFUNDED已退款、CANCELLED取消',
 active_order_no VARCHAR(40) NULL COMMENT '处理中售后关联订单号，用于限制同订单活动售后',
 decision_reason VARCHAR(500) NULL COMMENT '售后处理说明',
 order_item_id BIGINT NULL COMMENT '交易订单明细主键编号',
 quantity INT NULL COMMENT '商品数量',
 type VARCHAR(20) NOT NULL DEFAULT 'REFUND_ONLY' COMMENT '售后类型：REFUND_ONLY仅退款、RETURN_REFUND退货退款',
 return_carrier_code VARCHAR(40) NULL COMMENT '退货快递公司编码',
 return_tracking_no VARCHAR(100) NULL COMMENT '退货快递运单号',
 return_shipped_at DATETIME NULL COMMENT '会员寄出退货时间',
 return_received_at DATETIME NULL COMMENT '商家收到退货时间',
 requested_at DATETIME NOT NULL COMMENT '售后申请时间',
 decided_at DATETIME NULL COMMENT '售后审核决定时间',
 UNIQUE KEY uk_trade_refund_order_request(order_no,request_no),
 UNIQUE KEY uk_trade_refund_active(active_order_no),
 KEY idx_trade_refund_status_time(status,requested_at),
 CONSTRAINT fk_trade_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
 CONSTRAINT fk_trade_refund_item FOREIGN KEY(order_item_id) REFERENCES trade_order_item(id),
 CONSTRAINT chk_trade_refund_item CHECK((order_item_id IS NULL AND quantity IS NULL)
   OR (order_item_id IS NOT NULL AND quantity IS NOT NULL AND quantity>0)),
 CONSTRAINT chk_trade_refund_type CHECK(type IN ('REFUND_ONLY','RETURN_REFUND')),
 CONSTRAINT fk_trade_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
 CONSTRAINT chk_trade_refund_amount CHECK(amount>=0),
 CONSTRAINT chk_trade_refund_status CHECK(status IN ('REQUESTED','REJECTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','RETURN_REJECTED','REFUNDING','REFUNDED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '订单售后申请及商品快照';
