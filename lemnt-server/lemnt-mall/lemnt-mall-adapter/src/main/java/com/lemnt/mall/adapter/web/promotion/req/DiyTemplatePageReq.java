package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 装修模板分页请求。 */
@Schema(description = "装修模板分页请求")
public record DiyTemplatePageReq(
        @Schema(description = "模板名称") String name,
        @Schema(description = "是否启用") Boolean used,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements Serializable {}
