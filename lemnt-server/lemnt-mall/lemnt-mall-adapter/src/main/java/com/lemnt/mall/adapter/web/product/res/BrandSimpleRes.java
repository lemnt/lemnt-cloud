package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 品牌选择记录。 */
@Schema(description = "品牌选择记录")
public record BrandSimpleRes(
        @Schema(description = "品牌ID")
        Long id,
        @Schema(description = "品牌名称")
        String name
) implements Serializable {
}
