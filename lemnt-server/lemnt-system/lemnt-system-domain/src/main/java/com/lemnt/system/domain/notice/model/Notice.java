package com.lemnt.system.domain.notice.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告聚合根 sys_notice
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class Notice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
     * 当前查询用户是否已读（顶部通知列表用）。
     */
    private Boolean isRead;

    /**
     * 校验公告是否可发布（标题不能为空）
     */
    public void validateForPublish() {
        if (noticeTitle == null || noticeTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
    }

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
