package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 积分商城活动商品响应。 */
@Schema(description = "积分商城活动商品响应")
public record PointProductRes(
        @Schema(description = "商品明细编号")
        Long id,
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "SPU 编号")
        Long spuId,
        @Schema(description = "单会员可兑换次数")
        Integer exchangeCount,
        @Schema(description = "所需积分")
        Integer point,
        @Schema(description = "补差金额")
        BigDecimal price,
        @Schema(description = "库存")
        Integer stock
) implements java.io.Serializable {
}
