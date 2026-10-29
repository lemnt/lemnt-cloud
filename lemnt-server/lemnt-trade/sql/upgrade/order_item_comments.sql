-- 存量库专用；先核对列，不与新版初始化重复执行。历史状态不猜测未评价。
ALTER TABLE trade_order ADD COLUMN comment_status BOOLEAN NULL;
ALTER TABLE trade_order ADD COLUMN finish_time DATETIME NULL;
ALTER TABLE trade_order_item ADD COLUMN comment_status BOOLEAN NULL;
