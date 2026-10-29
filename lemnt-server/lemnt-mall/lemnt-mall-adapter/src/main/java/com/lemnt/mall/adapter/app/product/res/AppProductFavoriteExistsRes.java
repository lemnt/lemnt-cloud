package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商品收藏状态查询响应。 */
@Schema(description = "商品收藏状态查询响应")
public record AppProductFavoriteExistsRes(
        @Schema(description = "本人是否已收藏") boolean favorite
) {}
