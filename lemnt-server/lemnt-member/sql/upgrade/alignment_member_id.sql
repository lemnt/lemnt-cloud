-- 仅用于旧版 member_no VARCHAR(40) NOT NULL 的存量 member_user 表。
-- 停写备份并确认列定义一致后执行；保留历史编号及唯一索引，不删除历史值。
ALTER TABLE member_user MODIFY COLUMN member_no VARCHAR(40) NULL;
