package com.lemnt.mall.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 商品收藏状态查询请求。 */
@Schema(description = "商品收藏状态查询请求")
public record AppProductFavoriteExistsReq(
        @Schema(description = "商品SPU编号") @NotNull @Positive Long spuId
) {}
