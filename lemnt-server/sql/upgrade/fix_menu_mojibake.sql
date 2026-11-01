-- 修复通过 latin1 连接导入菜单 SQL 造成的双重编码。
-- 执行前先备份：mysqldump --default-character-set=utf8mb4 lemnt_system sys_menu > sys_menu_backup.sql
-- 执行时必须连接 utf8mb4：mysql --default-character-set=utf8mb4 lemnt_system < fix_menu_mojibake.sql
START TRANSACTION;

UPDATE sys_menu
SET menu_name = CONVERT(BINARY(CONVERT(menu_name USING latin1)) USING utf8mb4),
    update_time = CURRENT_TIMESTAMP
WHERE CHAR_LENGTH(CONVERT(BINARY(CONVERT(menu_name USING latin1)) USING utf8mb4))
      < CHAR_LENGTH(menu_name);

COMMIT;

-- 校验：应返回 0；再抽查“商城管理”“自提核销台”等菜单名称。
SELECT COUNT(*) AS remaining_mojibake
FROM sys_menu
WHERE CHAR_LENGTH(CONVERT(BINARY(CONVERT(menu_name USING latin1)) USING utf8mb4))
      < CHAR_LENGTH(menu_name);
