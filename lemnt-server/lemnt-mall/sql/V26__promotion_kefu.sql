-- 客服会话与消息：一个会员一个会话，消息按会话和时间查询。
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
