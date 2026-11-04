-- 商品导出复用原商品菜单，只登记权限，不自动授予角色。
BEGIN;
SET @spu_page=(SELECT menu_id FROM sys_menu WHERE component='mall/spu/index' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '商品导出',@spu_page,6,'',NULL,1,0,'F','0','1','mall:spu:export','shopping','migration',CURRENT_TIMESTAMP
WHERE @spu_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@spu_page AND perms='mall:spu:export' AND menu_type='F');
COMMIT;
