-- 评价管理入口，复用已有按钮权限；不自动授予任何角色。
BEGIN;
SET @comment_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '商品评价',@comment_root,62,'comments','mall/comment/index',1,0,'C','0','1','mall:comment:list','shopping','migration',CURRENT_TIMESTAMP
WHERE @comment_root IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE component='mall/comment/index' AND menu_type='C');
COMMIT;
