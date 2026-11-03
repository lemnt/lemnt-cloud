package com.lemnt.mall.adapter.app.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** App 秒杀活动分页请求。 */
@Schema(description = "App 秒杀活动分页请求")
public record AppSeckillPageReq(
        @Schema(description = "秒杀时段编号") Long configId,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
