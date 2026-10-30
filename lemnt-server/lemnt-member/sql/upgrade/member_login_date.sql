-- 仅生成未执行，历史登录时间未知不回填。
ALTER TABLE member_user ADD COLUMN login_date DATETIME NULL;
