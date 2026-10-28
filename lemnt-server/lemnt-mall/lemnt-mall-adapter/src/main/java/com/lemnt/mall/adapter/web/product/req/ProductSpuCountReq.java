package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 商品状态统计请求。 */
@Schema(description = "商品状态统计请求")
public record ProductSpuCountReq(
        @Schema(description = "商品名称关键词")
        @Size(max = 200)
        String name,
        @Schema(description = "精确分类ID")
        @Positive
        Long categoryId,
        @Schema(description = "精确品牌ID")
        @Positive
        Long brandId
) implements Serializable {
}
