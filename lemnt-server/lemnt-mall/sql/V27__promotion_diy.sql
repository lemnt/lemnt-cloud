-- 装修模板与页面：property 为 JSON 文本；有且仅有一个模板 used=1。
CREATE TABLE IF NOT EXISTS mall_promotion_diy_template (
 id BIGINT NOT NULL PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 used TINYINT(1) NOT NULL DEFAULT 0,
 used_time DATETIME(3) NULL,
 remark VARCHAR(255) NULL,
 preview_pic_urls LONGTEXT NOT NULL,
 property LONGTEXT NOT NULL,
 UNIQUE KEY uk_diy_template_name(name),
 KEY idx_diy_template_used(used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_promotion_diy_page (
 id BIGINT NOT NULL PRIMARY KEY,
 template_id BIGINT NOT NULL,
 name VARCHAR(100) NOT NULL,
 remark VARCHAR(255) NULL,
 preview_pic_urls LONGTEXT NOT NULL,
 property LONGTEXT NOT NULL,
 UNIQUE KEY uk_diy_page_template_name(template_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
