package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价活动新增请求。 */
@Schema(description = "砍价活动新增请求")
public record BargainActivityCreateReq(
        @Schema(description = "活动名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "开始时间")
        @NotNull
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        @NotNull
        LocalDateTime endTime,
        @Schema(description = "商品 SPU 编号")
        @NotNull
        Long spuId,
        @Schema(description = "商品 SKU 编号")
        @NotNull
        Long skuId,
        @Schema(description = "砍价起始价")
        @NotNull
        BigDecimal bargainFirstPrice,
        @Schema(description = "砍价底价")
        @NotNull
        BigDecimal bargainMinPrice,
        @Schema(description = "砍价库存")
        @NotNull
        Integer stock,
        @Schema(description = "砍价成功所需人数")
        @NotNull
        Integer helpMaxCount,
        @Schema(description = "每人可帮砍次数")
        @NotNull
        Integer bargainCount,
        @Schema(description = "总限购数量")
        @NotNull
        Integer totalLimitCount,
        @Schema(description = "单次随机最小砍价")
        @NotNull
        BigDecimal randomMinPrice,
        @Schema(description = "单次随机最大砍价")
        @NotNull
        BigDecimal randomMaxPrice
) implements java.io.Serializable {
}
