-- 仅缺少审计列的旧member_level执行一次；备份核对后操作，未执行真实库。
-- 历史审计未知NULL，不回填当前时间或操作者。
ALTER TABLE member_level ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_level ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_level ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_level ADD COLUMN update_by VARCHAR(64) NULL;
