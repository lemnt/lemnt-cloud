package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 规格字典详情响应。 */
@Schema(description = "规格字典详情响应")
public record ProductSpecDetailRes(
        @Schema(description = "规格项ID")
        Long id,
        @Schema(description = "规格名称")
        String name,
        @Schema(description = "说明")
        String remark
) implements Serializable {
}
