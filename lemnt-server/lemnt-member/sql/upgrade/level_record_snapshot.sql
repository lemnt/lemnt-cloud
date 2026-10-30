-- 仅旧结构执行一次；先备份核对列，未执行真实数据库。
-- 原experience为会员余额，改名保留；新增源记录值等字段未知NULL，不关联当前配置回填。
ALTER TABLE member_level_record CHANGE COLUMN member_id user_id BIGINT NOT NULL;
ALTER TABLE member_level_record CHANGE COLUMN experience user_experience BIGINT NOT NULL;
ALTER TABLE member_level_record CHANGE COLUMN reason remark VARCHAR(500) NOT NULL;
ALTER TABLE member_level_record ADD COLUMN experience BIGINT NULL;
ALTER TABLE member_level_record ADD COLUMN level INT NULL;
ALTER TABLE member_level_record ADD COLUMN discount_percent INT NULL;
ALTER TABLE member_level_record ADD COLUMN description VARCHAR(500) NULL;
ALTER TABLE member_level_record ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_level_record ADD COLUMN update_by VARCHAR(64) NULL;
ALTER TABLE member_level_record ADD COLUMN update_time DATETIME NULL;
