package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 拼团记录分页请求。 */
@Schema(description = "拼团记录分页请求")
public record CombinationRecordPageReq(
        @Schema(description = "会员编号")
        Long userId,
        @Schema(description = "拼团活动编号")
        Long activityId,
        @Schema(description = "0 进行中，1 成功，2 失败")
        Integer status,
        @Schema(description = "团长记录编号，0 表示只看团长")
        Long headId,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
