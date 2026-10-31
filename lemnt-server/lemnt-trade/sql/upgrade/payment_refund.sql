-- 已有 V5 的 MySQL 库升级：停止售后写入，核对约束后执行；新库无需执行此脚本。
ALTER TABLE trade_refund_request DROP CHECK chk_trade_refund_status;
ALTER TABLE trade_refund_request ADD CONSTRAINT chk_trade_refund_status
 CHECK(status IN ('REQUESTED','REJECTED','REFUNDING','REFUNDED'));
-- 随后执行 ../V6__payment_refund.sql；不得重复执行新库初始化脚本。
