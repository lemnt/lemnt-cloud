package com.lemnt.system.adapter.web.notice.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 标记公告已读请求
 *
 * @author wangxx
 */
@Schema(description = "标记公告已读请求")
public record NoticeMarkReadReq(
        @NotNull(message = "公告ID不能为空")
        @Schema(description = "公告ID", example = "1")
        Long noticeId
) implements Serializable {}
