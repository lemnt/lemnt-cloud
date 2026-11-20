-- 店员自提核销台：页面只暴露核销查询与确认，不授予门店维护权限。
BEGIN;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '交易管理',0,3,'trade',NULL,1,0,'M','0','1','','shopping','migration',CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M');
SET @trade_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '自提核销台',@trade_root,32,'pick-up-verify','trade/pick-up-verify/index',1,0,'C','0','1','trade:pickup-verify:list','shop','migration',CURRENT_TIMESTAMP
 WHERE @trade_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@trade_root AND path='pick-up-verify' AND menu_type='C');
COMMIT;
