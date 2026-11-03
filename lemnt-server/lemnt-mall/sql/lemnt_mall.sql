-- lemnt_mall：最终表结构初始化。仅用于建库，不提供存量升级或历史回填。
-- 本服务没有必须预置的业务数据；菜单、字典与默认参数归system，本次保持原样。

CREATE TABLE IF NOT EXISTS mall_product_stock_reservation (
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

CREATE TABLE IF NOT EXISTS mall_product_category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    parent_id BIGINT NULL COMMENT '上级分类编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    pic_url VARCHAR(500) NULL COMMENT '图片地址',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '分类状态：ENABLED启用、DISABLED停用',
    CONSTRAINT fk_mall_category_parent FOREIGN KEY(parent_id) REFERENCES mall_product_category(id),
    CONSTRAINT chk_mall_category_status CHECK(status IN ('ENABLED','DISABLED')),
    KEY idx_mall_category_parent(parent_id,sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品分类';

CREATE TABLE IF NOT EXISTS mall_product_brand (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    pic_url VARCHAR(500) NULL COMMENT '图片地址',
    description VARCHAR(2000) NULL COMMENT '描述说明',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '品牌状态：ENABLED启用、DISABLED停用',
    CONSTRAINT chk_mall_brand_status CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品品牌';

CREATE TABLE IF NOT EXISTS mall_cart (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    product_id BIGINT NOT NULL COMMENT '商品SKU编号',
    quantity INT NOT NULL COMMENT '商品数量',
    selected BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否选中购物车商品',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '购物车乐观锁版本号',
    UNIQUE KEY uk_trade_cart_member_product(member_id,product_id),
    CONSTRAINT chk_trade_cart_quantity CHECK(quantity BETWEEN 1 AND 999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员购物车商品';

CREATE TABLE IF NOT EXISTS mall_coupon_template (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    code VARCHAR(64) NOT NULL COMMENT '业务编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    kind VARCHAR(16) NOT NULL COMMENT '优惠券类型：FIXED固定金额、PERCENT比例折扣',
    threshold DECIMAL(19,2) NOT NULL COMMENT '优惠使用门槛金额',
    fixed_amount DECIMAL(19,2) NULL COMMENT '固定优惠金额，单位元',
    discount_percent INT NULL COMMENT '优惠券支付比例百分比，80表示支付原价的80%',
    discount_limit DECIMAL(19,2) NULL COMMENT '折扣优惠金额上限',
    total_count INT NOT NULL COMMENT '优惠券发放总名额，-1不限',
    issued_count INT NOT NULL DEFAULT 0 COMMENT '已发放优惠券数量',
    valid_from DATETIME NULL COMMENT '优惠券开始有效时间',
    valid_until DATETIME NULL COMMENT '优惠券结束有效时间',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    take_type VARCHAR(16) NOT NULL DEFAULT 'ADMIN' COMMENT '领取方式：ADMIN后台发放、USER会员领取',
    description VARCHAR(500) NULL COMMENT '描述说明',
    take_limit_count INT NOT NULL DEFAULT 1 COMMENT '每会员限领张数，-1不限',
    product_scope VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '商品适用范围：ALL全部、SPU_IDS指定SPU、CATEGORY_IDS指定分类',
    product_scope_values LONGTEXT NULL COMMENT '适用商品或分类编号JSON数组',
    validity_type VARCHAR(16) NOT NULL DEFAULT 'DATE' COMMENT '有效期方式：DATE固定日期、TERM领取后有效期',
    fixed_start_term INT NULL COMMENT '领取后延迟生效天数',
    fixed_end_term INT NULL COMMENT '生效后的有效天数',
    use_count INT NOT NULL DEFAULT 0 COMMENT '已使用优惠券数量',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_trade_coupon_template_code(code),
    CONSTRAINT chk_trade_coupon_template_kind CHECK(kind IN ('FIXED','PERCENT')),
    CONSTRAINT chk_trade_coupon_template_counts CHECK((total_count=-1 OR total_count>0) AND issued_count>=0 AND (take_type<>'USER' OR total_count=-1 OR issued_count<=total_count)),
    CONSTRAINT chk_trade_coupon_template_dates CHECK((validity_type='DATE' AND valid_until>valid_from) OR (validity_type='TERM' AND fixed_start_term>=0 AND fixed_end_term>0)),
    CONSTRAINT chk_trade_coupon_template_amount CHECK(threshold>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '优惠券模板及发放规则';

CREATE TABLE IF NOT EXISTS mall_promotion_reward_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    condition_type VARCHAR(16) NOT NULL COMMENT '满减门槛类型：PRICE金额、COUNT件数',
    product_scope VARCHAR(16) NOT NULL COMMENT '商品适用范围：ALL全部、SPU_IDS指定SPU、CATEGORY_IDS指定分类',
    product_scope_values_json LONGTEXT NOT NULL COMMENT '适用商品或分类编号JSON数组',
    rules_json LONGTEXT NOT NULL COMMENT '满减阶梯及包邮、赠积分、赠券规则JSON',
    CONSTRAINT chk_reward_status CHECK(status IN (0,1)),
    CONSTRAINT chk_reward_time CHECK(end_time>start_time),
    CONSTRAINT chk_reward_condition CHECK(condition_type IN ('PRICE','COUNT')),
    CONSTRAINT chk_reward_scope CHECK(product_scope IN ('ALL','SPU_IDS','CATEGORY_IDS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '满减赠送活动';

CREATE TABLE IF NOT EXISTS mall_promotion_discount_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    CONSTRAINT chk_discount_status CHECK(status IN (0,1)),
    CONSTRAINT chk_discount_time CHECK(end_time>start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动';

CREATE TABLE IF NOT EXISTS mall_product_spec (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(64) NOT NULL COMMENT '名称',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_mall_product_spec_name(name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品规格项字典';

CREATE TABLE IF NOT EXISTS mall_product_favorite (
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

CREATE TABLE IF NOT EXISTS mall_product_browse_history (
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

CREATE TABLE IF NOT EXISTS mall_promotion_seckill_config (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    start_time VARCHAR(8) NOT NULL COMMENT '秒杀时段开始时刻，HH:mm:ss',
    end_time VARCHAR(8) NOT NULL COMMENT '秒杀时段结束时刻，HH:mm:ss',
    slider_pic_urls LONGTEXT NOT NULL COMMENT '轮播图片地址JSON数组',
    status INT NOT NULL DEFAULT 0 COMMENT '业务状态',
    CONSTRAINT chk_seckill_config_status CHECK(status IN (0,1)),
    CONSTRAINT chk_seckill_config_time CHECK(start_time<end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀时段配置';

CREATE TABLE IF NOT EXISTS mall_promotion_seckill_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    config_ids LONGTEXT NOT NULL COMMENT '秒杀时段编号JSON数组',
    total_limit_count INT NULL COMMENT '每会员累计购买数量上限',
    single_limit_count INT NULL COMMENT '单次购买数量上限',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '活动配置总库存',
    CONSTRAINT chk_seckill_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_seckill_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_seckill_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动';

CREATE TABLE IF NOT EXISTS mall_promotion_combination_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    total_limit_count INT NOT NULL COMMENT '每会员累计购买数量上限',
    single_limit_count INT NOT NULL COMMENT '单次购买数量上限',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    user_size INT NOT NULL COMMENT '拼团成团所需人数',
    virtual_group BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许虚拟成团',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    limit_duration INT NOT NULL COMMENT '拼团有效时长，单位小时',
    CONSTRAINT chk_combination_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_combination_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_combination_activity_user_size CHECK(user_size>=2),
    CONSTRAINT chk_combination_activity_duration CHECK(limit_duration>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动';

CREATE TABLE IF NOT EXISTS mall_promotion_combination_record (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    combination_price DECIMAL(19,2) NOT NULL COMMENT '拼团成交单价，单位元',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    spu_name VARCHAR(200) NOT NULL COMMENT '商品SPU名称快照',
    pic_url VARCHAR(1024) NULL COMMENT '图片地址',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    count INT NOT NULL COMMENT '商品数量',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    nickname VARCHAR(100) NULL COMMENT '会员昵称',
    avatar VARCHAR(1024) NULL COMMENT '头像地址',
    head_id BIGINT NOT NULL COMMENT '团长拼团记录编号，0表示团长本人',
    status INT NOT NULL DEFAULT 0 COMMENT '拼团状态：0进行中、1成功、2失败',
    order_id BIGINT NULL COMMENT '交易订单主键编号',
    user_size INT NOT NULL COMMENT '拼团成团所需人数',
    user_count INT NOT NULL DEFAULT 1 COMMENT '当前参团人数',
    virtual_group BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否允许虚拟成团',
    expire_time DATETIME(3) NOT NULL COMMENT '到期时间',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NULL COMMENT '结束时间',
    KEY idx_combination_record_user(user_id,activity_id),
    KEY idx_combination_record_head(head_id,status),
    KEY idx_combination_record_expire(head_id,status,expire_time),
    CONSTRAINT chk_combination_record_status CHECK(status IN (0,1,2)),
    CONSTRAINT chk_combination_record_count CHECK(count>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团参与记录';

CREATE TABLE IF NOT EXISTS mall_promotion_bargain_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    bargain_first_price DECIMAL(19,2) NOT NULL COMMENT '砍价起始金额，单位元',
    bargain_min_price DECIMAL(19,2) NOT NULL COMMENT '砍价底价，单位元',
    stock INT NOT NULL COMMENT '当前可用库存',
    total_stock INT NOT NULL COMMENT '活动配置总库存',
    help_max_count INT NOT NULL COMMENT '砍价助力人数上限',
    bargain_count INT NOT NULL COMMENT '帮砍次数配置',
    total_limit_count INT NOT NULL COMMENT '每会员累计购买数量上限',
    random_min_price DECIMAL(19,2) NOT NULL COMMENT '单次助力随机减免金额下限，单位元',
    random_max_price DECIMAL(19,2) NOT NULL COMMENT '单次助力随机减免金额上限，单位元',
    CONSTRAINT chk_bargain_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_bargain_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_bargain_activity_price CHECK(bargain_first_price>0 AND bargain_min_price>=0 AND bargain_min_price<bargain_first_price),
    CONSTRAINT chk_bargain_activity_random CHECK(random_min_price>0 AND random_max_price>=random_min_price),
    CONSTRAINT chk_bargain_activity_stock CHECK(stock>=0 AND total_stock>=stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价活动';

CREATE TABLE IF NOT EXISTS mall_promotion_point_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '活动配置总库存',
    CONSTRAINT chk_point_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_point_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动';

CREATE TABLE IF NOT EXISTS mall_promotion_order (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    activity_type VARCHAR(16) NOT NULL COMMENT '活动类型：SECKILL秒杀、COMBINATION拼团、BARGAIN砍价、POINT积分商城',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    activity_record_id BIGINT NULL COMMENT '活动参与记录编号',
    head_id BIGINT NULL COMMENT '拼团团长记录编号，0表示开团',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    quantity INT NOT NULL COMMENT '商品数量',
    activity_price DECIMAL(19,2) NOT NULL COMMENT '活动成交单价，单位元',
    use_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单使用积分数',
    refunded_quantity INT NOT NULL DEFAULT 0 COMMENT '累计已退商品数量',
    status VARCHAR(16) NOT NULL DEFAULT 'CREATED' COMMENT '活动订单状态：CREATED已创建、RESERVED已预占、PAID已支付、CLOSED已关闭、REFUNDED已退完、FAILED失败',
    failure_reason VARCHAR(500) NULL COMMENT '失败原因',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    UNIQUE KEY uk_promotion_order_no(order_no),
    UNIQUE KEY uk_promotion_request(member_id,request_no),
    KEY idx_promotion_order_activity(activity_type,activity_id),
    CONSTRAINT chk_promotion_order_type CHECK(activity_type IN ('SECKILL','COMBINATION','BARGAIN','POINT')),
    CONSTRAINT chk_promotion_order_status CHECK(status IN ('CREATED','RESERVED','PAID','CLOSED','REFUNDED','FAILED')),
    CONSTRAINT chk_promotion_order_quantity CHECK(quantity>0),
    CONSTRAINT chk_promotion_order_price CHECK(activity_price>=0),
    CONSTRAINT chk_promotion_order_point CHECK(use_point>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '活动订单与交易订单关联';

CREATE TABLE IF NOT EXISTS mall_promotion_order_refund (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款业务单号',
    quantity INT NOT NULL COMMENT '商品数量',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_promotion_refund_no(refund_no),
    KEY idx_promotion_refund_order(order_no),
    CONSTRAINT chk_promotion_refund_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '活动订单退款幂等记录';

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

CREATE TABLE IF NOT EXISTS mall_promotion_kefu_conversation (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    last_message_time DATETIME(3) NOT NULL COMMENT '最近消息时间',
    last_message_content VARCHAR(500) NULL COMMENT '最近消息内容摘要',
    last_message_content_type INT NULL COMMENT '最近消息类型：1文本、2图片',
    admin_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '管理员端是否置顶会话',
    admin_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '管理员端是否删除会话',
    admin_unread_message_count INT NOT NULL DEFAULT 0 COMMENT '管理员未读消息数',
    UNIQUE KEY uk_kefu_conversation_user(user_id),
    KEY idx_kefu_conversation_list(admin_deleted,admin_pinned,last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员客服会话';

CREATE TABLE IF NOT EXISTS mall_promotion_kefu_message (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    conversation_id BIGINT NOT NULL COMMENT '客服会话编号',
    sender_id BIGINT NOT NULL COMMENT '消息发送人编号',
    sender_type INT NOT NULL COMMENT '发送人类型：1会员、2管理员',
    receiver_id BIGINT NOT NULL COMMENT '消息接收人编号',
    receiver_type INT NOT NULL COMMENT '接收人类型：1会员、2管理员',
    content_type INT NOT NULL COMMENT '消息内容类型：1文本、2图片',
    content VARCHAR(2000) NOT NULL COMMENT '内容',
    read_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读，0否、1是',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_kefu_message_conversation(conversation_id,create_time),
    KEY idx_kefu_message_unread(conversation_id,sender_type,read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '客服会话消息';

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

CREATE TABLE IF NOT EXISTS mall_product_spu (
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
    UNIQUE KEY uk_mall_spu_code(code),
    KEY idx_mall_spu_status(status,id),
    CONSTRAINT fk_mall_spu_category FOREIGN KEY(category_id) REFERENCES mall_product_category(id),
    CONSTRAINT fk_mall_spu_brand FOREIGN KEY(brand_id) REFERENCES mall_product_brand(id),
    CONSTRAINT chk_mall_spu_status CHECK(status IN ('ON_SHELF','OFF_SHELF','RECYCLED','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品SPU基础信息';

CREATE TABLE IF NOT EXISTS mall_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    template_id BIGINT NOT NULL COMMENT '优惠券模板编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    code VARCHAR(64) NULL COMMENT '业务编码',
    name VARCHAR(500) NULL COMMENT '名称',
    take_type VARCHAR(64) NULL COMMENT '领取方式：ADMIN后台发放、USER会员领取',
    kind VARCHAR(64) NULL COMMENT '优惠券类型：FIXED固定金额、PERCENT比例折扣',
    threshold DECIMAL(19,2) NULL COMMENT '优惠使用门槛金额',
    fixed_amount DECIMAL(19,2) NULL COMMENT '固定优惠金额，单位元',
    discount_percent INT NULL COMMENT '领取时支付比例百分比快照',
    discount_limit DECIMAL(19,2) NULL COMMENT '折扣优惠金额上限',
    product_scope VARCHAR(64) NULL COMMENT '商品适用范围：ALL全部、SPU_IDS指定SPU、CATEGORY_IDS指定分类',
    product_scope_values LONGTEXT NULL COMMENT '适用商品或分类编号JSON数组',
    valid_from DATETIME NULL COMMENT '优惠券开始有效时间',
    valid_until DATETIME NULL COMMENT '优惠券结束有效时间',
    use_time DATETIME NULL COMMENT '优惠券核销时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE' COMMENT '持券状态：AVAILABLE可用、LOCKED订单锁定、USED已用、EXPIRED过期',
    order_no VARCHAR(40) NULL COMMENT '交易订单业务单号',
    KEY idx_coupon_member_template(member_id,template_id),
    KEY idx_coupon_expire(status,valid_until),
    UNIQUE KEY uk_trade_coupon_order(order_no),
    CONSTRAINT fk_trade_coupon_template FOREIGN KEY(template_id) REFERENCES mall_coupon_template(id),
    CONSTRAINT chk_trade_coupon_status CHECK(status IN ('AVAILABLE','LOCKED','USED','EXPIRED')),
    KEY idx_trade_coupon_member_status(member_id,status),
    source_order_no VARCHAR(40) NULL COMMENT '营销赠券来源订单号',
    gift_sequence INT NULL COMMENT '同一订单、模板和会员下的赠券发放序号，从1开始',
    UNIQUE KEY uk_mall_coupon_gift (source_order_no, template_id, member_id, gift_sequence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员持券及领取时规则快照';

CREATE TABLE IF NOT EXISTS mall_promotion_discount_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    discount_type VARCHAR(16) NOT NULL COMMENT '优惠方式：PERCENT比例折扣、PRICE固定减价',
    discount_percent INT NULL COMMENT '支付比例万分比，8000表示支付原价的80%',
    discount_price DECIMAL(19,2) NULL COMMENT '优惠减免金额，单位元',
    UNIQUE KEY uk_discount_activity_sku(activity_id,sku_id),
    CONSTRAINT fk_discount_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_discount_activity(id),
    CONSTRAINT chk_discount_product CHECK((discount_type='PERCENT' AND discount_percent BETWEEN 1 AND 9999)
 OR (discount_type='PRICE' AND discount_price>0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动商品';

CREATE TABLE IF NOT EXISTS mall_product_spec_value (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    spec_id BIGINT NOT NULL COMMENT '商品规格项编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_mall_product_spec_value(spec_id,name),
    CONSTRAINT fk_mall_product_spec_value FOREIGN KEY(spec_id) REFERENCES mall_product_spec(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品规格值字典';

CREATE TABLE IF NOT EXISTS mall_promotion_seckill_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    config_ids LONGTEXT NOT NULL COMMENT '秒杀时段编号JSON数组',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    seckill_price DECIMAL(19,2) NOT NULL COMMENT '秒杀成交单价，单位元',
    stock INT NOT NULL COMMENT '当前可用库存',
    activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
    activity_start_time DATETIME(3) NOT NULL COMMENT '活动开始时间快照',
    activity_end_time DATETIME(3) NOT NULL COMMENT '活动结束时间快照',
    UNIQUE KEY uk_seckill_activity_sku(activity_id,sku_id),
    KEY idx_seckill_product_spu(spu_id),
    CONSTRAINT fk_seckill_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_seckill_activity(id),
    CONSTRAINT chk_seckill_product_price CHECK(seckill_price>0),
    CONSTRAINT chk_seckill_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动商品';

CREATE TABLE IF NOT EXISTS mall_promotion_combination_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    combination_price DECIMAL(19,2) NOT NULL COMMENT '拼团成交单价，单位元',
    activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
    activity_start_time DATETIME(3) NOT NULL COMMENT '活动开始时间快照',
    activity_end_time DATETIME(3) NOT NULL COMMENT '活动结束时间快照',
    UNIQUE KEY uk_combination_activity_sku(activity_id,sku_id),
    CONSTRAINT fk_combination_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_combination_activity(id),
    CONSTRAINT chk_combination_product_price CHECK(combination_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动商品';

CREATE TABLE IF NOT EXISTS mall_promotion_bargain_record (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    bargain_first_price DECIMAL(19,2) NOT NULL COMMENT '砍价起始金额，单位元',
    bargain_price DECIMAL(19,2) NOT NULL COMMENT '当前砍价金额，单位元',
    status INT NOT NULL DEFAULT 1 COMMENT '砍价状态：1进行中、2成功、3失败',
    end_time DATETIME(3) NULL COMMENT '结束时间',
    order_id BIGINT NULL COMMENT '交易订单主键编号',
    KEY idx_bargain_record_user(user_id,activity_id,status),
    CONSTRAINT fk_bargain_record_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_bargain_activity(id),
    CONSTRAINT chk_bargain_record_status CHECK(status IN (1,2,3)),
    CONSTRAINT chk_bargain_record_price CHECK(bargain_price>=0 AND bargain_price<=bargain_first_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员砍价参与记录';

CREATE TABLE IF NOT EXISTS mall_promotion_point_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    exchange_count INT NOT NULL COMMENT '每份兑换包含的商品数量',
    point INT NOT NULL COMMENT '每份兑换消耗的积分数',
    price DECIMAL(19,2) NOT NULL COMMENT '每份兑换需支付的现金金额，单位元',
    stock INT NOT NULL COMMENT '当前可用库存',
    activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
    UNIQUE KEY uk_point_activity_sku(activity_id,sku_id),
    CONSTRAINT fk_point_product_activity FOREIGN KEY(activity_id) REFERENCES mall_promotion_point_activity(id),
    CONSTRAINT chk_point_product_count CHECK(exchange_count>0),
    CONSTRAINT chk_point_product_point CHECK(point>=0),
    CONSTRAINT chk_point_product_price CHECK(price>=0),
    CONSTRAINT chk_point_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动商品';

CREATE TABLE IF NOT EXISTS mall_promotion_order_item (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    quantity INT NOT NULL COMMENT '商品数量',
    use_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单使用积分数',
    refunded_quantity INT NOT NULL DEFAULT 0 COMMENT '累计已退商品数量',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    UNIQUE KEY uk_promotion_order_item(order_no,sku_id),
    KEY idx_promotion_order_item_sku(sku_id),
    CONSTRAINT fk_promotion_order_item_order FOREIGN KEY(order_no) REFERENCES mall_promotion_order(order_no),
    CONSTRAINT chk_promotion_order_item_quantity CHECK(quantity>0),
    CONSTRAINT chk_promotion_order_item_point CHECK(use_point>=0),
    CONSTRAINT chk_promotion_order_item_refunded CHECK(refunded_quantity>=0 AND refunded_quantity<=quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '活动订单商品及积分退款快照';

CREATE TABLE IF NOT EXISTS mall_product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    product_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码快照',
    name VARCHAR(200) NOT NULL COMMENT '名称',
    description TEXT NULL COMMENT '描述说明',
    main_image VARCHAR(500) NULL COMMENT '商品主图地址',
    weight DECIMAL(18,6) NULL COMMENT '单件重量，千克',
    volume DECIMAL(18,6) NULL COMMENT '单件体积，立方米',
    price DECIMAL(19,2) NOT NULL COMMENT 'SKU销售单价，单位元',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    status VARCHAR(20) NOT NULL DEFAULT 'OFF_SHELF' COMMENT '商品状态：ON_SHELF上架、OFF_SHELF下架',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_mall_product_code(product_code),
    KEY idx_mall_product_status(status),
    CONSTRAINT chk_mall_product_price CHECK(price>0),
    CONSTRAINT chk_mall_product_stock CHECK(stock>=0),
    spu_id BIGINT NULL COMMENT '商品SPU编号',
    variant_key VARCHAR(64) NULL COMMENT 'SKU规格组合唯一摘要',
    CONSTRAINT fk_mall_product_spu FOREIGN KEY(spu_id) REFERENCES mall_product_spu(id),
    CONSTRAINT uk_mall_sku_variant UNIQUE(spu_id,variant_key),
    specs LONGTEXT NULL COMMENT '规格JSON数组，元素为specId/specName/specValueId/specValueName'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品';

CREATE TABLE IF NOT EXISTS mall_promotion_bargain_help (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    record_id BIGINT NOT NULL COMMENT '活动参与记录编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    reduce_price DECIMAL(19,2) NOT NULL COMMENT '本次助力减免金额，单位元',
    UNIQUE KEY uk_bargain_help_record_user(record_id,user_id),
    KEY idx_bargain_help_user_activity(user_id,activity_id),
    CONSTRAINT fk_bargain_help_record FOREIGN KEY(record_id) REFERENCES mall_promotion_bargain_record(id),
    CONSTRAINT chk_bargain_help_price CHECK(reduce_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价助力记录';

CREATE TABLE IF NOT EXISTS mall_product_comment (
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
    CONSTRAINT fk_mall_comment_spu FOREIGN KEY(spu_id) REFERENCES mall_product_spu(id),
    CONSTRAINT fk_mall_comment_sku FOREIGN KEY(sku_id) REFERENCES mall_product_sku(id),
    CONSTRAINT chk_mall_comment_score CHECK(score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商品订单评价及商家回复';
