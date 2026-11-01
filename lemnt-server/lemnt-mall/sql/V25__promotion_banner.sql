-- 营销 Banner：按位置展示，状态 0 开启、1 关闭；点击量由 App 累加。
CREATE TABLE IF NOT EXISTS mall_promotion_banner (
 id BIGINT NOT NULL PRIMARY KEY,
 title VARCHAR(100) NOT NULL,
 url VARCHAR(500) NOT NULL,
 pic_url VARCHAR(500) NOT NULL,
 sort INT NOT NULL DEFAULT 0,
 status INT NOT NULL DEFAULT 0,
 position INT NOT NULL DEFAULT 1,
 memo VARCHAR(255) NULL,
 browse_count INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_banner_status CHECK(status IN (0,1)),
 CONSTRAINT chk_banner_position CHECK(position BETWEEN 1 AND 5),
 CONSTRAINT chk_banner_browse_count CHECK(browse_count>=0),
 KEY idx_banner_position(position,status,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
