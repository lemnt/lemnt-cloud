-- 商品服务初始化：仅建表与必要基础数据，不用于升级已有数据库。

CREATE TABLE IF NOT EXISTS product_stock_reservation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    reservation_no VARCHAR(40) NOT NULL COMMENT '库存预占业务编号',
    source_no VARCHAR(40) NULL COMMENT '库存预占来源业务单号',
    product_id BIGINT NOT NULL COMMENT '商品SKU编号',
    quantity INT NOT NULL COMMENT '商品数量',
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED' COMMENT '库存预占状态：RESERVED已预占、CONFIRMED已确认、RELEASED已释放',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_mall_stock_reservation(reservation_no, product_id),
    KEY idx_mall_stock_reservation_status(status, update_time),
    CONSTRAINT chk_mall_stock_reservation_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占幂等台账';

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    parent_id BIGINT NULL COMMENT '上级分类编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    pic_url VARCHAR(500) NULL COMMENT '图片地址',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '分类状态：ENABLED启用、DISABLED停用',
    CONSTRAINT fk_mall_category_parent FOREIGN KEY(parent_id) REFERENCES product_category(id),
    CONSTRAINT chk_mall_category_status CHECK(status IN ('ENABLED','DISABLED')),
    KEY idx_mall_category_parent(parent_id,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品分类';

CREATE TABLE IF NOT EXISTS product_brand (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    pic_url VARCHAR(500) NULL COMMENT '图片地址',
    description VARCHAR(2000) NULL COMMENT '描述说明',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '品牌状态：ENABLED启用、DISABLED停用',
    CONSTRAINT chk_mall_brand_status CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品品牌';

CREATE TABLE IF NOT EXISTS product_spec (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(64) NOT NULL COMMENT '名称',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_mall_product_spec_name(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品规格项字典';

CREATE TABLE IF NOT EXISTS product_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    KEY idx_favorite_user_spu(user_id,spu_id,deleted),
    KEY idx_favorite_user_latest(user_id,deleted,id),
    KEY idx_favorite_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员商品收藏';

CREATE TABLE IF NOT EXISTS product_browse_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    user_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '会员是否删除记录',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    KEY idx_history_user_spu(user_id,spu_id,deleted),
    KEY idx_history_user_latest(user_id,deleted,id),
    KEY idx_history_spu(spu_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员商品浏览记录';

CREATE TABLE IF NOT EXISTS product_spu (
    keyword VARCHAR(255) NULL COMMENT '搜索关键字',
    introduction VARCHAR(128) NULL COMMENT '简介',
    slider_pic_urls LONGTEXT NULL COMMENT '轮播图片地址JSON数组',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    delivery_types LONGTEXT NULL COMMENT '支持的配送方式JSON数组：1快递、2自提',
    delivery_template_id BIGINT NULL COMMENT '运费模板编号',
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    code VARCHAR(64) NOT NULL COMMENT '业务编码',
    name VARCHAR(200) NOT NULL COMMENT '名称',
    category_id BIGINT NOT NULL COMMENT '商品分类编号',
    brand_id BIGINT NOT NULL COMMENT '品牌编号',
    description TEXT NULL COMMENT '描述说明',
    image VARCHAR(500) NULL COMMENT '商品图片地址',
    status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF' COMMENT '商品状态：ON_SHELF上架、OFF_SHELF下架、RECYCLED回收站、DELETED删除',
    sales_count INT NOT NULL DEFAULT 0 COMMENT '销量：支付确认事实累计，售后回补不冲减',
    UNIQUE KEY uk_mall_spu_code(code),
    KEY idx_mall_spu_status(status,id),
    CONSTRAINT fk_mall_spu_category FOREIGN KEY(category_id) REFERENCES product_category(id),
    CONSTRAINT fk_mall_spu_brand FOREIGN KEY(brand_id) REFERENCES product_brand(id),
    CONSTRAINT chk_mall_spu_status CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品SPU基础信息';

CREATE TABLE IF NOT EXISTS product_spec_value (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    spec_id BIGINT NOT NULL COMMENT '商品规格项编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_mall_product_spec_value(spec_id,name),
    CONSTRAINT fk_mall_product_spec_value FOREIGN KEY(spec_id) REFERENCES product_spec(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品规格值字典';

CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    product_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码快照',
    name VARCHAR(200) NOT NULL COMMENT '名称',
    description TEXT NULL COMMENT '描述说明',
    main_image VARCHAR(500) NULL COMMENT '商品主图地址',
    weight DECIMAL(18,6) NULL COMMENT '单件重量，千克',
    volume DECIMAL(18,6) NULL COMMENT '单件体积，立方米',
    price DECIMAL(19,2) NOT NULL COMMENT 'SKU销售单价，单位元',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF' COMMENT '商品状态：ON_SHELF上架、OFF_SHELF下架、RECYCLED回收站、DELETED删除',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_mall_product_code(product_code),
    KEY idx_mall_product_status(status),
    CONSTRAINT chk_mall_product_price CHECK(price>0),
    CONSTRAINT chk_mall_product_stock CHECK(stock>=0),
    CONSTRAINT chk_sku_status CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED')),
    spu_id BIGINT NULL COMMENT '商品SPU编号；历史独立商品（legacy standalone）可为空并保持可售，新建SKU应归属SPU',
    variant_key VARCHAR(64) NULL COMMENT 'SKU规格组合唯一摘要',
    CONSTRAINT fk_mall_product_spu FOREIGN KEY(spu_id) REFERENCES product_spu(id),
    CONSTRAINT uk_mall_sku_variant UNIQUE(spu_id,variant_key),
    specs LONGTEXT NULL COMMENT '规格JSON数组，元素为specId/specName/specValueId/specValueName'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

CREATE TABLE IF NOT EXISTS product_comment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    order_no VARCHAR(40) NULL COMMENT '交易订单业务单号',
    order_item_id BIGINT NULL COMMENT '交易订单明细主键编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    score INT NOT NULL COMMENT '商品综合评分，1至5分',
    content VARCHAR(1000) NOT NULL COMMENT '内容',
    visible BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否前台展示评价',
    reply_content VARCHAR(1000) NULL COMMENT '商家回复内容',
    reply_user_id BIGINT NULL COMMENT '回复管理员编号',
    reply_time DATETIME NULL COMMENT '商家回复时间',
    description_scores INT NULL COMMENT '商品描述评分',
    benefit_scores INT NULL COMMENT '服务评分',
    pic_urls LONGTEXT NULL COMMENT '图片地址JSON数组',
    anonymous BOOLEAN NULL COMMENT '是否匿名评价',
    user_nickname VARCHAR(80) NULL COMMENT '下单时会员昵称快照',
    user_avatar VARCHAR(1024) NULL COMMENT '下单时会员头像快照',
    order_id BIGINT NULL COMMENT '交易订单主键编号',
    spu_name VARCHAR(200) NULL COMMENT '商品SPU名称快照',
    sku_pic_url VARCHAR(1024) NULL COMMENT '下单时SKU图片快照',
    sku_properties LONGTEXT NULL COMMENT '下单时SKU规格JSON快照，元素为specId/specName/specValueId/specValueName',
    reply_status BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已回复',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_mall_comment_order_item(order_item_id),
    KEY idx_mall_comment_spu(spu_id,id),
    KEY idx_product_comment_member(member_id,id),
    KEY idx_product_comment_spu_visible(spu_id,visible,create_time,id),
    KEY idx_product_comment_order(order_no,sku_id),
    CONSTRAINT fk_mall_comment_spu FOREIGN KEY(spu_id) REFERENCES product_spu(id),
    CONSTRAINT fk_mall_comment_sku FOREIGN KEY(sku_id) REFERENCES product_sku(id),
    CONSTRAINT chk_mall_comment_score CHECK(score BETWEEN 1 AND 5),
    CONSTRAINT chk_comment_description_scores CHECK(description_scores IS NULL OR description_scores BETWEEN 1 AND 5),
    CONSTRAINT chk_comment_benefit_scores CHECK(benefit_scores IS NULL OR benefit_scores BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品订单评价及商家回复';

CREATE TABLE IF NOT EXISTS product_message_inbox (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    consumer_key VARCHAR(80) NOT NULL COMMENT '稳定消费处理器标识',
    event_no VARCHAR(64) NOT NULL COMMENT '已处理事件编号',
    event_type VARCHAR(40) NOT NULL COMMENT '事件类型',
    business_key VARCHAR(128) NOT NULL COMMENT '本处理器业务幂等键',
    payload_hash CHAR(64) NOT NULL COMMENT '载荷SHA256，同键不同内容拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费成功时间，与业务同事务',
    PRIMARY KEY (id),
    UNIQUE KEY uk_consumer_event (consumer_key, event_no),
    UNIQUE KEY uk_consumer_business (consumer_key, event_type, business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地事务消息消费幂等记录';
