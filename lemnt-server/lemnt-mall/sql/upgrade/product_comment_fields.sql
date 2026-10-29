-- 仅用于尚未扩展字段的存量库，先备份并核对information_schema；不与新版初始化重复执行。
ALTER TABLE mall_product_comment
 ADD COLUMN description_scores INT NULL,
 ADD COLUMN benefit_scores INT NULL,
 ADD COLUMN pic_urls LONGTEXT NULL,
 ADD COLUMN anonymous BOOLEAN NULL,
 ADD COLUMN user_nickname VARCHAR(80) NULL,
 ADD COLUMN user_avatar VARCHAR(1024) NULL,
 ADD COLUMN order_id BIGINT NULL,
 ADD COLUMN spu_name VARCHAR(200) NULL,
 ADD COLUMN sku_pic_url VARCHAR(1024) NULL,
 ADD COLUMN sku_properties LONGTEXT NULL,
 ADD COLUMN reply_status BOOLEAN NOT NULL DEFAULT FALSE;
-- 回复状态可以由已保存回复事实确定，其他历史快照与细分评分不回填。
UPDATE mall_product_comment SET reply_status = (reply_content IS NOT NULL);
