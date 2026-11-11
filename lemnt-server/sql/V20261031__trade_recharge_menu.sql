-- 充值套餐与充值单后台入口；仅登记菜单与按钮权限，不授予角色，可重复执行。
BEGIN;
SET @trade_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '充值套餐',@trade_root,27,'recharge-packages','trade/recharge-package/index',1,0,'C','0','1','trade:recharge-package:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='recharge-packages' AND menu_type='C');
SET @package_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='recharge-packages' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@package_page,1,'',NULL,1,0,'F','0','1','trade:recharge-package:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @package_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@package_page AND perms='trade:recharge-package:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '新增',@package_page,2,'',NULL,1,0,'F','0','1','trade:recharge-package:create','shopping','migration',CURRENT_TIMESTAMP
 WHERE @package_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@package_page AND perms='trade:recharge-package:create' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '修改',@package_page,3,'',NULL,1,0,'F','0','1','trade:recharge-package:update','shopping','migration',CURRENT_TIMESTAMP
 WHERE @package_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@package_page AND perms='trade:recharge-package:update' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '删除',@package_page,4,'',NULL,1,0,'F','0','1','trade:recharge-package:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @package_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@package_page AND perms='trade:recharge-package:remove' AND menu_type='F');

INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '充值单',@trade_root,28,'wallet-recharges','trade/wallet-recharge/index',1,0,'C','0','1','trade:wallet-recharge:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='wallet-recharges' AND menu_type='C');
SET @recharge_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@trade_root AND path='wallet-recharges' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '查询',@recharge_page,1,'',NULL,1,0,'F','0','1','trade:wallet-recharge:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @recharge_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@recharge_page AND perms='trade:wallet-recharge:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '确认到账',@recharge_page,2,'',NULL,1,0,'F','0','1','trade:wallet-recharge:confirm','shopping','migration',CURRENT_TIMESTAMP
 WHERE @recharge_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@recharge_page AND perms='trade:wallet-recharge:confirm' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '退款',@recharge_page,3,'',NULL,1,0,'F','0','1','trade:wallet-recharge:refund','shopping','migration',CURRENT_TIMESTAMP
 WHERE @recharge_page IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@recharge_page AND perms='trade:wallet-recharge:refund' AND menu_type='F');
COMMIT;
