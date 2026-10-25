-- Additive system-database menu seed. Run serially after review; grants no roles.
-- Does not modify existing menu rows or assign IDs from a reserved numeric range.
BEGIN;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商城管理',0,2,'mall',NULL,1,0,'M','0','1','','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M');
SET @mall_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品分类',@mall_root,1,'category','mall/category/index',1,0,'C','0','1','mall:category:list','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_root AND path='category' AND menu_type='C');
SET @mall_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@mall_root AND path='category' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'query',@mall_page,1,'',NULL,1,0,'F','0','1','mall:category:query','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:category:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'add',@mall_page,2,'',NULL,1,0,'F','0','1','mall:category:add','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:category:add' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'edit',@mall_page,3,'',NULL,1,0,'F','0','1','mall:category:edit','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:category:edit' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'remove',@mall_page,4,'',NULL,1,0,'F','0','1','mall:category:remove','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:category:remove' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品品牌',@mall_root,2,'brand','mall/brand/index',1,0,'C','0','1','mall:brand:list','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_root AND path='brand' AND menu_type='C');
SET @mall_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@mall_root AND path='brand' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'query',@mall_page,1,'',NULL,1,0,'F','0','1','mall:brand:query','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:brand:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'add',@mall_page,2,'',NULL,1,0,'F','0','1','mall:brand:add','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:brand:add' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'edit',@mall_page,3,'',NULL,1,0,'F','0','1','mall:brand:edit','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:brand:edit' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'remove',@mall_page,4,'',NULL,1,0,'F','0','1','mall:brand:remove','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:brand:remove' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品与库存',@mall_root,3,'spu','mall/spu/index',1,0,'C','0','1','mall:spu:list','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_root AND path='spu' AND menu_type='C');
SET @mall_page=(SELECT menu_id FROM sys_menu WHERE parent_id=@mall_root AND path='spu' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'query',@mall_page,1,'',NULL,1,0,'F','0','1','mall:spu:query','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:spu:query' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'add',@mall_page,2,'',NULL,1,0,'F','0','1','mall:spu:add','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:spu:add' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'edit',@mall_page,3,'',NULL,1,0,'F','0','1','mall:spu:edit','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:spu:edit' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT 'SKU资料',@mall_page,10,'',NULL,1,0,'F','0','1','mall:sku:edit','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:sku:edit' AND menu_type='F');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '库存调整',@mall_page,11,'',NULL,1,0,'F','0','1','mall:sku:stock','shopping','migration',CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@mall_page AND perms='mall:sku:stock' AND menu_type='F');
COMMIT;
