package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/** 商品收藏分页请求。 */
@Schema(description = "商品收藏分页请求")
public record ProductFavoritePageReq(
        @Schema(description = "会员编号，可空") @Positive Long userId,
        @Schema(description = "商品编号，可空") @Positive Long spuId,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页，默认20最多100") @Min(1) @Max(100) Integer pageSize
) {}
