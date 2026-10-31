package com.lemnt.trade.adapter.web.recharge.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/** 新增充值套餐请求。 */
@Schema(description = "新增充值套餐请求")
public record RechargePackageCreateReq(
        @Schema(description = "套餐名")
        @NotBlank @Size(max = 64) String name,
        @Schema(description = "支付金额，元")
        @NotNull @DecimalMin("0.01") @Digits(integer = 9, fraction = 2) BigDecimal payPrice,
        @Schema(description = "赠送金额，元")
        @NotNull @DecimalMin("0.00") @Digits(integer = 9, fraction = 2) BigDecimal bonusPrice,
        @Schema(description = "状态：0启用、1停用")
        @NotNull @Min(0) @Max(1) Integer status
) implements Serializable {
}
