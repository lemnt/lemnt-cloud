-- 仅旧独立账本执行，未执行真实库。不补填历史操作者或更新时刻。
ALTER TABLE member_point_record ADD COLUMN create_by VARCHAR(64) NULL, ADD COLUMN update_by VARCHAR(64) NULL, ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_experience_record ADD COLUMN create_by VARCHAR(64) NULL, ADD COLUMN update_by VARCHAR(64) NULL, ADD COLUMN update_time DATETIME NULL;
