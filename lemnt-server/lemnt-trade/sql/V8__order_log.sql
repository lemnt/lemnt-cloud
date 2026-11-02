-- 订单真实操作记录；不伪造迁移前的历史日志。
CREATE TABLE trade_order_log (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
 user_id BIGINT NOT NULL COMMENT '操作人编号',
 user_type INT NOT NULL COMMENT '操作人类型：0系统、1会员、2后台管理员',
 order_id BIGINT NOT NULL COMMENT '交易订单主键编号',
 before_status INT NULL COMMENT '操作前订单状态数值编码',
 after_status INT NOT NULL COMMENT '操作后订单状态数值编码',
 operate_type INT NOT NULL COMMENT '订单操作类型编码',
 content VARCHAR(2000) NOT NULL COMMENT '内容',
 create_by VARCHAR(64) NULL COMMENT '创建人',
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_by VARCHAR(64) NULL COMMENT '修改人',
 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
 KEY idx_trade_order_log_order(order_id,id),
 CONSTRAINT fk_trade_order_log_order FOREIGN KEY(order_id) REFERENCES trade_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志';
