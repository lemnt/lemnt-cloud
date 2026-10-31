-- 仅生成未执行；历史未知不回填。
ALTER TABLE member_asset_record ADD COLUMN point_biz_type INT NULL, ADD COLUMN title VARCHAR(255) NULL, ADD COLUMN description VARCHAR(500) NULL;
