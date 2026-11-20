-- 暂停支付服务写入和调度后执行。旧渠道支付编号、收款状态和退款关联保留。
CREATE TABLE IF NOT EXISTS pay_payment_order (
    order_no VARCHAR(40) NOT NULL COMMENT '商城或钱包充值原业务单号',
    payment_order_no VARCHAR(40) NOT NULL COMMENT '支付主单编号',
    member_id BIGINT NULL COMMENT '原会员编号，历史缺失留空',
    amount DECIMAL(19,2) NULL COMMENT '支付应付金额，单位元，历史不一致留空',
    status VARCHAR(24) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING待支付、SUCCESS已收款、CLOSED业务已关闭、RECONCILE_REQUIRED待对账',
    paid_payment_no VARCHAR(40) NULL COMMENT '首次确认成功的渠道尝试支付编号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY(order_no),
    UNIQUE KEY uk_pay_payment_order_no(payment_order_no),
    CONSTRAINT chk_pay_payment_order_status CHECK(status IN ('WAITING','SUCCESS','CLOSED','RECONCILE_REQUIRED')),
    CONSTRAINT chk_pay_payment_order_amount CHECK(amount IS NULL OR amount>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付主单';

INSERT INTO pay_payment_order
    (order_no, payment_order_no, member_id, amount, status, paid_payment_no, create_time)
SELECT order_no,
       CONCAT('PO', REPLACE(UUID(), '-', '')),
       CASE WHEN COUNT(DISTINCT member_id) = 1 AND COUNT(member_id) = COUNT(*) THEN MIN(member_id) END,
       CASE WHEN COUNT(DISTINCT amount) = 1 THEN MIN(amount) END,
       CASE
           WHEN COUNT(DISTINCT amount) <> 1 OR COUNT(DISTINCT member_id) > 1
                OR SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) > 1
               THEN 'RECONCILE_REQUIRED'
           WHEN SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) = 1 THEN 'SUCCESS'
           ELSE 'WAITING'
       END,
       CASE WHEN SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) = 1
            THEN MAX(CASE WHEN status = 'SUCCESS' THEN payment_no END) END,
       MIN(create_time)
FROM pay_payment
GROUP BY order_no;

SELECT order_no, payment_order_no, amount, status, paid_payment_no
FROM pay_payment_order
WHERE status = 'RECONCILE_REQUIRED';
