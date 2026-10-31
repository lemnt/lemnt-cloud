-- 仅生成，未执行真实库。历史收货时间未知不回填。
ALTER TABLE trade_order ADD COLUMN receive_time DATETIME NULL;
