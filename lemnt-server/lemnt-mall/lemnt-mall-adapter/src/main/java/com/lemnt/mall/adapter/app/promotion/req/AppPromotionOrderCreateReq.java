package com.lemnt.mall.adapter.app.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** App 活动下单请求，活动/记录编号按接口类型使用。 */
@Schema(description = "App 活动下单请求")
public record AppPromotionOrderCreateReq(
        @Schema(description = "活动编号，砍价订单不需要")
        Long activityId,
        @Schema(description = "砍价记录编号，其他活动不需要")
        Long recordId,
        @Schema(description = "拼团父团记录编号，0 或缺省表示新开团")
        Long headId,
        @Schema(description = "SKU 编号")
        @NotNull
        Long skuId,
        @Schema(description = "购买数量")
        @NotNull @Min(1) @Max(999)
        Integer quantity,
        @Schema(description = "收货地址编号，不传表示不使用配送快照")
        Long addressId,
        @Schema(description = "客户端请求编号，用于幂等重试")
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,64}")
        String requestNo
) implements java.io.Serializable {
}
