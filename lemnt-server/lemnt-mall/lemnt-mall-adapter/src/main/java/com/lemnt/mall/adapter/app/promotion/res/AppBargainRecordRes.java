package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** App 砍价记录。 */
@Schema(description = "App 砍价记录")
public record AppBargainRecordRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long userId,
        @Schema(description = "活动编号")
        Long activityId,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "砍价起始价")
        BigDecimal bargainFirstPrice,
        @Schema(description = "当前砍后价")
        BigDecimal bargainPrice,
        @Schema(description = "1 砍价中，2 成功，3 失败")
        Integer status,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "订单编号")
        Long orderId
) implements java.io.Serializable {
}
