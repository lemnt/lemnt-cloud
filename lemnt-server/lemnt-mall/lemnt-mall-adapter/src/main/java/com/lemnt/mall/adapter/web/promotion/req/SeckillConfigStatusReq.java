package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/** 秒杀时段状态修改请求。 */
@Schema(description = "秒杀时段状态修改请求")
public record SeckillConfigStatusReq(
        @Schema(description = "0 开启，1 关闭")
        @NotNull
        Integer status
) implements java.io.Serializable {
}
