-- lemnt_trade：最终表结构初始化。仅用于建库，不提供存量升级或历史回填。
-- 本服务没有必须预置的业务数据；菜单、字典与默认参数归system，本次保持原样。

CREATE TABLE IF NOT EXISTS trade_order (
    original_amount DECIMAL(19,2) NULL COMMENT '下单时商品原价合计，单位元，历史未知为NULL',
    discount_amount DECIMAL(19,2) NULL COMMENT '商品优惠总额，含实际积分抵现金额，单位元，历史未知为NULL',
    delivery_original_price DECIMAL(19,2) NULL COMMENT '运费减免前应收运费，单位元，历史未知为NULL',
    delivery_discount_price DECIMAL(19,2) NULL COMMENT '运费优惠金额，单位元，历史未知为NULL',
    adjustment_amount DECIMAL(19,2) NULL COMMENT '人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
    pricing_snapshot_status VARCHAR(20) NOT NULL DEFAULT 'LEGACY_PARTIAL' COMMENT '金额快照状态：COMPLETE完整、LEGACY_PARTIAL历史不完整',
    pricing_snapshot_version INT NOT NULL DEFAULT 0 COMMENT '金额快照版本，历史未记录为0，支付单生成前调价递增',
    CONSTRAINT chk_trade_order_pricing_status CHECK(pricing_snapshot_status IN ('COMPLETE','LEGACY_PARTIAL')),
    CONSTRAINT chk_trade_order_discount CHECK(discount_amount IS NULL OR discount_amount>=0),
    CONSTRAINT chk_trade_order_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price)),
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    total_amount DECIMAL(19,2) NOT NULL COMMENT '订单应付总金额，单位元',
    use_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单使用积分数',
    give_point BIGINT NOT NULL DEFAULT 0 COMMENT '订单赠送积分数',
    address_id BIGINT NULL COMMENT '会员收货地址编号',
    delivery_type INT NULL COMMENT '配送方式：1快递、2自提',
    receiver_name VARCHAR(100) NULL COMMENT '收件人姓名快照',
    receiver_mobile VARCHAR(32) NULL COMMENT '收件手机号码快照',
    receiver_area_id BIGINT NULL COMMENT '收件地区编号快照',
    receiver_detail_address VARCHAR(500) NULL COMMENT '收件详细地址快照',
    delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '运费金额，单位元',
    status VARCHAR(32) NOT NULL COMMENT '订单状态：RESERVING预占中、PENDING_PAYMENT待支付、PAID已支付、SHIPPED已发货、COMPLETED已完成、CLOSED已关闭、REFUNDED已退款',
    comment_status BOOLEAN NULL COMMENT '是否已评价，历史未知可为空',
    finish_time DATETIME NULL COMMENT '订单完成时间',
    receive_time DATETIME NULL COMMENT '订单确认收货时间',
    remark VARCHAR(500) NULL COMMENT '备注说明',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_order_no(order_no),
    KEY idx_trade_order_member_created(member_id,create_time),
    KEY idx_trade_order_status_created(status,create_time),
    CONSTRAINT chk_trade_order_total CHECK(total_amount>=0),
    pick_up_store_id BIGINT NULL COMMENT '自提门店编号',
    pick_up_verify_code VARCHAR(8) NULL COMMENT '8位自提核销码',
    UNIQUE KEY uk_trade_order_pick_up_code(pick_up_verify_code),
    give_coupon_template_counts LONGTEXT NULL COMMENT '下单时赠券模板编号与数量JSON映射'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单';

