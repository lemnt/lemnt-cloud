-- 芋道配送与交易配置基础表。仅新增；先在演练库验证，不自动执行。
CREATE TABLE trade_delivery_express (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    code VARCHAR(40) NOT NULL COMMENT '快递公司编码',
    name VARCHAR(100) NOT NULL COMMENT '快递公司名称',
    logo VARCHAR(500) COMMENT '图标地址',
    sort INT NOT NULL COMMENT '排序',
    status INT NOT NULL COMMENT '状态，0 启用、1 停用',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    KEY idx_delivery_express_code(code),
    CONSTRAINT chk_delivery_express_status CHECK(status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_delivery_express_template (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    charge_mode INT NOT NULL COMMENT '计费方式，1 件数、2 重量千克、3 体积立方米',
    sort INT NOT NULL COMMENT '排序',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    KEY idx_delivery_template_name(name),
    CONSTRAINT chk_delivery_charge_mode CHECK(charge_mode IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_delivery_express_template_charge (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    template_id BIGINT NOT NULL COMMENT '模板编号',
    area_ids LONGTEXT NOT NULL COMMENT '配送区域编号集合',
    charge_mode INT NOT NULL COMMENT '模板计费方式快照',
    start_count DECIMAL(18,6) NOT NULL COMMENT '首量',
    start_price INT NOT NULL COMMENT '首价，分',
    extra_count DECIMAL(18,6) NOT NULL COMMENT '续量',
    extra_price INT NOT NULL COMMENT '续价，分',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    KEY idx_delivery_charge_template(template_id),
    CONSTRAINT fk_delivery_charge_template FOREIGN KEY(template_id) REFERENCES trade_delivery_express_template(id),
    CONSTRAINT chk_delivery_charge_values CHECK(start_count>0 AND extra_count>0 AND start_price>=0 AND extra_price>=0 AND charge_mode IN (1,2,3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_delivery_express_template_free (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    template_id BIGINT NOT NULL COMMENT '模板编号',
    area_ids LONGTEXT NOT NULL COMMENT '配送区域编号集合',
    free_price INT NOT NULL COMMENT '包邮金额门槛，分',
    free_count INT NOT NULL COMMENT '包邮计费量门槛',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    KEY idx_delivery_free_template(template_id),
    CONSTRAINT fk_delivery_free_template FOREIGN KEY(template_id) REFERENCES trade_delivery_express_template(id),
    CONSTRAINT chk_delivery_free_values CHECK(free_price>=0 AND free_count>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_config (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '单例配置编号',
    after_sale_refund_reasons LONGTEXT NOT NULL COMMENT '仅退款原因',
    after_sale_return_reasons LONGTEXT NOT NULL COMMENT '退货退款原因',
    delivery_express_free_enabled BOOLEAN NOT NULL COMMENT '是否全场包邮',
    delivery_express_free_price INT NOT NULL COMMENT '全场包邮金额门槛，分',
    delivery_pick_up_enabled BOOLEAN NOT NULL COMMENT '是否开启自提',
    brokerage_enabled BOOLEAN NOT NULL COMMENT '是否启用分佣',
    brokerage_enabled_condition INT NOT NULL COMMENT '分佣模式',
    brokerage_bind_mode INT NOT NULL COMMENT '分销绑定模式',
    brokerage_poster_urls LONGTEXT NOT NULL COMMENT '分销海报地址',
    brokerage_first_percent INT NOT NULL COMMENT '一级返佣百分比',
    brokerage_second_percent INT NOT NULL COMMENT '二级返佣百分比',
    brokerage_withdraw_min_price INT NOT NULL COMMENT '最低提现金额，分',
    brokerage_withdraw_fee_percent INT NOT NULL COMMENT '提现手续费百分比',
    brokerage_frozen_days INT NOT NULL COMMENT '佣金冻结天数',
    brokerage_withdraw_types LONGTEXT NOT NULL COMMENT '提现方式',
    create_by VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO trade_config (id, after_sale_refund_reasons, after_sale_return_reasons, delivery_express_free_enabled, delivery_express_free_price, delivery_pick_up_enabled, brokerage_enabled, brokerage_enabled_condition, brokerage_bind_mode, brokerage_poster_urls, brokerage_first_percent, brokerage_second_percent, brokerage_withdraw_min_price, brokerage_withdraw_fee_percent, brokerage_frozen_days, brokerage_withdraw_types)
VALUES (1, '[]', '[]', FALSE, 0, FALSE, FALSE, 1, 1, '[]', 0, 0, 0, 0, 0, '[]');
