package com.lemnt.trade.adapter.app.recharge.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** App 按套餐创建充值单请求。 */
@Schema(description = "创建充值单请求")
public record AppRechargeCreateReq(
        @Schema(description = "套餐编号")
        @NotNull @Positive Long packageId,
        @Schema(description = "幂等请求编号")
        @NotBlank @Size(max = 64) String requestNo
) implements Serializable {
}
