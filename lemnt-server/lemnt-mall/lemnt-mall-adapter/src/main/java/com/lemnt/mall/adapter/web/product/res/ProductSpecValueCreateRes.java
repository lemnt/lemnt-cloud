package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 规格字典新增响应。 */
@Schema(description = "规格字典新增响应")
public record ProductSpecValueCreateRes(
        @Schema(description = "规格值ID")
        Long id,
        @Schema(description = "所属规格项ID")
        Long specId,
        @Schema(description = "规格值名称")
        String name,
        @Schema(description = "说明")
        String remark
) implements Serializable {
}
