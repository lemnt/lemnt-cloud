package com.lemnt.trade.adapter.web.aftersale.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 商品售后申请请求。 */
@Schema(description = "商品售后申请请求")
public record AfterSaleCreateReq(
        @Schema(description = "幂等请求编号")
        @NotBlank
        @Size(max = 64)
        String requestNo,
        @Schema(description = "原订单商品行ID")
        @NotNull
        @Positive
        Long orderItemId,
        @Schema(description = "售后数量")
        @NotNull
        @Min(1)
        @Max(999)
        Integer quantity,
        @Schema(description = "REFUND_ONLY或RETURN_REFUND")
        @NotBlank @Pattern(regexp = "REFUND_ONLY|RETURN_REFUND")
        String type,
        @Schema(description = "申请原因")
        @NotBlank
        @Size(max = 500)
        String reason
) implements Serializable {
}
