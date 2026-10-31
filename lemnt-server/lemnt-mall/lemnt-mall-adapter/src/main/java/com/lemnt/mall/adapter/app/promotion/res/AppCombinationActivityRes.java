package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** App 拼团活动摘要。 */
@Schema(description = "App 拼团活动摘要")
public record AppCombinationActivityRes(
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
        @Schema(description = "商品市场价")
        BigDecimal marketPrice,
        @Schema(description = "最低拼团价")
        BigDecimal combinationPrice,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "成团人数")
        Integer userSize
) implements java.io.Serializable {
}
