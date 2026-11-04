package com.lemnt.promotion.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 小程序装修模板响应，property/home/user 均为 JSON 文本。
 */
@Schema(description = "小程序装修模板响应")
public record AppDiyTemplateRes(
    @Schema(description = "模板编号") Long id,
    @Schema(description = "模板名称") String name,
    @Schema(description = "模板属性 JSON") String property,
    @Schema(description = "首页属性 JSON") String home,
    @Schema(description = "我的页面属性 JSON") String user)
    implements Serializable {
}
