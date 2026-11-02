-- 已有环境执行一次；先备份并停止旧写者，保留历史快照的原始JSON和NULL。
ALTER TABLE trade_order_item RENAME COLUMN properties TO specs;
ALTER TABLE trade_after_sale RENAME COLUMN properties TO specs;
