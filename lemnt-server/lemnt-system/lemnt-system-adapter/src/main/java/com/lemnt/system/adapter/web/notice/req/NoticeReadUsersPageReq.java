package com.lemnt.system.adapter.web.notice.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已读用户列表查询请求
 *
 * @author wangxx
 */
@Schema(description = "已读用户列表查询请求")
public record NoticeReadUsersPageReq(
        @NotNull(message = "公告ID不能为空")
        @Schema(description = "公告ID", example = "1")
        Long noticeId,
        @Schema(description = "搜索关键词")
        String searchValue,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {
    public NoticeReadUsersPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
