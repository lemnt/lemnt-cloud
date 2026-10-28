-- 已有订单表升级，执行一次；新库 V1 已包含备注字段。
ALTER TABLE trade_order ADD COLUMN remark VARCHAR(500) NULL;
