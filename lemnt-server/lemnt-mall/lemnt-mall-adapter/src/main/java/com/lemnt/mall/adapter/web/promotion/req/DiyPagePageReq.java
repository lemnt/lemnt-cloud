package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 装修页面分页请求。 */
@Schema(description = "装修页面分页请求")
public record DiyPagePageReq(
        @Schema(description = "模板编号") Long templateId,
        @Schema(description = "页面名称") String name,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
