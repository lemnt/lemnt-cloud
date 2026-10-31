package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 砍价记录分页请求。 */
@Schema(description = "砍价记录分页请求")
public record BargainRecordPageReq(
        @Schema(description = "会员编号")
        Long userId,
        @Schema(description = "活动编号")
        Long activityId,
        @Schema(description = "1 砍价中，2 成功，3 失败")
        Integer status,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
