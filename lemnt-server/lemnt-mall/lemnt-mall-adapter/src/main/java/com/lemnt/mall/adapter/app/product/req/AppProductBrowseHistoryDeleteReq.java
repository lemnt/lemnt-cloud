package com.lemnt.mall.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 浏览足迹移除请求。 */
@Schema(description = "浏览足迹移除请求")
public record AppProductBrowseHistoryDeleteReq(
        @Schema(description = "商品SPU编号，最多100项") @NotEmpty @Size(max = 100) java.util.List<@NotNull @Positive Long> spuIds
) {}
