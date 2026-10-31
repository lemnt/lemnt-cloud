package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** 秒杀时段分页请求。 */
@Schema(description = "秒杀时段分页请求")
public record SeckillConfigPageReq(
        @Schema(description = "时段名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "pageNum")
        Integer pageNum,
        @Schema(description = "pageSize")
        Integer pageSize
) implements java.io.Serializable {
}
