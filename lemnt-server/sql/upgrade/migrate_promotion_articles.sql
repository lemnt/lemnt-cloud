-- 应用于已分库的promotion数据库。

CREATE TABLE IF NOT EXISTS promotion_article_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '文章分类编号',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    pic_url VARCHAR(500) NULL COMMENT '分类图片',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序，值越大越靠前',
    status INT NOT NULL DEFAULT 0 COMMENT '0启用，1停用',
    CONSTRAINT chk_promotion_article_category_status CHECK(status IN(0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城文章分类';

CREATE TABLE IF NOT EXISTS promotion_article (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '文章编号',
    category_id BIGINT NOT NULL COMMENT '文章分类编号',
    spu_id BIGINT NULL COMMENT '关联商品编号，商品归product领域',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    author VARCHAR(100) NULL COMMENT '作者',
    pic_url VARCHAR(500) NULL COMMENT '封面',
    introduction VARCHAR(500) NULL COMMENT '摘要',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序，值越大越靠前',
    status INT NOT NULL DEFAULT 0 COMMENT '0启用，1停用',
    recommend_hot BOOLEAN NOT NULL DEFAULT FALSE COMMENT '热门推荐',
    recommend_banner BOOLEAN NOT NULL DEFAULT FALSE COMMENT '轮播推荐',
    content LONGTEXT NOT NULL COMMENT '正文',
    browse_count BIGINT NOT NULL DEFAULT 0 COMMENT '累计浏览次数',
    INDEX idx_promotion_article_category(category_id),
    CONSTRAINT fk_promotion_article_category FOREIGN KEY(category_id) REFERENCES promotion_article_category(id),
    CONSTRAINT chk_promotion_article_status CHECK(status IN(0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城文章';
