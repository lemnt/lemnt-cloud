package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 砍价助力分页请求。 */
@Schema(description = "砍价助力分页请求")
public record BargainHelpPageReq(
        @Schema(description = "砍价记录编号")
        Long recordId,
        @Schema(description = "会员编号")
        Long userId,
        @Schema(description = "活动编号")
        Long activityId,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
