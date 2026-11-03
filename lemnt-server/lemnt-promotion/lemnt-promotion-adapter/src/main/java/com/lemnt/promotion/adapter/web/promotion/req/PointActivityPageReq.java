package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 积分商城活动分页请求。 */
@Schema(description = "积分商城活动分页请求")
public record PointActivityPageReq(
        @Schema(description = "0 开启，1 关闭") Integer status,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
