-- 订单改价与改址功能权限，不授予角色，由管理员按需授权。
BEGIN;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '交易管理',0,3,'trade',NULL,1,0,'M','0','1','','shopping','migration',CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M');
SET @fulfillment_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '订单改价改址',@fulfillment_root,49,'',NULL,1,0,'F','0','1','trade:order:update','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:order:update');
COMMIT;
