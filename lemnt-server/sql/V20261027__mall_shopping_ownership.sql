-- 商城业务归属调整：保留原菜单 ID 与角色关联，只更新归属、组件和权限。
-- 部署前确认服务和前端均已切换到 /mall 路径；本脚本未自动执行。
BEGIN;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time) SELECT '商城管理',0,3,'mall',NULL,1,0,'M','0','1','','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M');
SET @mall_owner=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
UPDATE sys_menu SET parent_id=@mall_owner,component='mall/coupon/index' WHERE component='trade/coupon/index';
UPDATE sys_menu SET parent_id=@mall_owner,component='mall/cart/index' WHERE component='trade/cart/index';
UPDATE sys_menu SET parent_id=@mall_owner WHERE perms LIKE 'trade:promotion:%' OR perms='trade:order:create';
UPDATE sys_menu SET menu_name=CASE WHEN menu_name LIKE 'trade:%' THEN CONCAT('mall:',SUBSTRING(menu_name,7)) ELSE menu_name END,
 perms=CONCAT('mall:',SUBSTRING(perms,7))
 WHERE perms LIKE 'trade:cart:%' OR perms LIKE 'trade:checkout:%' OR perms LIKE 'trade:coupon:%'
 OR perms LIKE 'trade:coupon-template:%' OR perms LIKE 'trade:promotion:%' OR perms='trade:order:create';
COMMIT;
