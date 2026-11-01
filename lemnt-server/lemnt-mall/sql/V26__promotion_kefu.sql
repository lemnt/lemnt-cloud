-- 客服会话与消息：一个会员一个会话，消息按会话和时间查询。
CREATE TABLE IF NOT EXISTS mall_promotion_kefu_conversation (
 id BIGINT NOT NULL PRIMARY KEY,
 user_id BIGINT NOT NULL,
 last_message_time DATETIME(3) NOT NULL,
 last_message_content VARCHAR(500) NULL,
 last_message_content_type INT NULL,
 admin_pinned TINYINT(1) NOT NULL DEFAULT 0,
 admin_deleted TINYINT(1) NOT NULL DEFAULT 0,
 admin_unread_message_count INT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_kefu_conversation_user(user_id),
 KEY idx_kefu_conversation_list(admin_deleted,admin_pinned,last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_promotion_kefu_message (
 id BIGINT NOT NULL PRIMARY KEY,
 conversation_id BIGINT NOT NULL,
 sender_id BIGINT NOT NULL,
 sender_type INT NOT NULL,
 receiver_id BIGINT NOT NULL,
 receiver_type INT NOT NULL,
 content_type INT NOT NULL,
 content VARCHAR(2000) NOT NULL,
 read_status TINYINT(1) NOT NULL DEFAULT 0,
 create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_kefu_message_conversation(conversation_id,create_time),
 KEY idx_kefu_message_unread(conversation_id,sender_type,read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
