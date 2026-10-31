-- 仅旧member_config结构执行一次；备份核对后操作，未执行真实库。
-- 保留已有ID和业务字段，不回填未知历史审计。
ALTER TABLE member_config CHANGE COLUMN id id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE member_config ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_config ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_config ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_config ADD COLUMN update_by VARCHAR(64) NULL;
