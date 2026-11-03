package com.lemnt.promotion.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 小程序装修页面响应。 */
@Schema(description = "小程序装修页面响应")
public record AppDiyPageRes(
        @Schema(description = "页面编号") Long id,
        @Schema(description = "页面名称") String name,
        @Schema(description = "页面属性 JSON") String property)
        implements Serializable {}
