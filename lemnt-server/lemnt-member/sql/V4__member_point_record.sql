-- 积分独立表；biz_code/reason保留当前业务键与原因，不覆盖源数值类型和描述。
CREATE TABLE member_point_record (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 biz_id VARCHAR(64) NOT NULL,
 biz_type INT NULL,
 title VARCHAR(255) NULL,
 description VARCHAR(500) NULL,
 point BIGINT NOT NULL,
 total_point BIGINT NOT NULL,
 biz_code VARCHAR(40) NOT NULL,
 reason VARCHAR(500) NOT NULL,
 create_time DATETIME NOT NULL,
 create_by VARCHAR(64) NULL,
 update_by VARCHAR(64) NULL,
 update_time DATETIME NULL,
 UNIQUE KEY uk_member_point_request(user_id,biz_code,biz_id),
 KEY idx_member_point_user(user_id,id),
 FOREIGN KEY(user_id) REFERENCES member_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
