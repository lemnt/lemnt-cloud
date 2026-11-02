-- 活动订单售后回调去重；同一退款单号只处理一次。
CREATE TABLE mall_promotion_order_refund (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 refund_no VARCHAR(64) NOT NULL COMMENT '退款业务单号',
 quantity INT NOT NULL COMMENT '商品数量',
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 UNIQUE KEY uk_promotion_refund_no(refund_no),
 KEY idx_promotion_refund_order(order_no),
 CONSTRAINT chk_promotion_refund_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '活动订单退款幂等记录';