CREATE TABLE IF NOT EXISTS trade_payment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    payment_no VARCHAR(40) NOT NULL COMMENT '支付业务单号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    channel VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
    app_key VARCHAR(64) NULL COMMENT '支付应用标识',
    channel_trade_no VARCHAR(128) NULL COMMENT '渠道支付流水号',
    amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '支付状态：WAITING待支付、SUCCESS支付成功、CLOSED已关闭',
    notify_id VARCHAR(128) NULL COMMENT '支付通知唯一标识',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_payment_no(payment_no),
    UNIQUE KEY uk_trade_payment_order_channel(order_no,channel),
    UNIQUE KEY uk_trade_payment_notify(notify_id),
    KEY idx_trade_payment_status(status,create_time),
    KEY idx_trade_payment_app(app_key,channel),
    CONSTRAINT chk_trade_payment_amount CHECK(amount>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

CREATE TABLE IF NOT EXISTS trade_delivery_express (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    code VARCHAR(40) NOT NULL COMMENT '快递公司编码',
    name VARCHAR(100) NOT NULL COMMENT '快递公司名称',
    logo VARCHAR(500) COMMENT '图标地址',
    sort INT NOT NULL COMMENT '排序',
    status INT NOT NULL COMMENT '状态，0 启用、1 停用',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_delivery_express_code(code),
    CONSTRAINT chk_delivery_express_status CHECK(status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '快递公司';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    charge_mode INT NOT NULL COMMENT '计费方式，1 件数、2 重量千克、3 体积立方米',
    sort INT NOT NULL COMMENT '排序',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_delivery_template_name(name),
    CONSTRAINT chk_delivery_charge_mode CHECK(charge_mode IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '运费模板';

CREATE TABLE IF NOT EXISTS trade_pay_app (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    app_key VARCHAR(64) NOT NULL COMMENT '支付应用标识',
    name VARCHAR(64) NOT NULL COMMENT '名称',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    remark VARCHAR(255) NULL COMMENT '备注说明',
    order_notify_url VARCHAR(255) NULL COMMENT '支付成功业务通知地址',
    refund_notify_url VARCHAR(255) NULL COMMENT '退款业务通知地址',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_pay_app_key(app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付应用';

CREATE TABLE IF NOT EXISTS trade_pay_notify_task (
    execute_version BIGINT NOT NULL DEFAULT 0 COMMENT '单调执行版本，领取和人工重置均递增，完成时按版本CAS',
    lease_until DATETIME NULL COMMENT '当前执行租约到期时间，使用数据库时间，过期允许重新领取',
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    type VARCHAR(16) NOT NULL COMMENT '派发业务类型：PAY支付、REFUND历史退款、AFTER_SALE售后权益',
    biz_no VARCHAR(40) NOT NULL COMMENT '业务单号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '派发状态：PENDING待派发、SUCCESS成功、FAILED失败待重试',
    notify_times INT NOT NULL DEFAULT 0 COMMENT '已派发尝试次数',
    max_notify_times INT NOT NULL DEFAULT 8 COMMENT '最大派发尝试次数',
    next_notify_time DATETIME NULL COMMENT '下次计划派发时间',
    last_execute_time DATETIME NULL COMMENT '最近一次派发执行时间',
    last_response VARCHAR(500) NULL COMMENT '最近一次派发结果摘要',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_pay_notify_biz(type,biz_no),
    KEY idx_trade_pay_notify_due(status,next_notify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果下游派发任务';

CREATE TABLE IF NOT EXISTS trade_member_wallet (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    balance DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '可用余额，单位元',
    freeze_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '冻结余额，单位元',
    total_expense DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '累计支出金额，单位元',
    total_recharge DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '累计充值金额，单位元',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_member_wallet(member_id),
    CONSTRAINT chk_wallet_balance CHECK(balance>=0),
    CONSTRAINT chk_wallet_freeze CHECK(freeze_price>=0),
    CONSTRAINT chk_wallet_expense CHECK(total_expense>=0),
    CONSTRAINT chk_wallet_recharge CHECK(total_recharge>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包账户';

CREATE TABLE IF NOT EXISTS trade_pay_recharge_package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    name VARCHAR(64) NOT NULL COMMENT '名称',
    pay_price DECIMAL(19,2) NOT NULL COMMENT '实付金额，单位元',
    bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '充值赠送金额，单位元',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_recharge_package_name(name),
    CONSTRAINT chk_recharge_package_pay CHECK(pay_price>0),
    CONSTRAINT chk_recharge_package_bonus CHECK(bonus_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值套餐';

CREATE TABLE IF NOT EXISTS trade_brokerage_user (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '分销会员编号，关联会员账户',
    bind_user_id BIGINT NULL COMMENT '绑定的上级分销会员编号',
    bind_user_time DATETIME(3) NULL COMMENT '上级关系绑定时间',
    brokerage_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用分销资格',
    brokerage_time DATETIME(3) NULL COMMENT '分销资格开通时间',
    brokerage_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '可提现佣金余额，单位元',
    frozen_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '冻结佣金余额，单位元',
    KEY idx_brokerage_bind_user(bind_user_id),
    CONSTRAINT chk_brokerage_user_price CHECK(brokerage_price>=0 AND frozen_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销会员及佣金账户';

CREATE TABLE IF NOT EXISTS trade_brokerage_record (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务关联编号',
    biz_type INT NOT NULL COMMENT '佣金业务类型：1订单、2提现、3提现驳回、4订单退款扣回',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description VARCHAR(255) NULL COMMENT '描述说明',
    price DECIMAL(19,2) NOT NULL COMMENT '本次佣金变更金额，扣减为负，单位元',
    total_price DECIMAL(19,2) NOT NULL COMMENT '变更后可用与冻结佣金合计，单位元',
    status INT NOT NULL COMMENT '佣金状态：0待结算、1已结算、2已取消',
    frozen_days INT NULL COMMENT '佣金冻结天数',
    unfreeze_time DATETIME(3) NULL COMMENT '佣金计划解冻时间',
    source_user_level INT NULL COMMENT '来源分销层级，1一级、2二级',
    source_user_id BIGINT NULL COMMENT '来源会员编号',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_brokerage_record_biz(user_id,biz_type,biz_id),
    KEY idx_brokerage_record_user(user_id,id),
    KEY idx_brokerage_record_unfreeze(status,unfreeze_time),
    CONSTRAINT chk_brokerage_record_status CHECK(status IN (0,1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销佣金变更记录';

CREATE TABLE IF NOT EXISTS trade_brokerage_withdraw (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    price DECIMAL(19,2) NOT NULL COMMENT '申请提现佣金，单位元',
    fee_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '手续费金额，单位元',
    total_price DECIMAL(19,2) NOT NULL COMMENT '扣除手续费后的实际到账金额，单位元',
    type INT NOT NULL COMMENT '提现方式：1钱包、2银行、3微信收款码、4支付宝收款码',
    user_name VARCHAR(64) NOT NULL COMMENT '提现收款人姓名',
    user_account VARCHAR(128) NOT NULL COMMENT '提现收款账号',
    qr_code_url VARCHAR(500) NULL COMMENT '收款二维码地址',
    bank_name VARCHAR(100) NULL COMMENT '开户银行名称',
    bank_address VARCHAR(255) NULL COMMENT '开户行地址',
    status INT NOT NULL COMMENT '提现状态：0待审核、10审核通过、11已到账、20审核拒绝',
    audit_reason VARCHAR(255) NULL COMMENT '审核说明',
    audit_time DATETIME(3) NULL COMMENT '审核时间',
    remark VARCHAR(255) NULL COMMENT '备注说明',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_brokerage_withdraw_user(user_id,id),
    KEY idx_brokerage_withdraw_status(status,id),
    CONSTRAINT chk_brokerage_withdraw_price CHECK(price>0 AND fee_price>=0 AND fee_price<=price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '分销佣金提现申请';

CREATE TABLE IF NOT EXISTS trade_delivery_pick_up_store (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    introduction VARCHAR(500) NULL COMMENT '简介',
    phone VARCHAR(32) NOT NULL COMMENT '联系电话',
    area_id BIGINT NOT NULL COMMENT '行政区划编号',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    logo VARCHAR(500) NULL COMMENT '图标地址',
    opening_time VARCHAR(8) NOT NULL COMMENT '门店开门时间，HH:mm:ss；与关门相同表示全天营业',
    closing_time VARCHAR(8) NOT NULL COMMENT '门店关门时间，HH:mm:ss；早于开门表示跨夜',
    latitude DECIMAL(10,6) NULL COMMENT '门店纬度',
    longitude DECIMAL(10,6) NULL COMMENT '门店经度',
    verify_user_ids LONGTEXT NOT NULL COMMENT '允许核销的管理员编号JSON数组',
    status INT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    KEY idx_pick_up_store_status(status),
    CONSTRAINT chk_pick_up_store_status CHECK(status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '自提门店及核销店员';

CREATE TABLE IF NOT EXISTS trade_order_item (
    discount_amount DECIMAL(19,2) NULL COMMENT '本行商品优惠合计，单位元，历史未知为NULL',
    delivery_original_price DECIMAL(19,2) NULL COMMENT '分摊至本行的原始应收运费，单位元，历史未知为NULL',
    delivery_discount_price DECIMAL(19,2) NULL COMMENT '分摊至本行的运费优惠，单位元，历史未知为NULL',
    adjustment_amount DECIMAL(19,2) NULL COMMENT '本行人工调价差额，正数加价负数减价，单位元，历史未知为NULL',
    use_point BIGINT NULL COMMENT '分摊至本行的使用积分数量，历史未知为NULL',
    give_point BIGINT NULL COMMENT '分摊至本行的赠送积分数量，历史未知为NULL',
    refunded_quantity INT NULL COMMENT '已成功退款数量，售后首次成功同事务增加，历史未知为NULL',
    CONSTRAINT chk_trade_item_discount CHECK(discount_amount IS NULL OR (discount_amount>=0 AND discount_amount<=line_amount)),
    CONSTRAINT chk_trade_item_delivery_discount CHECK(delivery_discount_price IS NULL OR
   (delivery_original_price IS NOT NULL AND delivery_discount_price>=0 AND delivery_discount_price<=delivery_original_price)),
    CONSTRAINT chk_trade_item_points CHECK((use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0)),
    CONSTRAINT chk_trade_item_refund_quantity CHECK(
   refunded_quantity IS NULL OR (refunded_quantity>=0 AND refunded_quantity<=quantity)),
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    order_id BIGINT NOT NULL COMMENT '交易订单主键编号',
    comment_status BOOLEAN NULL COMMENT '是否已评价，历史未知可为空',
    sku_id BIGINT NOT NULL COMMENT '商品SKU编号',
    spu_id BIGINT NULL COMMENT '商品SPU编号',
    spu_name VARCHAR(200) NULL COMMENT '商品SPU名称快照',
    specs LONGTEXT NULL COMMENT '下单时规格JSON快照；未知为NULL、已知无规格为[]；元素specId/specName/specValueId/specValueName',
    pic_url VARCHAR(1024) NULL COMMENT '图片地址',
    product_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码快照',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照',
    unit_price DECIMAL(19,2) NOT NULL COMMENT '商品单价，单位元',
    quantity INT NOT NULL COMMENT '商品数量',
    line_amount DECIMAL(19,2) NOT NULL COMMENT '商品行原价合计，单位元',
    pay_amount DECIMAL(19,2) NULL COMMENT '商品行实付金额，单位元',
    delivery_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '分摊至商品行的运费，单位元',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    KEY idx_trade_order_item_order(order_id),
    CONSTRAINT fk_trade_order_item_order FOREIGN KEY(order_id) REFERENCES trade_order(id),
    CONSTRAINT chk_trade_order_item_quantity CHECK(quantity>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单明细';

CREATE TABLE IF NOT EXISTS trade_inventory_confirmation (
    order_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '交易订单业务单号',
    confirmed_at DATETIME NOT NULL COMMENT '库存确认时间',
    CONSTRAINT fk_trade_inventory_confirmation_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '交易订单库存确认记录';

CREATE TABLE IF NOT EXISTS trade_shipment (
    order_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '交易订单业务单号',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
    fingerprint VARCHAR(64) NOT NULL COMMENT '请求内容指纹，用于幂等核对',
    carrier_code VARCHAR(40) NOT NULL COMMENT '快递公司编码',
    tracking_no VARCHAR(100) NOT NULL COMMENT '快递运单号',
    recipient_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
    phone VARCHAR(32) NOT NULL COMMENT '联系电话',
    address VARCHAR(500) NOT NULL COMMENT '收件详细地址快照',
    shipped_at DATETIME NOT NULL COMMENT '发货时间',
    received_at DATETIME NULL COMMENT '签收时间',
    CONSTRAINT fk_trade_shipment_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '订单发货与收件快照';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template_charge (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    template_id BIGINT NOT NULL COMMENT '模板编号',
    area_ids LONGTEXT NOT NULL COMMENT '配送区域编号集合',
    charge_mode INT NOT NULL COMMENT '模板计费方式快照',
    start_count DECIMAL(18,6) NOT NULL COMMENT '首量',
    start_price INT NOT NULL COMMENT '首价，分',
    extra_count DECIMAL(18,6) NOT NULL COMMENT '续量',
    extra_price INT NOT NULL COMMENT '续价，分',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_delivery_charge_template(template_id),
    CONSTRAINT fk_delivery_charge_template FOREIGN KEY(template_id) REFERENCES trade_delivery_express_template(id),
    CONSTRAINT chk_delivery_charge_values CHECK(start_count>0 AND extra_count>0 AND start_price>=0 AND extra_price>=0 AND charge_mode IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '运费模板区域计费规则';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template_free (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    template_id BIGINT NOT NULL COMMENT '模板编号',
    area_ids LONGTEXT NOT NULL COMMENT '配送区域编号集合',
    free_price INT NOT NULL COMMENT '包邮金额门槛，分',
    free_count INT NOT NULL COMMENT '包邮计费量门槛',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_delivery_free_template(template_id),
    CONSTRAINT fk_delivery_free_template FOREIGN KEY(template_id) REFERENCES trade_delivery_express_template(id),
    CONSTRAINT chk_delivery_free_values CHECK(free_price>=0 AND free_count>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '运费模板区域包邮规则';

CREATE TABLE IF NOT EXISTS trade_order_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '操作人编号',
    user_type INT NOT NULL COMMENT '操作人类型：0系统、1会员、2后台管理员',
    order_id BIGINT NOT NULL COMMENT '交易订单主键编号',
    before_status INT NULL COMMENT '操作前订单状态数值编码',
    after_status INT NOT NULL COMMENT '操作后订单状态数值编码',
    operate_type INT NOT NULL COMMENT '订单操作类型编码',
    content VARCHAR(2000) NOT NULL COMMENT '内容',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_trade_order_log_order(order_id,id),
    CONSTRAINT fk_trade_order_log_order FOREIGN KEY(order_id) REFERENCES trade_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志';

CREATE TABLE IF NOT EXISTS trade_pay_channel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    app_id BIGINT NOT NULL COMMENT '支付应用编号',
    code VARCHAR(32) NOT NULL COMMENT '业务编码',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '启停状态：0启用、1停用',
    fee_rate DECIMAL(6,2) NULL COMMENT '渠道手续费百分比',
    remark VARCHAR(255) NULL COMMENT '备注说明',
    config TEXT NULL COMMENT '支付渠道内部JSON配置，包含密钥且不对外明文返回',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除，0否、1是',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_pay_channel(app_id,code),
    KEY idx_trade_pay_channel_app(app_id),
    CONSTRAINT fk_trade_pay_channel_app FOREIGN KEY(app_id) REFERENCES trade_pay_app(id),
    CONSTRAINT chk_trade_pay_channel_fee CHECK(fee_rate IS NULL OR (fee_rate>=0 AND fee_rate<=100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道配置';

CREATE TABLE IF NOT EXISTS trade_pay_notify_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    task_id BIGINT NOT NULL COMMENT '支付结果派发任务编号',
    notify_times INT NOT NULL COMMENT '已派发尝试次数',
    status VARCHAR(20) NOT NULL COMMENT '本次派发结果状态',
    response VARCHAR(500) NULL COMMENT '本次派发结果摘要',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    KEY idx_trade_pay_notify_log_task(task_id),
    CONSTRAINT fk_trade_pay_notify_log_task FOREIGN KEY(task_id) REFERENCES trade_pay_notify_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付结果派发尝试日志';

CREATE TABLE IF NOT EXISTS trade_member_wallet_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    transaction_no VARCHAR(64) NOT NULL COMMENT '钱包流水业务编号',
    wallet_id BIGINT NOT NULL COMMENT '会员钱包编号',
    biz_type VARCHAR(32) NOT NULL COMMENT '钱包业务类型：RECHARGE、RECHARGE_REFUND、PAYMENT、PAYMENT_REFUND、UPDATE_BALANCE、TRANSFER、BROKERAGE_WITHDRAW',
    biz_no VARCHAR(64) NOT NULL COMMENT '业务单号',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    amount DECIMAL(19,2) NOT NULL COMMENT '本次余额变更量，收入为正、支出为负，单位元',
    balance_after DECIMAL(19,2) NOT NULL COMMENT '变更后可用余额，单位元',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_wallet_tx_biz(wallet_id,biz_type,biz_no),
    KEY idx_wallet_tx_wallet(wallet_id,id),
    CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
    CONSTRAINT chk_wallet_tx_amount CHECK(amount<>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包余额流水';

CREATE TABLE IF NOT EXISTS trade_pay_wallet_recharge (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    recharge_no VARCHAR(40) NOT NULL COMMENT '充值业务单号',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
    wallet_id BIGINT NOT NULL COMMENT '会员钱包编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    package_id BIGINT NULL COMMENT '充值套餐编号',
    pay_price DECIMAL(19,2) NOT NULL COMMENT '实付金额，单位元',
    bonus_price DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '充值赠送金额，单位元',
    total_price DECIMAL(19,2) NOT NULL COMMENT '实付与赠送合计入账金额，单位元',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '充值状态：PENDING待支付、PAID已到账、REFUNDED已退款',
    channel_code VARCHAR(32) NULL COMMENT '支付渠道编码',
    paid_time DATETIME NULL COMMENT '支付到账时间',
    refunded_time DATETIME NULL COMMENT '退款完成时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_wallet_recharge_no(recharge_no),
    UNIQUE KEY uk_trade_wallet_recharge_request(request_no),
    KEY idx_trade_wallet_recharge_member(member_id,id),
    KEY idx_trade_wallet_recharge_status(status),
    CONSTRAINT fk_wallet_recharge_wallet FOREIGN KEY(wallet_id) REFERENCES trade_member_wallet(id),
    CONSTRAINT chk_wallet_recharge_total CHECK(total_price>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包充值单';

CREATE TABLE IF NOT EXISTS trade_member_wallet_transfer (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    transfer_no VARCHAR(40) NOT NULL COMMENT '转账业务单号',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
    from_wallet_id BIGINT NOT NULL COMMENT '转出钱包编号',
    from_member_id BIGINT NOT NULL COMMENT '转出会员编号',
    to_wallet_id BIGINT NOT NULL COMMENT '转入钱包编号',
    to_member_id BIGINT NOT NULL COMMENT '转入会员编号',
    amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
    remark VARCHAR(128) NULL COMMENT '备注说明',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '转账状态：SUCCESS成功，同步转出转入',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_wallet_transfer_no(transfer_no),
    UNIQUE KEY uk_wallet_transfer_request(request_no),
    KEY idx_wallet_transfer_from(from_member_id,id),
    KEY idx_wallet_transfer_to(to_member_id,id),
    CONSTRAINT fk_wallet_transfer_from FOREIGN KEY(from_wallet_id) REFERENCES trade_member_wallet(id),
    CONSTRAINT fk_wallet_transfer_to FOREIGN KEY(to_wallet_id) REFERENCES trade_member_wallet(id),
    CONSTRAINT chk_wallet_transfer_amount CHECK(amount>0),
    CONSTRAINT chk_wallet_transfer_distinct CHECK(from_member_id<>to_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员钱包转账';

CREATE TABLE IF NOT EXISTS trade_order_promotion (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    order_id BIGINT NOT NULL COMMENT '所属交易订单主键编号',
    promotion_key VARCHAR(96) NOT NULL COMMENT '订单内稳定优惠标识，用于重放校验与去重',
    promotion_type VARCHAR(32) NOT NULL COMMENT '优惠来源：SECKILL、COMBINATION、BARGAIN、POINT_EXCHANGE、DISCOUNT、REWARD、COUPON、POINT_DEDUCT、MEMBER_LEVEL、ORDER_GIFT',
    source_id BIGINT NULL COMMENT '来源活动、会员券或等级等编号，按优惠类型解释，无来源编号时为NULL',
    promotion_name VARCHAR(200) NULL COMMENT '下单时优惠名称快照，历史未知为NULL，不读取当前名称回填',
    rule_summary VARCHAR(500) NULL COMMENT '下单时优惠规则展示摘要，历史未知为NULL',
    rule_snapshot LONGTEXT NULL COMMENT '实际使用的计算因子JSON快照，仅保存核对和支付前调价需要的规则',
    discount_amount DECIMAL(19,2) NULL COMMENT '该项商品优惠金额，单位元，历史未知为NULL',
    delivery_discount_price DECIMAL(19,2) NULL COMMENT '该项运费优惠金额，单位元，历史未知为NULL',
    use_point BIGINT NULL COMMENT '该项使用积分数量，历史未知为NULL',
    give_point BIGINT NULL COMMENT '该项赠送积分数量，历史未知为NULL',
    give_coupon_template_counts LONGTEXT NULL COMMENT '该来源赠券模板编号与数量JSON映射，已知无赠券为{}，历史未知为NULL',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_order_promotion_key(order_id,promotion_key),
    CONSTRAINT fk_trade_order_promotion_order FOREIGN KEY(order_id) REFERENCES trade_order(id),
    CONSTRAINT chk_trade_order_promotion_amount CHECK(
   (discount_amount IS NULL OR discount_amount>=0) AND (delivery_discount_price IS NULL OR delivery_discount_price>=0)),
    CONSTRAINT chk_trade_order_promotion_points CHECK(
   (use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单实际采用的促销与权益结果快照';

CREATE TABLE IF NOT EXISTS trade_after_sale (
    refund_discount_amount DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次商品优惠归因，非额外退现金，单位元',
    refund_delivery_discount_price DECIMAL(19,2) NULL COMMENT '生成退单时冻结的本次运费优惠归因，非额外退现金，单位元',
    return_use_point BIGINT NULL COMMENT '生成退单时冻结的应返使用积分，实际到账以账户流水为准',
    deduct_give_point BIGINT NULL COMMENT '生成退单时冻结的应回收赠送积分，实际扣回以账户流水为准',
    refund_rule_version INT NULL COMMENT '退款分摊规则版本，1为现金单件基准末次尾差及积分累计比例，历史未知为NULL',
    CONSTRAINT chk_trade_after_sale_promotion_amount CHECK(
   (refund_discount_amount IS NULL OR refund_discount_amount>=0) AND
   (refund_delivery_discount_price IS NULL OR refund_delivery_discount_price>=0)),
    CONSTRAINT chk_trade_after_sale_points CHECK(
   (return_use_point IS NULL OR return_use_point>=0) AND (deduct_give_point IS NULL OR deduct_give_point>=0)),
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    refund_no VARCHAR(40) NOT NULL COMMENT '售后业务单号',
    order_id BIGINT NULL COMMENT '交易订单主键编号',
    sku_id BIGINT NULL COMMENT '商品SKU编号',
    spu_id BIGINT NULL COMMENT '商品SPU编号',
    spu_name VARCHAR(200) NULL COMMENT '商品SPU名称快照',
    specs LONGTEXT NULL COMMENT '申请时原订单规格JSON快照，历史未知为NULL，不读取当前商品回填',
    pic_url VARCHAR(1024) NULL COMMENT '图片地址',
    refund_time DATETIME NULL COMMENT '退款完成时间',
    audit_user_id BIGINT NULL COMMENT '审核管理员编号',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    UNIQUE KEY uk_after_sale_no(refund_no),
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    payment_no VARCHAR(40) NULL COMMENT '支付业务单号',
    amount DECIMAL(19,2) NOT NULL COMMENT '申请退款金额，单位元',
    reason VARCHAR(500) NOT NULL COMMENT '业务原因说明',
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '售后状态：REQUESTED待审核、REJECTED拒绝、AWAITING_RETURN待寄回、RETURNED已寄回、WAIT_REFUND待退款、RETURN_REJECTED拒收、REFUNDING退款中、REFUNDED已退款、CANCELLED取消',
    active_order_no VARCHAR(40) NULL COMMENT '处理中售后关联订单号，用于限制同订单活动售后',
    decision_reason VARCHAR(500) NULL COMMENT '售后处理说明',
    order_item_id BIGINT NULL COMMENT '交易订单明细主键编号',
    quantity INT NULL COMMENT '商品数量',
    type VARCHAR(20) NOT NULL DEFAULT 'REFUND_ONLY' COMMENT '售后类型：REFUND_ONLY仅退款、RETURN_REFUND退货退款',
    return_carrier_code VARCHAR(40) NULL COMMENT '退货快递公司编码',
    return_tracking_no VARCHAR(100) NULL COMMENT '退货快递运单号',
    return_shipped_at DATETIME NULL COMMENT '会员寄出退货时间',
    return_received_at DATETIME NULL COMMENT '商家收到退货时间',
    requested_at DATETIME NOT NULL COMMENT '售后申请时间',
    decided_at DATETIME NULL COMMENT '售后审核决定时间',
    UNIQUE KEY uk_trade_refund_order_request(order_no,request_no),
    UNIQUE KEY uk_trade_refund_active(active_order_no),
    KEY idx_trade_refund_status_time(status,requested_at),
    CONSTRAINT fk_trade_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
    CONSTRAINT fk_trade_refund_item FOREIGN KEY(order_item_id) REFERENCES trade_order_item(id),
    CONSTRAINT chk_trade_refund_item CHECK((order_item_id IS NULL AND quantity IS NULL)
   OR (order_item_id IS NOT NULL AND quantity IS NOT NULL AND quantity>0)),
    CONSTRAINT chk_trade_refund_type CHECK(type IN ('REFUND_ONLY','RETURN_REFUND')),
    CONSTRAINT fk_trade_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
    CONSTRAINT chk_trade_refund_amount CHECK(amount>=0),
    CONSTRAINT chk_trade_refund_status CHECK(status IN ('REQUESTED','REJECTED','AWAITING_RETURN','RETURNED','WAIT_REFUND','RETURN_REJECTED','REFUNDING','REFUNDED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '订单售后申请及商品快照';

CREATE TABLE IF NOT EXISTS trade_order_item_promotion (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    order_promotion_id BIGINT NOT NULL COMMENT '原订单优惠结果主键编号',
    order_item_id BIGINT NOT NULL COMMENT '原订单商品明细主键编号，不使用SKU编号替代',
    discount_amount DECIMAL(19,2) NULL COMMENT '该优惠分摊至本行的商品优惠金额，单位元，历史未知为NULL',
    delivery_discount_price DECIMAL(19,2) NULL COMMENT '该优惠分摊至本行的运费优惠金额，单位元，历史未知为NULL',
    use_point BIGINT NULL COMMENT '该来源分摊至本行的使用积分数量，历史未知为NULL',
    give_point BIGINT NULL COMMENT '该来源分摊至本行的赠送积分数量，历史未知为NULL',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_item_promotion(order_promotion_id,order_item_id),
    KEY idx_trade_item_promotion_item(order_item_id),
    CONSTRAINT fk_trade_item_promotion_header FOREIGN KEY(order_promotion_id) REFERENCES trade_order_promotion(id),
    CONSTRAINT fk_trade_item_promotion_item FOREIGN KEY(order_item_id) REFERENCES trade_order_item(id),
    CONSTRAINT chk_trade_item_promotion_amount CHECK(
   (discount_amount IS NULL OR discount_amount>=0) AND (delivery_discount_price IS NULL OR delivery_discount_price>=0)),
    CONSTRAINT chk_trade_item_promotion_points CHECK(
   (use_point IS NULL OR use_point>=0) AND (give_point IS NULL OR give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单商品明细优惠与权益分摊快照';

CREATE TABLE IF NOT EXISTS trade_payment_refund (
    refund_no VARCHAR(40) NOT NULL PRIMARY KEY COMMENT '退款业务单号',
    after_sale_no VARCHAR(40) NOT NULL COMMENT '售后业务单号',
    payment_no VARCHAR(40) NULL COMMENT '支付业务单号',
    order_no VARCHAR(40) NOT NULL COMMENT '交易订单业务单号',
    channel VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
    amount DECIMAL(19,2) NOT NULL COMMENT '业务金额，单位元',
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '退款状态：WAITING待退款、SUCCESS成功、FAILED失败',
    channel_refund_no VARCHAR(128) NULL COMMENT '渠道退款流水号',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_payment_refund_after_sale(after_sale_no),
    KEY idx_payment_refund_payment(payment_no),
    KEY idx_payment_refund_status_time(status,create_time),
    CONSTRAINT fk_payment_refund_after_sale FOREIGN KEY(after_sale_no) REFERENCES trade_after_sale(refund_no),
    CONSTRAINT fk_payment_refund_payment FOREIGN KEY(payment_no) REFERENCES trade_payment(payment_no),
    CONSTRAINT fk_payment_refund_order FOREIGN KEY(order_no) REFERENCES trade_order(order_no),
    CONSTRAINT chk_payment_refund_amount CHECK(amount>=0),
    CONSTRAINT chk_payment_refund_status CHECK(status IN ('WAITING','SUCCESS','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后原路支付退款交易';

CREATE TABLE IF NOT EXISTS trade_after_sale_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '操作人编号',
    user_type INT NOT NULL COMMENT '操作人类型：0系统、1会员、2后台管理员',
    after_sale_id BIGINT NOT NULL COMMENT '售后单主键编号',
    before_status INT NULL COMMENT '操作前售后状态数值编码',
    after_status INT NOT NULL COMMENT '操作后售后状态数值编码',
    operate_type INT NOT NULL COMMENT '售后操作类型编码',
    content VARCHAR(2000) NOT NULL COMMENT '内容',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否逻辑删除，0否、1是',
    KEY idx_trade_after_sale_log_order(after_sale_id,id),
    CONSTRAINT fk_trade_after_sale_log_order FOREIGN KEY(after_sale_id) REFERENCES trade_after_sale(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后操作日志';

CREATE TABLE IF NOT EXISTS trade_order_promotion_refund (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键编号',
    after_sale_id BIGINT NOT NULL COMMENT '本次售后主键编号，关联trade_after_sale.id',
    order_item_promotion_id BIGINT NOT NULL COMMENT '原商品明细优惠分摊主键编号',
    refund_discount_amount DECIMAL(19,2) NULL COMMENT '本次商品优惠回退归因，非额外退现金，单位元，历史未知为NULL',
    refund_delivery_discount_price DECIMAL(19,2) NULL COMMENT '本次运费优惠回退归因，非额外退现金，单位元，历史未知为NULL',
    return_use_point BIGINT NULL COMMENT '该来源本次应返使用积分，实际到账以账户流水为准',
    deduct_give_point BIGINT NULL COMMENT '该来源本次应回收赠送积分，实际扣回以账户流水为准',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_trade_promotion_refund(after_sale_id,order_item_promotion_id),
    KEY idx_trade_promotion_refund_source(order_item_promotion_id),
    CONSTRAINT fk_trade_promotion_refund_after_sale FOREIGN KEY(after_sale_id) REFERENCES trade_after_sale(id),
    CONSTRAINT fk_trade_promotion_refund_source FOREIGN KEY(order_item_promotion_id) REFERENCES trade_order_item_promotion(id),
    CONSTRAINT chk_trade_promotion_refund_amount CHECK(
   (refund_discount_amount IS NULL OR refund_discount_amount>=0) AND
   (refund_delivery_discount_price IS NULL OR refund_delivery_discount_price>=0)),
    CONSTRAINT chk_trade_promotion_refund_points CHECK(
   (return_use_point IS NULL OR return_use_point>=0) AND (deduct_give_point IS NULL OR deduct_give_point>=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易售后生成时冻结的原优惠回退归因与权益方案';
