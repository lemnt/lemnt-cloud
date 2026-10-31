-- 活动订单商品行：多商品零元订单按行保存积分与退款数量。
CREATE TABLE mall_promotion_order_item (
 id BIGINT NOT NULL PRIMARY KEY,
 order_no VARCHAR(40) NOT NULL,
 sku_id BIGINT NOT NULL,
 quantity INT NOT NULL,
 use_point BIGINT NOT NULL DEFAULT 0,
 refunded_quantity INT NOT NULL DEFAULT 0,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_promotion_order_item(order_no,sku_id),
 KEY idx_promotion_order_item_sku(sku_id),
 CONSTRAINT fk_promotion_order_item_order FOREIGN KEY(order_no) REFERENCES mall_promotion_order(order_no),
 CONSTRAINT chk_promotion_order_item_quantity CHECK(quantity>0),
 CONSTRAINT chk_promotion_order_item_point CHECK(use_point>=0),
 CONSTRAINT chk_promotion_order_item_refunded CHECK(refunded_quantity>=0 AND refunded_quantity<=quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
