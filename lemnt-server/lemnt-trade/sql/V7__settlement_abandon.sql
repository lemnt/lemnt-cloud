CREATE TABLE trade_settlement_abandon (
 member_id BIGINT NOT NULL,
 settlement_id VARCHAR(36) NOT NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(member_id,settlement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
