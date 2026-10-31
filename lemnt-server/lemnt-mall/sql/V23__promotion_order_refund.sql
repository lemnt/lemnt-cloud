-- 活动订单售后回调去重；同一退款单号只处理一次。
CREATE TABLE mall_promotion_order_refund (
 id BIGINT NOT NULL PRIMARY KEY,
 order_no VARCHAR(40) NOT NULL,
 refund_no VARCHAR(64) NOT NULL,
 quantity INT NOT NULL,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_promotion_refund_no(refund_no),
 KEY idx_promotion_refund_order(order_no),
 CONSTRAINT chk_promotion_refund_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
