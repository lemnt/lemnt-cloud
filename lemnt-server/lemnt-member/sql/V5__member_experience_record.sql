-- 经验独立表；biz_code/reason保留当前业务键与原因，不覆盖源数值类型和描述。
CREATE TABLE member_experience_record (
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
