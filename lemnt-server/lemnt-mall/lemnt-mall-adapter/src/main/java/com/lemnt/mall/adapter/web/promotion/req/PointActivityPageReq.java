package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 积分商城活动分页请求。 */
@Schema(description = "积分商城活动分页请求")
public record PointActivityPageReq(
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
