package com.lemnt.system.adapter.web.notice.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 修改通知公告请求
 *
 * @author wangxx
 */
@Schema(description = "修改通知公告请求")
public record NoticeUpdateReq(
        @NotNull(message = "公告ID不能为空")
        @Schema(description = "公告ID")
        Long noticeId,
        @NotBlank(message = "公告标题不能为空")
        @Schema(description = "公告标题")
        String noticeTitle,
        @NotBlank(message = "公告类型不能为空")
        @Schema(description = "公告类型（1通知 2公告）")
        String noticeType,
        @Schema(description = "公告内容")
        String noticeContent,
        @Schema(description = "公告状态（1正常 2关闭）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
