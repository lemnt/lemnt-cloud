-- 仅生成未执行，不猜测历史来源。
ALTER TABLE member_user ADD COLUMN login_ip VARCHAR(50) NULL;
