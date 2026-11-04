-- 商品管理功能权限；挂载既有商城目录，不创建页面、不授予角色。
BEGIN;
SET @product_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格新增',@product_root,40,'',NULL,1,0,'F','0','1','mall:spec:add','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec:add');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格修改',@product_root,41,'',NULL,1,0,'F','0','1','mall:spec:edit','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec:edit');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格删除',@product_root,42,'',NULL,1,0,'F','0','1','mall:spec:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec:remove');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格详情',@product_root,43,'',NULL,1,0,'F','0','1','mall:spec:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格列表',@product_root,44,'',NULL,1,0,'F','0','1','mall:spec:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格值新增',@product_root,45,'',NULL,1,0,'F','0','1','mall:spec-value:add','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec-value:add');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格值修改',@product_root,46,'',NULL,1,0,'F','0','1','mall:spec-value:edit','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec-value:edit');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格值删除',@product_root,47,'',NULL,1,0,'F','0','1','mall:spec-value:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec-value:remove');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格值详情',@product_root,48,'',NULL,1,0,'F','0','1','mall:spec-value:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec-value:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品规格值列表',@product_root,49,'',NULL,1,0,'F','0','1','mall:spec-value:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spec-value:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品评价列表',@product_root,50,'',NULL,1,0,'F','0','1','mall:comment:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:comment:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品评价详情',@product_root,51,'',NULL,1,0,'F','0','1','mall:comment:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:comment:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品评价回复',@product_root,52,'',NULL,1,0,'F','0','1','mall:comment:reply','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:comment:reply');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品评价修改',@product_root,53,'',NULL,1,0,'F','0','1','mall:comment:edit','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:comment:edit');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品SKU新增',@product_root,54,'',NULL,1,0,'F','0','1','mall:sku:add','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:sku:add');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品SKU删除',@product_root,55,'',NULL,1,0,'F','0','1','mall:sku:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:sku:remove');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品SKU详情',@product_root,56,'',NULL,1,0,'F','0','1','mall:sku:query','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:sku:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品SKU列表',@product_root,57,'',NULL,1,0,'F','0','1','mall:sku:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:sku:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '库存流水列表',@product_root,58,'',NULL,1,0,'F','0','1','mall:stock:list','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:stock:list');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
 SELECT '商品SPU删除',@product_root,59,'',NULL,1,0,'F','0','1','mall:spu:remove','shopping','migration',CURRENT_TIMESTAMP
 WHERE @product_root IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='mall:spu:remove');
COMMIT;
