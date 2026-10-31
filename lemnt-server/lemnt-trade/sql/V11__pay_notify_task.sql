-- 支付结果下游派发任务与每次尝试日志；type=PAY/REFUND，唯一键保证同一业务单只入队一次。
CREATE TABLE IF NOT EXISTS trade_pay_notify_task (
 id BIGINT NOT NULL AUTO_INCREMENT,
 type VARCHAR(16) NOT NULL,
 biz_no VARCHAR(40) NOT NULL,
 order_no VARCHAR(40) NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 notify_times INT NOT NULL DEFAULT 0,
 max_notify_times INT NOT NULL DEFAULT 8,
 next_notify_time DATETIME NULL,
 last_execute_time DATETIME NULL,
 last_response VARCHAR(500) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_notify_biz(type,biz_no),
 KEY idx_trade_pay_notify_due(status,next_notify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果下游派发任务';

CREATE TABLE IF NOT EXISTS trade_pay_notify_log (
 id BIGINT NOT NULL AUTO_INCREMENT,
 task_id BIGINT NOT NULL,
 notify_times INT NOT NULL,
 status VARCHAR(20) NOT NULL,
 response VARCHAR(500) NULL,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), KEY idx_trade_pay_notify_log_task(task_id),
 CONSTRAINT fk_trade_pay_notify_log_task FOREIGN KEY(task_id) REFERENCES trade_pay_notify_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果派发尝试日志';
