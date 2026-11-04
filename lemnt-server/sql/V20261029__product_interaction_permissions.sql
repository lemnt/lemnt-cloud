-- 只读按钮权限挂载既有商城，不创建菜单页面或授予角色。
BEGIN;
SET @product_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品收藏记录',@product_root,60,'',NULL,1,0,'F','0','1','mall:product-favorite:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:product-favorite:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品浏览足迹',@product_root,61,'',NULL,1,0,'F','0','1','mall:product-history:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:product-history:list');
COMMIT;
