-- 营销 Banner：按位置展示，状态 0 开启、1 关闭；点击量由 App 累加。
CREATE TABLE IF NOT EXISTS mall_promotion_banner (
 id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
 title VARCHAR(100) NOT NULL COMMENT '标题',
 url VARCHAR(500) NOT NULL COMMENT '跳转地址',
 pic_url VARCHAR(500) NOT NULL COMMENT '图片地址',
 sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
 status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
 position INT NOT NULL DEFAULT 1 COMMENT '展示位置编码',
 memo VARCHAR(255) NULL COMMENT '备注说明',
 browse_count INT NOT NULL DEFAULT 0 COMMENT '点击浏览次数',
 CONSTRAINT chk_banner_status CHECK(status IN (0,1)),
 CONSTRAINT chk_banner_position CHECK(position BETWEEN 1 AND 5),
 CONSTRAINT chk_banner_browse_count CHECK(browse_count>=0),
 KEY idx_banner_position(position,status,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '营销展示横幅';
