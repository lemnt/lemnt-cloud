-- 营销服务初始化，仅用于空库；商品和订单不在本库。

CREATE TABLE IF NOT EXISTS promotion_cart (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    product_id BIGINT NOT NULL COMMENT '加入购车商品SKU编号（历史列名，实为 sku_id 语义）',
    quantity INT NOT NULL COMMENT '商品数量',
    selected BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否选中购物车商品',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '购物车乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间，支撑过期清理与最近加入排序',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_trade_cart_member_product(member_id,product_id),
    KEY idx_cart_member_create(member_id,create_time),
    CONSTRAINT chk_trade_cart_quantity CHECK(quantity BETWEEN 1 AND 999)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员购物车商品';

CREATE TABLE IF NOT EXISTS promotion_coupon_template (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    code VARCHAR(64) NOT NULL COMMENT '业务编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    kind VARCHAR(16) NOT NULL COMMENT '优惠券类型：FIXED固定金额、PERCENT比例折扣',
    threshold DECIMAL(19,2) NOT NULL COMMENT '优惠使用门槛金额',
    fixed_amount DECIMAL(19,2) NULL COMMENT '固定优惠金额，单位元',
    discount_percent INT NULL COMMENT '优惠券支付比例，百分比刻度：80表示支付原价的80%；与 promotion_discount_product.discount_percent 的万分比刻度互斥',
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
    CONSTRAINT chk_trade_coupon_template_amount CHECK(threshold>=0),
    CONSTRAINT chk_coupon_template_discount_percent CHECK(discount_percent IS NULL OR discount_percent BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '优惠券模板及发放规则';

CREATE TABLE IF NOT EXISTS promotion_reward_activity (
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
    CONSTRAINT chk_reward_scope CHECK(product_scope IN ('ALL','SPU_IDS','CATEGORY_IDS')),
    KEY idx_reward_activity_sweep(status,start_time,end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '满减赠送活动';

CREATE TABLE IF NOT EXISTS promotion_discount_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    start_time DATETIME(3) NOT NULL COMMENT '开始时间',
    end_time DATETIME(3) NOT NULL COMMENT '结束时间',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    KEY idx_discount_activity_sweep(status,start_time,end_time),
    CONSTRAINT chk_discount_status CHECK(status IN (0,1)),
    CONSTRAINT chk_discount_time CHECK(end_time>start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动';

CREATE TABLE IF NOT EXISTS promotion_seckill_config (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    start_time VARCHAR(8) NOT NULL COMMENT '秒杀时段开始时刻，HH:mm:ss',
    end_time VARCHAR(8) NOT NULL COMMENT '秒杀时段结束时刻，HH:mm:ss',
    slider_pic_urls LONGTEXT NOT NULL COMMENT '轮播图片地址JSON数组',
    status INT NOT NULL DEFAULT 0 COMMENT '业务状态',
    CONSTRAINT chk_seckill_config_status CHECK(status IN (0,1)),
    CONSTRAINT chk_seckill_config_time CHECK(start_time<end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀时段配置';

CREATE TABLE IF NOT EXISTS promotion_seckill_activity (
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
    KEY idx_seckill_activity_sweep(status,start_time,end_time),
    CONSTRAINT chk_seckill_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_seckill_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_seckill_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动';

CREATE TABLE IF NOT EXISTS promotion_combination_activity (
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
    KEY idx_combination_activity_sweep(status,start_time,end_time),
    CONSTRAINT chk_combination_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_combination_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_combination_activity_user_size CHECK(user_size>=2),
    CONSTRAINT chk_combination_activity_duration CHECK(limit_duration>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动';

CREATE TABLE IF NOT EXISTS promotion_combination_record (
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
    KEY idx_combination_record_activity(activity_id,status),
    KEY idx_combination_record_expire(head_id,status,expire_time),
    KEY idx_combination_record_sweep(status,expire_time),
    CONSTRAINT chk_combination_record_status CHECK(status IN (0,1,2)),
    CONSTRAINT chk_combination_record_count CHECK(count>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团参与记录';

CREATE TABLE IF NOT EXISTS promotion_bargain_activity (
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
    KEY idx_bargain_activity_sweep(status,start_time,end_time),
    CONSTRAINT chk_bargain_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_bargain_activity_time CHECK(end_time>start_time),
    CONSTRAINT chk_bargain_activity_price CHECK(bargain_first_price>0 AND bargain_min_price>=0 AND bargain_min_price<bargain_first_price),
    CONSTRAINT chk_bargain_activity_random CHECK(random_min_price>0 AND random_max_price>=random_min_price),
    CONSTRAINT chk_bargain_activity_stock CHECK(stock>=0 AND total_stock>=stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价活动';

CREATE TABLE IF NOT EXISTS promotion_point_activity (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '活动配置总库存',
    KEY idx_point_activity_status(status),
    CONSTRAINT chk_point_activity_status CHECK(status IN (0,1)),
    CONSTRAINT chk_point_activity_stock CHECK(stock>=0 AND total_stock>=0 AND stock<=total_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动';

CREATE TABLE IF NOT EXISTS promotion_banner (
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

CREATE TABLE IF NOT EXISTS promotion_kefu_conversation (
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

CREATE TABLE IF NOT EXISTS promotion_kefu_message (
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

CREATE TABLE IF NOT EXISTS promotion_diy_template (
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

CREATE TABLE IF NOT EXISTS promotion_diy_page (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    template_id BIGINT NOT NULL COMMENT '装修模板编号，关联 promotion_diy_template.id',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    remark VARCHAR(255) NULL COMMENT '备注说明',
    preview_pic_urls LONGTEXT NOT NULL COMMENT '预览图片地址JSON数组',
    property LONGTEXT NOT NULL COMMENT '装修组件配置JSON',
    UNIQUE KEY uk_diy_page_template_name(template_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '商城装修页面';

CREATE TABLE IF NOT EXISTS promotion_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    template_id BIGINT NOT NULL COMMENT '优惠券模板编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    code VARCHAR(64) NULL COMMENT '业务编码',
    name VARCHAR(500) NULL COMMENT '名称',
    take_type VARCHAR(64) NULL COMMENT '领取方式：ADMIN后台发放、USER会员领取',
    kind VARCHAR(64) NULL COMMENT '优惠券类型：FIXED固定金额、PERCENT比例折扣',
    threshold DECIMAL(19,2) NULL COMMENT '优惠使用门槛金额',
    fixed_amount DECIMAL(19,2) NULL COMMENT '固定优惠金额，单位元',
    discount_percent INT NULL COMMENT '领取时支付比例百分比快照，80表示支付原价的80%',
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
    CONSTRAINT fk_trade_coupon_template FOREIGN KEY(template_id) REFERENCES promotion_coupon_template(id),
    CONSTRAINT chk_trade_coupon_status CHECK(status IN ('AVAILABLE','LOCKED','USED','EXPIRED')),
    CONSTRAINT chk_coupon_discount_percent CHECK(discount_percent IS NULL OR discount_percent BETWEEN 1 AND 100),
    KEY idx_trade_coupon_member_status(member_id,status),
    source_order_no VARCHAR(40) NULL COMMENT '营销赠券来源订单号',
    gift_sequence INT NULL COMMENT '同一订单、模板和会员下的赠券发放序号，从1开始',
    UNIQUE KEY uk_mall_coupon_gift (source_order_no, template_id, member_id, gift_sequence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员持券及领取时规则快照';

CREATE TABLE IF NOT EXISTS promotion_discount_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    discount_type VARCHAR(16) NOT NULL COMMENT '优惠方式：PERCENT比例折扣、PRICE固定减价',
    discount_percent INT NULL COMMENT '支付比例，万分比刻度：8000表示支付原价的80%；与券表的百分比刻度互斥',
    discount_price DECIMAL(19,2) NULL COMMENT '优惠减免金额，单位元',
    UNIQUE KEY uk_discount_activity_sku(activity_id,sku_id),
    CONSTRAINT fk_discount_activity FOREIGN KEY(activity_id) REFERENCES promotion_discount_activity(id),
    CONSTRAINT chk_discount_product CHECK((discount_type='PERCENT' AND discount_percent BETWEEN 1 AND 9999)
 OR (discount_type='PRICE' AND discount_price>0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '限时折扣活动商品';

CREATE TABLE IF NOT EXISTS promotion_seckill_product (
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
    CONSTRAINT fk_seckill_product_activity FOREIGN KEY(activity_id) REFERENCES promotion_seckill_activity(id),
    CONSTRAINT chk_seckill_product_price CHECK(seckill_price>0),
    CONSTRAINT chk_seckill_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '秒杀活动商品';

CREATE TABLE IF NOT EXISTS promotion_combination_product (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    combination_price DECIMAL(19,2) NOT NULL COMMENT '拼团成交单价，单位元',
    activity_status INT NOT NULL DEFAULT 0 COMMENT '活动状态快照',
    activity_start_time DATETIME(3) NOT NULL COMMENT '活动开始时间快照',
    activity_end_time DATETIME(3) NOT NULL COMMENT '活动结束时间快照',
    UNIQUE KEY uk_combination_activity_sku(activity_id,sku_id),
    CONSTRAINT fk_combination_product_activity FOREIGN KEY(activity_id) REFERENCES promotion_combination_activity(id),
    CONSTRAINT chk_combination_product_price CHECK(combination_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '拼团活动商品';

CREATE TABLE IF NOT EXISTS promotion_bargain_record (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    spu_id BIGINT NOT NULL COMMENT '商品SPU编号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    bargain_first_price DECIMAL(19,2) NOT NULL COMMENT '砍价起始金额，单位元',
    bargain_min_price DECIMAL(19,2) NOT NULL COMMENT '开砍时冻结的砍价底价，单位元；达标判定以此为准，不随活动配置变更重算',
    bargain_price DECIMAL(19,2) NOT NULL COMMENT '当前砍价金额，单位元',
    status INT NOT NULL DEFAULT 1 COMMENT '砍价状态：1进行中、2成功、3失败',
    end_time DATETIME(3) NULL COMMENT '结束时间',
    order_id BIGINT NULL COMMENT '交易订单主键编号',
    KEY idx_bargain_record_user(user_id,activity_id,status),
    KEY idx_bargain_record_activity(activity_id,status),
    CONSTRAINT fk_bargain_record_activity FOREIGN KEY(activity_id) REFERENCES promotion_bargain_activity(id),
    CONSTRAINT chk_bargain_record_status CHECK(status IN (1,2,3)),
    CONSTRAINT chk_bargain_record_price CHECK(bargain_price>=0 AND bargain_price<=bargain_first_price),
    CONSTRAINT chk_bargain_record_min CHECK(bargain_min_price>=0 AND bargain_min_price<=bargain_first_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员砍价参与记录';

CREATE TABLE IF NOT EXISTS promotion_point_product (
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
    CONSTRAINT fk_point_product_activity FOREIGN KEY(activity_id) REFERENCES promotion_point_activity(id),
    CONSTRAINT chk_point_product_count CHECK(exchange_count>0),
    CONSTRAINT chk_point_product_point CHECK(point>=0),
    CONSTRAINT chk_point_product_price CHECK(price>=0),
    CONSTRAINT chk_point_product_stock CHECK(stock>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '积分商城活动商品';

CREATE TABLE IF NOT EXISTS promotion_bargain_help (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    activity_id BIGINT NOT NULL COMMENT '活动编号',
    record_id BIGINT NOT NULL COMMENT '活动参与记录编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    reduce_price DECIMAL(19,2) NOT NULL COMMENT '本次助力减免金额，单位元',
    UNIQUE KEY uk_bargain_help_record_user(record_id,user_id),
    KEY idx_bargain_help_user_activity(user_id,activity_id),
    CONSTRAINT fk_bargain_help_record FOREIGN KEY(record_id) REFERENCES promotion_bargain_record(id),
    CONSTRAINT chk_bargain_help_price CHECK(reduce_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '砍价助力记录';

CREATE TABLE IF NOT EXISTS promotion_message_inbox (
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
