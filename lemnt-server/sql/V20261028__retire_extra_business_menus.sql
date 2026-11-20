-- 在已有菜单脚本之后执行；只移除已退役操作，不自动授予角色权限。
BEGIN;
-- 会员券查询仍使用，先从已退役购物车页面移到优惠券模板页面。
UPDATE sys_menu SET parent_id=(SELECT target_id FROM (
 SELECT MIN(menu_id) AS target_id FROM sys_menu WHERE component='mall/coupon/index'
) target_menu) WHERE perms='mall:coupon:list' AND EXISTS (
 SELECT 1 FROM (SELECT menu_id FROM sys_menu WHERE component='mall/coupon/index') existing_menu
);
DELETE FROM sys_role_menu WHERE menu_id IN (
 SELECT menu_id FROM sys_menu WHERE component='mall/cart/index' OR perms IN (
 'mall:cart:query','mall:cart:edit','mall:checkout:quote','mall:checkout:create','mall:checkout:query',
 'mall:order:create','mall:promotion:publish','mall:promotion:state',
 'member:user:create','member:user:reset-password',
 'trade:fulfillment:receive','trade:refund:request','trade:refund:query','trade:refund:reject'
));
DELETE FROM sys_menu WHERE component='mall/cart/index' OR perms IN (
 'mall:cart:query','mall:cart:edit','mall:checkout:quote','mall:checkout:create','mall:checkout:query',
 'mall:order:create','mall:promotion:publish','mall:promotion:state',
 'member:user:create','member:user:reset-password',
 'trade:fulfillment:receive','trade:refund:request','trade:refund:query','trade:refund:reject'
);
-- 页面继续使用原路由，但由商品售后分页查询；原角色绑定保留，新增售后操作单独授权。
UPDATE sys_menu SET menu_name='售后申请',perms='trade:after-sale:list'
 WHERE component='trade/refund/index' AND perms='trade:refund:list';

-- 芋道仅会员取消及系统超时关闭订单，退役后台手工关单入口。
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms='trade:order:close');
DELETE FROM sys_menu WHERE perms='trade:order:close';
COMMIT;
