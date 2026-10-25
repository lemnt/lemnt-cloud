CREATE TABLE trade_settlement_order (
 settlement_id VARCHAR(36) NOT NULL PRIMARY KEY,
 member_id BIGINT NOT NULL,
 order_no VARCHAR(40) NOT NULL,
 UNIQUE KEY uk_settlement_order(order_no),
 KEY idx_settlement_member(member_id,settlement_id),
 CONSTRAINT fk_settlement_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
