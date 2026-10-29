package com.lemnt.mall.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 商品收藏添加请求。 */
@Schema(description = "商品收藏添加请求")
public record AppProductFavoriteCreateReq(
        @Schema(description = "商品SPU编号") @NotNull @Positive Long spuId
) {}
