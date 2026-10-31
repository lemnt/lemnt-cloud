package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** App 砍价活动。 */
@Schema(description = "App 砍价活动")
public record AppBargainActivityRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "SKU 原价")
        BigDecimal skuPrice,
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "砍价起始价")
        BigDecimal bargainFirstPrice,
        @Schema(description = "砍价底价")
        BigDecimal bargainMinPrice,
        @Schema(description = "剩余库存")
        Integer stock,
        @Schema(description = "总库存")
        Integer totalStock,
        @Schema(description = "砍价成功所需人数")
        Integer helpMaxCount,
        @Schema(description = "每人可帮砍次数")
        Integer bargainCount,
        @Schema(description = "总限购数量")
        Integer totalLimitCount,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "成功砍价人数")
        Long successCount
) implements java.io.Serializable {
}
