-- 支付派发任务后台入口；仅登记菜单与按钮权限，不授予角色，可重复执行。
BEGIN;
SET @trade_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付派发任务',@trade_root,25,'pay-notify-tasks','trade/pay-notify/index',1,0,'C','0','1','trade:pay-notify:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='pay-notify-tasks' AND menu_type='C');
SET @notify_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='pay-notify-tasks' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@notify_page,1,'',NULL,1,0,'F','0','1','trade:pay-notify:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @notify_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@notify_page AND perms='trade:pay-notify:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '重发',@notify_page,2,'',NULL,1,0,'F','0','1','trade:pay-notify:retry','shopping','migration',CURRENT_TIMESTAMP
 WHERE @notify_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@notify_page AND perms='trade:pay-notify:retry' AND menu_type='F');
COMMIT;
