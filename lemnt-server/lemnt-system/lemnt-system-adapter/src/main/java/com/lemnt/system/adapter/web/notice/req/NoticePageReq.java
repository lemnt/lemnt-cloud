package com.lemnt.system.adapter.web.notice.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告分页查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "通知公告分页查询条件")
public record NoticePageReq(
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
 * 公告状态（1正常 2关闭）
 */
        @Schema(description = "公告状态（1正常 2关闭）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize,
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime
) implements Serializable {
    public NoticePageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
