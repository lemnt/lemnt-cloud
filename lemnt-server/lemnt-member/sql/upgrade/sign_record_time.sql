-- 仅旧member_sign_record结构执行一次；停写、备份并核对字段及约束后操作，未执行真实库。
-- 历史只有业务日期，真实事件时间/审计未知保持NULL，不按日期或当前时间回填。
ALTER TABLE member_sign_record RENAME TO member_sign_in_record;
ALTER TABLE member_sign_in_record CHANGE COLUMN member_id user_id BIGINT NOT NULL;
ALTER TABLE member_sign_in_record CHANGE COLUMN signin_day `day` INT NOT NULL;
ALTER TABLE member_sign_in_record ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_sign_in_record ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_sign_in_record ADD COLUMN update_by VARCHAR(64) NULL;
ALTER TABLE member_sign_in_record ADD COLUMN update_time DATETIME NULL;
