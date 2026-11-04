-- 后台支付管理接口权限；仅添加功能权限，不创建未交付页面，不授予角色。
-- 先执行既有交易菜单脚本；串行执行，缺少交易根菜单时不插入。
BEGIN;
SET @trade_payment_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支持支付渠道',@trade_payment_root,21,'',NULL,1,0,'F','0','1','trade:payment-channel:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment-channel:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付单列表',@trade_payment_root,22,'',NULL,1,0,'F','0','1','trade:payment:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付单详情',@trade_payment_root,23,'',NULL,1,0,'F','0','1','trade:payment:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付退单列表',@trade_payment_root,24,'',NULL,1,0,'F','0','1','trade:payment-refund:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment-refund:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '支付退单详情',@trade_payment_root,25,'',NULL,1,0,'F','0','1','trade:payment-refund:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment-refund:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '售后原路退款',@trade_payment_root,26,'',NULL,1,0,'F','0','1','trade:payment-refund:create','shopping','migration',CURRENT_TIMESTAMP
 WHERE @trade_payment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:payment-refund:create');
COMMIT;
