-- 后台订单、商品售后和物流接口功能权限，不注册未交付页面，不授予角色。
BEGIN;
SET @fulfillment_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='trade' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '物流单列表',@fulfillment_root,40,'',NULL,1,0,'F','0','1','trade:shipment:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:shipment:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '物流单详情',@fulfillment_root,41,'',NULL,1,0,'F','0','1','trade:shipment:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:shipment:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '物流轨迹查询',@fulfillment_root,42,'',NULL,1,0,'F','0','1','trade:shipment:track','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:shipment:track');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '关闭未支付订单',@fulfillment_root,43,'',NULL,1,0,'F','0','1','trade:order:close','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:order:close');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '订单统计',@fulfillment_root,44,'',NULL,1,0,'F','0','1','trade:order:summary','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:order:summary');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '订单备注',@fulfillment_root,45,'',NULL,1,0,'F','0','1','trade:order:remark','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:order:remark');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '订单查询',@fulfillment_root,46,'',NULL,1,0,'F','0','1','trade:order:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:order:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品售后申请',@fulfillment_root,47,'',NULL,1,0,'F','0','1','trade:after-sale:create','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:create');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品售后列表',@fulfillment_root,48,'',NULL,1,0,'F','0','1','trade:after-sale:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品售后详情',@fulfillment_root,49,'',NULL,1,0,'F','0','1','trade:after-sale:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '同意商品售后',@fulfillment_root,50,'',NULL,1,0,'F','0','1','trade:after-sale:agree','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:agree');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '拒绝商品售后',@fulfillment_root,51,'',NULL,1,0,'F','0','1','trade:after-sale:reject','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:reject');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '登记退货物流',@fulfillment_root,52,'',NULL,1,0,'F','0','1','trade:after-sale:return','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:return');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '确认退货收货',@fulfillment_root,53,'',NULL,1,0,'F','0','1','trade:after-sale:receive','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:receive');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '拒绝退货收货',@fulfillment_root,54,'',NULL,1,0,'F','0','1','trade:after-sale:refuse','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:refuse');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '确认售后退款',@fulfillment_root,55,'',NULL,1,0,'F','0','1','trade:after-sale:refund','shopping','migration',CURRENT_TIMESTAMP
 WHERE @fulfillment_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='trade:after-sale:refund');
COMMIT;
