-- 会员钱包后台入口；仅登记菜单与按钮权限，不授予角色，可重复执行。
BEGIN;
SET @trade_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '会员钱包',@trade_root,26,'wallets','trade/wallet/index',1,0,'C','0','1','trade:wallet:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='wallets' AND menu_type='C');
SET @wallet_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='wallets' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@wallet_page,1,'',NULL,1,0,'F','0','1','trade:wallet:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @wallet_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@wallet_page AND perms='trade:wallet:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '调整余额',@wallet_page,2,'',NULL,1,0,'F','0','1','trade:wallet:adjust','shopping','migration',CURRENT_TIMESTAMP
 WHERE @wallet_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@wallet_page AND perms='trade:wallet:adjust' AND menu_type='F');
COMMIT;
