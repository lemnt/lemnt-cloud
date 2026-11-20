-- 存量支付库单次切流。停止旧支付写入方、中继和旧通知调度后单独执行下面的预检。
-- 查询必须返回 0 行。存在未匹配或任务归属冲突时先对账。
SELECT o.event_no, o.event_type, o.business_key, o.aggregate_no
FROM pay_message_outbox o
LEFT JOIN pay_payment p ON o.event_type = 'PAY_SUCCESS'
    AND p.payment_no = o.business_key AND p.order_no = o.aggregate_no AND p.status = 'SUCCESS'
LEFT JOIN pay_payment_refund r ON o.event_type = 'PAY_REFUND_SUCCESS'
    AND r.refund_no = o.business_key AND r.order_no = o.aggregate_no AND r.status = 'SUCCESS'
LEFT JOIN pay_notify_task t ON t.type = CASE o.event_type WHEN 'PAY_SUCCESS' THEN 'PAY' ELSE 'REFUND' END
    AND t.biz_no = o.business_key
WHERE o.status IN ('PENDING', 'SENDING', 'FAILED')
    AND ((p.payment_no IS NULL AND r.refund_no IS NULL)
        OR (t.id IS NOT NULL AND t.order_no <> o.aggregate_no));

-- 旧 SENT 消息由订单兼容消费者继续处理。本脚本保留原出站记录和原通知日志。
START TRANSACTION;

INSERT INTO pay_notify_task (type, biz_no, order_no, status, next_notify_time)
SELECT 'PAY', p.payment_no, p.order_no, 'PENDING', CURRENT_TIMESTAMP
FROM pay_message_outbox o
JOIN pay_payment p ON p.payment_no = o.business_key AND p.order_no = o.aggregate_no AND p.status = 'SUCCESS'
WHERE o.event_type = 'PAY_SUCCESS' AND o.status IN ('PENDING', 'SENDING', 'FAILED')
ON DUPLICATE KEY UPDATE status = 'PENDING', notify_times = 0, next_notify_time = CURRENT_TIMESTAMP,
    execute_version = pay_notify_task.execute_version + 1, lease_until = NULL, last_response = NULL;

INSERT INTO pay_notify_task (type, biz_no, order_no, status, next_notify_time)
SELECT 'REFUND', r.refund_no, r.order_no, 'PENDING', CURRENT_TIMESTAMP
FROM pay_message_outbox o
JOIN pay_payment_refund r ON r.refund_no = o.business_key AND r.order_no = o.aggregate_no AND r.status = 'SUCCESS'
WHERE o.event_type = 'PAY_REFUND_SUCCESS' AND o.status IN ('PENDING', 'SENDING', 'FAILED')
ON DUPLICATE KEY UPDATE status = 'PENDING', notify_times = 0, next_notify_time = CURRENT_TIMESTAMP,
    execute_version = pay_notify_task.execute_version + 1, lease_until = NULL, last_response = NULL;

COMMIT;
