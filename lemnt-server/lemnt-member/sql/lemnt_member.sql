-- lemnt_member：最终表结构初始化。仅用于建库，不提供存量升级或历史回填。
-- 本服务没有必须预置的业务数据；菜单、字典与默认参数归system，本次保持原样。

CREATE TABLE IF NOT EXISTS member_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(80) NOT NULL UNIQUE COMMENT '名称',
    level INT NOT NULL UNIQUE COMMENT '会员等级序号',
    experience BIGINT NOT NULL UNIQUE COMMENT '升级所需经验门槛',
    discount_percent INT NOT NULL COMMENT '等级折扣百分比，100表示不打折',
    icon VARCHAR(500) COMMENT '图标地址',
    background_url VARCHAR(500) COMMENT '等级背景图片地址',
    status INT NOT NULL COMMENT '等级状态：1启用、0停用',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_time DATETIME NULL COMMENT '修改时间',
    update_by VARCHAR(64) NULL COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员等级';

CREATE TABLE IF NOT EXISTS member_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(80) NOT NULL UNIQUE COMMENT '名称',
    remark VARCHAR(500) COMMENT '备注说明',
    status INT NOT NULL COMMENT '分组状态：1启用、0停用',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_time DATETIME NULL COMMENT '修改时间',
    update_by VARCHAR(64) NULL COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员分组';

CREATE TABLE IF NOT EXISTS member_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    name VARCHAR(80) NOT NULL UNIQUE COMMENT '名称',
    remark VARCHAR(500) COMMENT '备注说明',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_time DATETIME NULL COMMENT '修改时间',
    update_by VARCHAR(64) NULL COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员标签';

CREATE TABLE IF NOT EXISTS member_sign_in_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    `day` INT NOT NULL COMMENT '连续签到第几天',
    point BIGINT NOT NULL COMMENT '本档签到奖励积分',
    experience BIGINT NOT NULL COMMENT '本档签到奖励经验',
    status INT NOT NULL COMMENT '规则状态：1启用、0停用',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_time DATETIME NULL COMMENT '修改时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    UNIQUE KEY uk_member_sign_config_day(`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员连续签到奖励规则';

