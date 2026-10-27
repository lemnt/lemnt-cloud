CREATE TABLE mall_settlement_order (
 settlement_id VARCHAR(36) NOT NULL PRIMARY KEY,
 member_id BIGINT NOT NULL,
 order_no VARCHAR(40) NOT NULL,
 UNIQUE KEY uk_settlement_order(order_no),
 KEY idx_settlement_member(member_id,settlement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
