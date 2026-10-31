-- 仅缺少审计列的旧表执行一次；备份核对，历史未知NULL，不执行真实库。
ALTER TABLE member_group ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_group ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_group ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_group ADD COLUMN update_by VARCHAR(64) NULL;
ALTER TABLE member_tag ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_tag ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_tag ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_tag ADD COLUMN update_by VARCHAR(64) NULL;
