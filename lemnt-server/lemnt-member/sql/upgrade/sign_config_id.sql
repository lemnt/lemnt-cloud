-- 仅旧day主键结构执行一次；停写备份核对后操作，未执行真实库。
-- 新ID为本次升级生成的定位键，不代表源历史ID；未知审计保持NULL。
ALTER TABLE member_sign_config RENAME TO member_sign_in_config;
ALTER TABLE member_sign_in_config CHANGE COLUMN signin_day `day` INT NOT NULL;
ALTER TABLE member_sign_in_config DROP PRIMARY KEY;
ALTER TABLE member_sign_in_config ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE member_sign_in_config ADD CONSTRAINT uk_member_sign_config_day UNIQUE (`day`);
ALTER TABLE member_sign_in_config ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_sign_in_config ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_sign_in_config ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_sign_in_config ADD COLUMN update_by VARCHAR(64) NULL;
