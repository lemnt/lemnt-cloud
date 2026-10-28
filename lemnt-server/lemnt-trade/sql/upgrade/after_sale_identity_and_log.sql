-- MySQL 8.0.16+，从原trade_refund_request升级。停相关写入、备份后审核执行一次。
-- 新库使用V5/V6/V9初始化，不执行本升级；未在真实数据库执行。
ALTER TABLE trade_payment_refund DROP FOREIGN KEY fk_payment_refund_after_sale;
RENAME TABLE trade_refund_request TO trade_after_sale;
ALTER TABLE trade_after_sale
 DROP PRIMARY KEY,
 DROP CHECK chk_trade_refund_status,
 ADD id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
 ADD order_id BIGINT NULL,
 ADD refund_time DATETIME NULL,
 ADD audit_user_id BIGINT NULL,
 ADD create_by VARCHAR(64) NULL,
 ADD create_time DATETIME NULL,
 ADD update_by VARCHAR(64) NULL,
 ADD update_time DATETIME NULL,
 ADD deleted BOOLEAN NOT NULL DEFAULT FALSE,
 ADD UNIQUE KEY uk_after_sale_no(refund_no),
 ADD CONSTRAINT chk_trade_refund_status CHECK(status IN
 ('REQUESTED','REJECTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','RETURN_REJECTED','REFUNDING','REFUNDED','CANCELLED'));
-- 原业务单号唯一、旧订单外键确保可准确关联；不重新计算退款金额。
UPDATE trade_after_sale a JOIN trade_order o ON o.order_no=a.order_no SET a.order_id=o.id;
-- 申请时刻是已存在事实；历史最后修改时刻和审批人未知，保持NULL。
UPDATE trade_after_sale SET create_time=requested_at;
-- 仅已完成且唯一退款交易成功的记录，可取原成功落库时刻；没有成功事实的不回填。
UPDATE trade_after_sale a JOIN trade_payment_refund p ON p.after_sale_no=a.refund_no AND p.status='SUCCESS'
 SET a.refund_time=p.update_time WHERE a.status='REFUNDED' AND p.update_time IS NOT NULL;
ALTER TABLE trade_payment_refund ADD CONSTRAINT fk_payment_refund_after_sale
 FOREIGN KEY(after_sale_no) REFERENCES trade_after_sale(refund_no);
-- 旧库完成以上操作后，单独执行V9__after_sale_log.sql创建日志表。
-- 不回填虚构的历史操作日志；回退需停写恢复备份，不能直接切回旧表名服务。
