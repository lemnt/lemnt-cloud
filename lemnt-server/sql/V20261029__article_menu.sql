-- 商城文章与分类菜单；需已有商城根节点，不自动授予角色。
BEGIN;
SET @article_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='mall' AND menu_type='M' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '商城文章',@article_root,70,'article','mall/article/index',1,0,'C','0','1','mall:article:list','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_root IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE component='mall/article/index' AND menu_type='C');
SET @article_page=(SELECT menu_id FROM sys_menu WHERE component='mall/article/index' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '查看商城文章',@article_page,1,'',NULL,1,0,'F','0','1','mall:article:query','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '新增商城文章',@article_page,2,'',NULL,1,0,'F','0','1','mall:article:create','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article:create');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '修改商城文章',@article_page,3,'',NULL,1,0,'F','0','1','mall:article:update','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article:update');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '删除商城文章',@article_page,4,'',NULL,1,0,'F','0','1','mall:article:delete','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article:delete');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '文章分类',@article_root,70,'article-category','mall/article-category/index',1,0,'C','0','1','mall:article-category:list','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_root IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE component='mall/article-category/index' AND menu_type='C');
SET @article_category_page=(SELECT menu_id FROM sys_menu WHERE component='mall/article-category/index' AND menu_type='C' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '查看文章分类',@article_category_page,1,'',NULL,1,0,'F','0','1','mall:article-category:query','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_category_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article-category:query');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '新增文章分类',@article_category_page,2,'',NULL,1,0,'F','0','1','mall:article-category:create','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_category_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article-category:create');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '修改文章分类',@article_category_page,3,'',NULL,1,0,'F','0','1','mall:article-category:update','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_category_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article-category:update');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '删除文章分类',@article_category_page,4,'',NULL,1,0,'F','0','1','mall:article-category:delete','shopping','migration',CURRENT_TIMESTAMP
WHERE @article_category_page IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='mall:article-category:delete');
COMMIT;
