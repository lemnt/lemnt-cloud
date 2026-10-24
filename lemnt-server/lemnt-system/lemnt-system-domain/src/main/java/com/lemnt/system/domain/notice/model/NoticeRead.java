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
public class NoticeRead implements Serializable {
    @Serial
    private static final long serialVersionUID = 4444418413107258088L;

    /**
     * 主键
     */
    private Long readId;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}