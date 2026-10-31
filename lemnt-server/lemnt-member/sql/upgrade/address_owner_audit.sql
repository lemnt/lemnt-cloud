-- 仅旧member_address(member_id、无审计列)执行一次；停写、备份核对，未执行真实库。
-- 不回填无法确认的历史创建/更新人及时间；原归属、默认状态、索引和外键保持。
ALTER TABLE member_address CHANGE COLUMN member_id user_id BIGINT NOT NULL;
ALTER TABLE member_address ADD COLUMN create_time DATETIME NULL;
ALTER TABLE member_address ADD COLUMN create_by VARCHAR(64) NULL;
ALTER TABLE member_address ADD COLUMN update_time DATETIME NULL;
ALTER TABLE member_address ADD COLUMN update_by VARCHAR(64) NULL;
