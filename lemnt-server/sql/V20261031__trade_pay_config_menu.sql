-- 支付应用与渠道配置后台入口；仅登记菜单与按钮权限，不授予角色，可重复执行。
BEGIN;
SET @trade_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付应用',@trade_root,23,'pay-apps','trade/pay-app/index',1,0,'C','0','1','trade:pay-app:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='pay-apps' AND menu_type='C');
SET @app_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='pay-apps' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@app_page,1,'',NULL,1,0,'F','0','1','trade:pay-app:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @app_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@app_page AND perms='trade:pay-app:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '新增',@app_page,2,'',NULL,1,0,'F','0','1','trade:pay-app:create','shopping','migration',CURRENT_TIMESTAMP
 WHERE @app_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@app_page AND perms='trade:pay-app:create' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '修改',@app_page,3,'',NULL,1,0,'F','0','1','trade:pay-app:update','shopping','migration',CURRENT_TIMESTAMP
 WHERE @app_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@app_page AND perms='trade:pay-app:update' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '删除',@app_page,4,'',NULL,1,0,'F','0','1','trade:pay-app:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @app_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@app_page AND perms='trade:pay-app:remove' AND menu_type='F');

INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付渠道配置',@trade_root,24,'pay-channels','trade/pay-channel/index',1,0,'C','0','1','trade:pay-channel:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='pay-channels' AND menu_type='C');
SET @channel_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='pay-channels' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@channel_page,1,'',NULL,1,0,'F','0','1','trade:pay-channel:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @channel_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@channel_page AND perms='trade:pay-channel:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '新增',@channel_page,2,'',NULL,1,0,'F','0','1','trade:pay-channel:create','shopping','migration',CURRENT_TIMESTAMP
 WHERE @channel_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@channel_page AND perms='trade:pay-channel:create' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '修改',@channel_page,3,'',NULL,1,0,'F','0','1','trade:pay-channel:update','shopping','migration',CURRENT_TIMESTAMP
 WHERE @channel_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@channel_page AND perms='trade:pay-channel:update' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '删除',@channel_page,4,'',NULL,1,0,'F','0','1','trade:pay-channel:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @channel_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@channel_page AND perms='trade:pay-channel:remove' AND menu_type='F');
COMMIT;
