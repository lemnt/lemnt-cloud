package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 门店详情响应。
 */
@Schema(description = "StoreDetail公开响应")
public record StoreDetailRes(
        @Schema(description = "id")
        Long id,
        @Schema(description = "name")
        String name,
        @Schema(description = "categoryId")
        Long categoryId,
        @Schema(description = "brandId")
        Long brandId,
        @Schema(description = "description")
        String description,
        @Schema(description = "image")
        String image,
        @Schema(description = "skus")
        List<StoreSkuRes> skus
) implements Serializable {}
