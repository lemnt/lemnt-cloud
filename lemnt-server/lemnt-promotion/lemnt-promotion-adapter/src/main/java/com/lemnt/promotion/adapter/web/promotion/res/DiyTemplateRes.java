package com.lemnt.promotion.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 装修模板响应。 */
@Schema(description = "装修模板响应")
public record DiyTemplateRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "名称") String name,
        @Schema(description = "是否启用") Boolean used,
        @Schema(description = "启用时间") LocalDateTime usedTime,
        @Schema(description = "备注") String remark,
        @Schema(description = "预览图") List<String> previewPicUrls,
        @Schema(description = "JSON 属性") String property)
        implements java.io.Serializable {}
