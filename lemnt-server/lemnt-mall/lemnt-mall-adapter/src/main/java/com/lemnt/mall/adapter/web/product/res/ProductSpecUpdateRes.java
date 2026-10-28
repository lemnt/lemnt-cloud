package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 规格字典修改响应。 */
@Schema(description = "规格字典修改响应")
public record ProductSpecUpdateRes(
        @Schema(description = "规格项ID")
        Long id,
        @Schema(description = "规格名称")
        String name,
        @Schema(description = "说明")
        String remark
) implements Serializable {
}
