package com.lemnt.mall.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** 浏览足迹添加请求。 */
@Schema(description = "浏览足迹添加请求")
public record AppProductBrowseHistoryCreateReq(
        @Schema(description = "商品SPU编号") @NotNull @Positive Long spuId
) {}
