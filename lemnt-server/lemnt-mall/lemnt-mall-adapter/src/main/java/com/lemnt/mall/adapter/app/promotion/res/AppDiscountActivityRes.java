package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 小程序限时折扣详情。 */
@Schema(description = "小程序限时折扣详情")
public record AppDiscountActivityRes(
        @Schema(description = "活动编号") Long id,
        @Schema(description = "活动名称") String name,
        @Schema(description = "开始时间") LocalDateTime startTime,
        @Schema(description = "结束时间") LocalDateTime endTime,
        @Schema(description = "折扣商品") List<Product> products
) implements java.io.Serializable {
    public AppDiscountActivityRes {
        products = products == null ? List.of() : List.copyOf(products);
    }

    /** 折扣商品。 */
    @Schema(description = "折扣商品")
    public record Product(
            @Schema(description = "SPU 编号") Long spuId,
            @Schema(description = "SKU 编号") Long skuId,
            @Schema(description = "折扣类型 PRICE/PERCENT") String discountType,
            @Schema(description = "折扣百分比") Integer discountPercent,
            @Schema(description = "优惠金额，元") BigDecimal discountPrice
    ) implements java.io.Serializable {
    }
}
