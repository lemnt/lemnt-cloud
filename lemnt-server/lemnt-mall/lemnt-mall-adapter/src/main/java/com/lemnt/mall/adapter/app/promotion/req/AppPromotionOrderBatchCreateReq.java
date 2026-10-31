package com.lemnt.mall.adapter.app.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/** App 多商品活动下单请求，目前用于积分商城。 */
@Schema(description = "App 多商品活动下单请求")
public record AppPromotionOrderBatchCreateReq(
        @Schema(description = "活动编号")
        @NotNull
        Long activityId,
        @Schema(description = "商品行")
        @NotEmpty @Valid
        List<Line> lines,
        @Schema(description = "收货地址编号，不传表示不使用配送快照")
        Long addressId,
        @Schema(description = "客户端请求编号，用于幂等重试")
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,64}")
        String requestNo
) implements java.io.Serializable {
    /** 商品行。 */
    public record Line(
            @Schema(description = "SKU 编号")
            @NotNull
            Long skuId,
            @Schema(description = "购买数量")
            @NotNull @Min(1) @Max(999)
            Integer quantity
    ) implements java.io.Serializable {
    }
}
