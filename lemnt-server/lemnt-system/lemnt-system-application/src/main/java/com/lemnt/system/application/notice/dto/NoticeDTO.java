package com.lemnt.system.application.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告传输对象
 *
 * @author wangxx
 */
@Data
public class NoticeDTO {
    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（1正常 2关闭）
     */
    private String status;

    /**
     * 是否已读（首页顶部公告用）
     */
    private Boolean isRead;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
