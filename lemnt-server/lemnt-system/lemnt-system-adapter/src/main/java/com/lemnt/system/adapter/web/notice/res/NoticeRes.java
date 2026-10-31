package com.lemnt.system.adapter.web.notice.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告信息响应
 *
 * @author wangxx
 */
@Schema(description = "通知公告信息响应")
public record NoticeRes(
        /**
 * 公告ID
 */
        @Schema(description = "公告ID")
        Long noticeId,
        /**
 * 公告标题
 */
        @Schema(description = "公告标题")
        String noticeTitle,
        /**
 * 公告类型（1通知 2公告）
 */
        @Schema(description = "公告类型（1通知 2公告）")
        String noticeType,
        /**
 * 公告内容
 */
        @Schema(description = "公告内容")
        String noticeContent,
        /**
 * 公告状态（1正常 2关闭）
 */
        @Schema(description = "公告状态（1正常 2关闭）")
        String status,
        /**
 * 是否已读（首页顶部公告用）
 */
        @Schema(description = "是否已读")
        Boolean isRead,
        /**
 * 创建者
 */
        @Schema(description = "创建者")
        String createBy,
        /**
 * 创建时间
 */
        @Schema(description = "创建时间")
        LocalDateTime createTime,
        /**
 * 更新者
 */
        @Schema(description = "更新者")
        String updateBy,
        /**
 * 更新时间
 */
        @Schema(description = "更新时间")
        LocalDateTime updateTime,
        /**
 * 备注
 */
        @Schema(description = "备注")
        String remark
) implements Serializable {}
