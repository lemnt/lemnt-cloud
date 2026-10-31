package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商品收藏分页响应。 */
@Schema(description = "商品收藏分页响应")
public record ProductFavoritePageRes(
        @Schema(description = "记录编号") Long id,
        @Schema(description = "商品SPU编号") Long spuId,
        @Schema(description = "当前商品名称，失效时为空") String spuName,
        @Schema(description = "当前图片，缺失时为空") String picUrl,
        @Schema(description = "当前最低价格，元，缺失时为空") java.math.BigDecimal price,
        @Schema(description = "会员编号") Long userId,
        @Schema(description = "收藏时间") java.time.LocalDateTime createTime
) {}
