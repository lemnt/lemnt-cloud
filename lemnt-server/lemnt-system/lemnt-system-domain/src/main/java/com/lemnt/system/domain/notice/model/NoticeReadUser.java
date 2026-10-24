package com.lemnt.system.domain.notice.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读记录聚合根 sys_notice_read
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class NoticeReadUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 4444418413107258088L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 手机号
     */
    private String phonenumber;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}