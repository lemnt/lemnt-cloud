-- 会员持券管理独立入口；不自动授予角色，不覆盖已有页面。
BEGIN;
SET @coupon_mall_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '会员优惠券',@coupon_mall_root,2,'coupon-records','mall/coupon/records',1,0,'C','0','1','mall:coupon:list','shopping','migration',CURRENT_TIMESTAMP
WHERE @coupon_mall_root IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE component='mall/coupon/records' AND menu_type='C');
SET @coupon_template_page=(SELECT menu_id FROM sys_menu WHERE component='mall/coupon/index' AND menu_type='C' ORDER BY menu_id LIMIT 1);
SET @coupon_record_page=(SELECT menu_id FROM sys_menu WHERE component='mall/coupon/records' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '删除优惠券模板',@coupon_template_page,4,'',NULL,1,0,'F','0','1','mall:coupon-template:remove','shopping','migration',CURRENT_TIMESTAMP
WHERE @coupon_template_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:coupon-template:remove');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '撤回会员优惠券',@coupon_record_page,1,'',NULL,1,0,'F','0','1','mall:coupon:remove','shopping','migration',CURRENT_TIMESTAMP
WHERE @coupon_record_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:coupon:remove');
COMMIT;
