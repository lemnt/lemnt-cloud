package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 规格字典选择记录。 */
@Schema(description = "规格字典选择记录")
public record ProductSpecValueSimpleRes(
        @Schema(description = "规格值ID")
        Long id,
        @Schema(description = "所属规格项ID")
        Long specId,
        @Schema(description = "规格值名称")
        String name
) implements Serializable {
}
