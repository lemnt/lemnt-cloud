-- 文章与分类；字段来源芋道ArticleDO/ArticleCategoryDO，审计按目标BaseDO。
CREATE TABLE mall_promotion_article_category (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(128) NOT NULL, pic_url VARCHAR(1024) NULL,
 status INT NOT NULL, sort INT NOT NULL,
 deleted BOOLEAN NOT NULL DEFAULT FALSE,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 KEY idx_article_category_status(status,deleted,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mall_promotion_article (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 category_id BIGINT NOT NULL, spu_id BIGINT NOT NULL,
 title VARCHAR(255) NOT NULL, author VARCHAR(128) NULL,
 pic_url VARCHAR(1024) NOT NULL, introduction VARCHAR(2000) NULL,
 browse_count INT NOT NULL DEFAULT 0, sort INT NOT NULL, status INT NOT NULL,
 recommend_hot BOOLEAN NOT NULL, recommend_banner BOOLEAN NOT NULL,
 content LONGTEXT NOT NULL, deleted BOOLEAN NOT NULL DEFAULT FALSE,
 create_by VARCHAR(64) NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_by VARCHAR(64) NULL, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 KEY idx_article_category(category_id,deleted,id),
 KEY idx_article_title(title,deleted,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
