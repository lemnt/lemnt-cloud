-- 仅生成未执行，历史备注未知保持NULL。
ALTER TABLE member_user ADD COLUMN mark VARCHAR(255) NULL;
