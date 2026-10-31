package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 门店商品响应。
 */
@Schema(description = "StoreProduct公开响应")
public record StoreProductRes(
        @Schema(description = "id")
        Long id,
        @Schema(description = "name")
        String name,
        @Schema(description = "categoryId")
        Long categoryId,
        @Schema(description = "brandId")
        Long brandId,
        @Schema(description = "image")
        String image,
        @Schema(description = "最低售价（元，精确两位小数字符串）")
        String minPrice,
        @Schema(description = "最高售价（元，精确两位小数字符串）")
        String maxPrice,
        @Schema(description = "是否存在有库存的在售规格，结算时再次核验")
        boolean available
) implements Serializable {}
