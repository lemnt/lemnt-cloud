-- 自评独立权限，不自动授予角色。
BEGIN;
SET @comment_page=(SELECT menu_id FROM sys_menu WHERE component='mall/comment/index' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '添加自评',@comment_page,1,'',NULL,1,0,'F','0','1','mall:comment:create','shopping','migration',CURRENT_TIMESTAMP
WHERE @comment_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:comment:create');
COMMIT;
