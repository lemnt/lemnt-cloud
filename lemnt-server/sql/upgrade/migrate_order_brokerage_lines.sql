-- 仅在订单库执行。旧订单没有佣金行快照，继续使用原记录与扣回规则。
CREATE TABLE IF NOT EXISTS order_brokerage_line (
    order_item_id BIGINT NOT NULL PRIMARY KEY COMMENT '订单明细编号',
    order_id BIGINT NOT NULL COMMENT '订单编号',
    first_fixed DECIMAL(19,2) NULL COMMENT '建单一级每件固定佣金，元',
    second_fixed DECIMAL(19,2) NULL COMMENT '建单二级每件固定佣金，元',
    first_earned DECIMAL(19,2) NULL COMMENT '一级原行实发佣金，元；NULL未入账',
    second_earned DECIMAL(19,2) NULL COMMENT '二级原行实发佣金，元；NULL未入账',
    KEY idx_brokerage_line_order(order_id),
    CONSTRAINT fk_brokerage_line_item FOREIGN KEY(order_item_id) REFERENCES order_item(id),
    CONSTRAINT fk_brokerage_line_order FOREIGN KEY(order_id) REFERENCES order_info(id),
    CONSTRAINT chk_brokerage_line_fixed CHECK(
        (first_fixed IS NULL AND second_fixed IS NULL)
        OR (first_fixed IS NOT NULL AND second_fixed IS NOT NULL AND first_fixed>=0 AND second_fixed>=0)),
    CONSTRAINT chk_brokerage_line_earned CHECK(first_earned>=0 AND second_earned>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单行佣金配置和原入账快照';
