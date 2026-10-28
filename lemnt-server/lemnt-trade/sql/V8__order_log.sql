-- 订单真实操作记录；不伪造迁移前的历史日志。
CREATE TABLE trade_order_log (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 user_type INT NOT NULL,
 order_id BIGINT NOT NULL,
 before_status INT NULL,
 after_status INT NOT NULL,
 operate_type INT NOT NULL,
 content VARCHAR(2000) NOT NULL,
 create_by VARCHAR(64) NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL,
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 deleted BOOLEAN NOT NULL DEFAULT FALSE,
 KEY idx_trade_order_log_order(order_id,id),
 CONSTRAINT fk_trade_order_log_order FOREIGN KEY(order_id) REFERENCES trade_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志';
