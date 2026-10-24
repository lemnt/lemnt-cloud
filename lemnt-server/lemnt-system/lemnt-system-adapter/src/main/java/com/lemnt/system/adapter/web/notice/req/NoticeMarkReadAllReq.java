package com.lemnt.system.adapter.web.notice.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * 批量标记公告已读请求
 *
 * @author wangxx
 */
@Schema(description = "批量标记公告已读请求")
public record NoticeMarkReadAllReq(
        @NotEmpty(message = "公告ID列表不能为空")
        @Schema(description = "公告ID列表", example = "[1, 2, 3]")
        List<Long> ids
) implements Serializable {}
