-- 装修模板与页面：property 为 JSON 文本；有且仅有一个模板 used=1。
CREATE TABLE IF NOT EXISTS mall_promotion_diy_template (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 used TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否当前启用的装修模板',
 used_time DATETIME(3) NULL COMMENT '装修模板启用时间',
 remark VARCHAR(255) NULL COMMENT '备注说明',
 preview_pic_urls LONGTEXT NOT NULL COMMENT '预览图片地址JSON数组',
 property LONGTEXT NOT NULL COMMENT '装修组件配置JSON',
 UNIQUE KEY uk_diy_template_name(name),
 KEY idx_diy_template_used(used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商城装修模板';

CREATE TABLE IF NOT EXISTS mall_promotion_diy_page (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 template_id BIGINT NOT NULL COMMENT '优惠券模板编号',
 name VARCHAR(100) NOT NULL COMMENT '名称',
 remark VARCHAR(255) NULL COMMENT '备注说明',
 preview_pic_urls LONGTEXT NOT NULL COMMENT '预览图片地址JSON数组',
 property LONGTEXT NOT NULL COMMENT '装修组件配置JSON',
 UNIQUE KEY uk_diy_page_template_name(template_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商城装修页面';
