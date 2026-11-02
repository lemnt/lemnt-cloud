-- 活动订单商品行：多商品零元订单按行保存积分与退款数量。
CREATE TABLE mall_promotion_order_item (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
 quantity INT NOT NULL COMMENT '商品数量',
 use_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单使用积分数',
 refunded_quantity INT NOT NULL DEFAULT 0 COMMENT '累计已退商品数量',
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
 UNIQUE KEY uk_promotion_order_item(order_no,sku_id),
 KEY idx_promotion_order_item_sku(sku_id),
 CONSTRAINT fk_promotion_order_item_order FOREIGN KEY(order_no) REFERENCES mall_promotion_order(order_no),
 CONSTRAINT chk_promotion_order_item_quantity CHECK(quantity>0),
 CONSTRAINT chk_promotion_order_item_point CHECK(use_point>=0),
 CONSTRAINT chk_promotion_order_item_refunded CHECK(refunded_quantity>=0 AND refunded_quantity<=quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '活动订单商品及积分退款快照';
