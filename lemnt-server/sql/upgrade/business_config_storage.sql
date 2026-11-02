-- 旧库执行一次；先检查重复键并由管理员核实，禁止自动删重复数据。
-- SELECT config_key,COUNT(*) FROM sys_config GROUP BY config_key HAVING COUNT(*)>1;
ALTER TABLE sys_config MODIFY COLUMN config_value LONGTEXT;
ALTER TABLE sys_config ADD UNIQUE KEY uk_sys_config_key(config_key);