CREATE TABLE IF NOT EXISTS member_login_attempt (
    login_hash VARCHAR(64) PRIMARY KEY COMMENT '登录账号摘要',
    failures INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    window_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录失败计数窗口开始时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员登录失败计数';

CREATE TABLE IF NOT EXISTS member_sms_challenge (
    mobile VARCHAR(24) NOT NULL COMMENT '手机号码',
    scene VARCHAR(40) NOT NULL COMMENT '验证码业务场景',
    code_hash VARCHAR(64) NULL COMMENT '短信验证码摘要',
    expires_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '到期时间',
    attempts INT NOT NULL DEFAULT 0 COMMENT '已尝试校验次数',
    last_sent_at DATETIME NULL COMMENT '最近短信发送时间',
    sent_date DATE NULL COMMENT '短信发送计数日期',
    daily_count INT NOT NULL DEFAULT 0 COMMENT '当日短信发送次数',
    PRIMARY KEY(mobile,scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员短信验证码校验状态';

CREATE TABLE IF NOT EXISTS member_sms_budget (
    budget_day DATE PRIMARY KEY COMMENT '短信总预算日期',
    requests INT NOT NULL DEFAULT 0 COMMENT '当日短信请求总数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员短信日请求预算';

CREATE TABLE IF NOT EXISTS member_social_state (
    state_hash VARCHAR(64) PRIMARY KEY COMMENT '第三方授权状态摘要',
    provider VARCHAR(100) NOT NULL COMMENT '第三方身份提供方',
    purpose VARCHAR(16) NOT NULL COMMENT '第三方授权用途',
    member_id BIGINT NULL COMMENT '会员编号',
    expires_at DATETIME NOT NULL COMMENT '到期时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员第三方授权状态';

CREATE TABLE IF NOT EXISTS member_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员编号',
    nickname VARCHAR(80) NOT NULL COMMENT '会员昵称',
    mark VARCHAR(255) NULL COMMENT '后台会员备注',
    name VARCHAR(30) NULL COMMENT '名称',
    area_id INT NULL COMMENT '行政区划编号',
    login_date DATETIME NULL COMMENT '最近登录时间',
    login_ip VARCHAR(50) NULL COMMENT '最近登录IP',
    mobile VARCHAR(24) NULL COMMENT '手机号码',
    password VARCHAR(100) NULL COMMENT '密码哈希',
    email VARCHAR(254) NULL COMMENT '电子邮箱',
    avatar VARCHAR(255) NULL COMMENT '头像地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '会员状态：1启用、0停用',
    points BIGINT NOT NULL DEFAULT 0 COMMENT '当前积分余额',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY(id),
    UNIQUE KEY uk_member_mobile(mobile),
    UNIQUE KEY uk_member_email(email),
    KEY idx_member_status(status),
    experience BIGINT NOT NULL DEFAULT 0 COMMENT '当前累计经验余额',
    level_id BIGINT NULL COMMENT '会员等级编号',
    group_id BIGINT NULL COMMENT '会员分组编号',
    sex INT NULL COMMENT '性别：0未知、1男、2女',
    birthday DATE NULL COMMENT '出生日期',
    CONSTRAINT fk_member_level FOREIGN KEY(level_id) REFERENCES member_level(id),
    CONSTRAINT fk_member_group FOREIGN KEY(group_id) REFERENCES member_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员基础信息';

CREATE TABLE IF NOT EXISTS member_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键编号',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    access_hash VARCHAR(64) NOT NULL UNIQUE COMMENT '访问令牌摘要',
    refresh_hash VARCHAR(64) NOT NULL UNIQUE COMMENT '刷新令牌摘要',
    expires_at DATETIME NOT NULL COMMENT '到期时间',
    refresh_expires_at DATETIME NOT NULL COMMENT '刷新令牌到期时间',
    FOREIGN KEY(member_id) REFERENCES member_user(id),
    KEY idx_member_session_owner(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员访问与刷新会话';

CREATE TABLE IF NOT EXISTS member_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    name VARCHAR(80) NOT NULL COMMENT '名称',
    mobile VARCHAR(24) NOT NULL COMMENT '手机号码',
    area_id BIGINT NOT NULL COMMENT '行政区划编号',
    detail_address VARCHAR(500) NOT NULL COMMENT '详细地址',
    default_status BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否默认收货地址',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_time DATETIME NULL COMMENT '修改时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    FOREIGN KEY(user_id) REFERENCES member_user(id),
    KEY idx_member_address_owner(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员收货地址';

CREATE TABLE IF NOT EXISTS member_tag_link (
    member_id BIGINT NOT NULL COMMENT '会员编号',
    tag_id BIGINT NOT NULL COMMENT '会员标签编号',
    PRIMARY KEY(member_id,tag_id),
    FOREIGN KEY(member_id) REFERENCES member_user(id),
    FOREIGN KEY(tag_id) REFERENCES member_tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员标签关联';

CREATE TABLE IF NOT EXISTS member_level_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    level_id BIGINT NULL COMMENT '会员等级编号',
    user_experience BIGINT NOT NULL COMMENT '变更后会员经验余额',
    experience BIGINT NULL COMMENT '源记录经验：自动变更取等级门槛，手工调整取经验差值；历史未知为空',
    level INT NULL COMMENT '变更时等级序号快照',
    discount_percent INT NULL COMMENT '变更时等级折扣百分比快照',
    remark VARCHAR(500) NOT NULL COMMENT '备注说明',
    description VARCHAR(500) NULL COMMENT '描述说明',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    FOREIGN KEY(user_id) REFERENCES member_user(id),
    KEY idx_member_level_record(user_id,id)
) DEFAULT CHARSET=utf8mb4 ENGINE=InnoDB COMMENT '会员等级变更记录';

CREATE TABLE IF NOT EXISTS member_sign_in_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    sign_date DATE NOT NULL COMMENT '签到业务日期',
    `day` INT NOT NULL COMMENT '连续签到第几天',
    point BIGINT NOT NULL COMMENT '本次签到实际奖励积分',
    experience BIGINT NOT NULL COMMENT '本次签到实际奖励经验',
    create_time DATETIME NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    UNIQUE KEY uk_member_sign_date(user_id,sign_date),
    FOREIGN KEY(user_id) REFERENCES member_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员每日签到记录';

CREATE TABLE IF NOT EXISTS member_social_binding (
    provider VARCHAR(100) NOT NULL COMMENT '第三方身份提供方',
    subject VARCHAR(200) NOT NULL COMMENT '第三方用户唯一标识',
    member_id BIGINT NOT NULL COMMENT '会员编号',
    PRIMARY KEY(provider,subject),
    UNIQUE KEY uk_member_provider(member_id,provider),
    FOREIGN KEY(member_id) REFERENCES member_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员第三方身份绑定';

CREATE TABLE IF NOT EXISTS member_point_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务关联编号',
    biz_type INT NULL COMMENT '积分业务数值类型，历史未知为空',
    title VARCHAR(255) NULL COMMENT '标题',
    description VARCHAR(500) NULL COMMENT '描述说明',
    point BIGINT NOT NULL COMMENT '本次积分变更量，收入为正、支出为负',
    total_point BIGINT NOT NULL COMMENT '变更后积分余额',
    biz_code VARCHAR(40) NOT NULL COMMENT '幂等业务代码',
    reason VARCHAR(500) NOT NULL COMMENT '业务原因说明',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    UNIQUE KEY uk_member_point_request(user_id,biz_code,biz_id),
    KEY idx_member_point_user(user_id,id),
    FOREIGN KEY(user_id) REFERENCES member_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员积分变更流水';

CREATE TABLE IF NOT EXISTS member_experience_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键编号',
    user_id BIGINT NOT NULL COMMENT '会员编号',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务关联编号',
    biz_type INT NULL COMMENT '经验业务数值类型，历史未知为空',
    title VARCHAR(255) NULL COMMENT '标题',
    description VARCHAR(500) NULL COMMENT '描述说明',
    experience BIGINT NOT NULL COMMENT '本次经验变更量，增加为正、扣减为负',
    total_experience BIGINT NOT NULL COMMENT '变更后经验余额',
    biz_code VARCHAR(40) NOT NULL COMMENT '幂等业务代码',
    reason VARCHAR(500) NOT NULL COMMENT '业务原因说明',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    UNIQUE KEY uk_member_experience_request(user_id,biz_code,biz_id),
    KEY idx_member_experience_user(user_id,id),
    FOREIGN KEY(user_id) REFERENCES member_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '会员经验变更流水';


CREATE TABLE IF NOT EXISTS member_message_inbox (
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
