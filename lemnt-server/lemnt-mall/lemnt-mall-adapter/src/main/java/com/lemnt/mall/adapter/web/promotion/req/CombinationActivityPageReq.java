package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 拼团活动分页请求。 */
@Schema(description = "拼团活动分页请求")
public record CombinationActivityPageReq(
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
