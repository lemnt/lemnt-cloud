package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商品收藏数量查询响应。 */
@Schema(description = "商品收藏数量查询响应")
public record AppProductFavoriteCountRes(
        @Schema(description = "本人收藏数量") long count
) {}
