package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 装修页面响应。 */
@Schema(description = "装修页面响应")
public record DiyPageRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "模板编号") Long templateId,
        @Schema(description = "名称") String name,
        @Schema(description = "备注") String remark,
        @Schema(description = "预览图") List<String> previewPicUrls,
        @Schema(description = "JSON 属性") String property)
        implements java.io.Serializable {}
