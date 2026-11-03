-- 支付结果下游派发任务与每次尝试日志；type=PAY/REFUND/AFTER_SALE，唯一键保证同一业务单只入队一次。
CREATE TABLE IF NOT EXISTS trade_pay_notify_task (
 execute_version BIGINT NOT NULL DEFAULT 0 COMMENT '单调执行版本，领取和人工重置均递增，完成时按版本CAS',
 lease_until DATETIME NULL COMMENT '当前执行租约到期时间，使用数据库时间，过期允许重新领取',

 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 type VARCHAR(16) NOT NULL COMMENT '派发业务类型：PAY支付、REFUND历史退款、AFTER_SALE售后权益',
 biz_no VARCHAR(40) NOT NULL COMMENT '业务单号',
 order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '派发状态：PENDING待派发、SUCCESS成功、FAILED失败待重试',
 notify_times INT NOT NULL DEFAULT 0 COMMENT '已派发尝试次数',
 max_notify_times INT NOT NULL DEFAULT 8 COMMENT '最大派发尝试次数',
 next_notify_time DATETIME NULL COMMENT '下次计划派发时间',
 last_execute_time DATETIME NULL COMMENT '最近一次派发执行时间',
 last_response VARCHAR(500) NULL COMMENT '最近一次派发结果摘要',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), UNIQUE KEY uk_trade_pay_notify_biz(type,biz_no),
 KEY idx_trade_pay_notify_due(status,next_notify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果下游派发任务';

CREATE TABLE IF NOT EXISTS trade_pay_notify_log (
 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
 task_id BIGINT NOT NULL COMMENT '支付结果派发任务编号',
 notify_times INT NOT NULL COMMENT '已派发尝试次数',
 status VARCHAR(20) NOT NULL COMMENT '本次派发结果状态',
 response VARCHAR(500) NULL COMMENT '本次派发结果摘要',
 create_by VARCHAR(64) NULL COMMENT '创建人', create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人', update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 PRIMARY KEY(id), KEY idx_trade_pay_notify_log_task(task_id),
 CONSTRAINT fk_trade_pay_notify_log_task FOREIGN KEY(task_id) REFERENCES trade_pay_notify_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果派发尝试日志';
