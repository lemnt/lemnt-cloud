-- 仅生成未执行，历史未知不回填。
ALTER TABLE member_user ADD COLUMN name VARCHAR(30) NULL, ADD COLUMN area_id INT NULL;
