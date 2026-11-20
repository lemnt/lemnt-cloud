package com.lemnt.pay.adapter.app.recharge.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * App 按套餐创建充值单请求。
 */
@Schema(description = "创建充值单请求")
public record AppRechargeCreateReq(
        @Schema(description = "套餐编号，与自定义金额二选一") @Positive Long packageId,
        @Schema(description = "自定义充值金额，单位元") @DecimalMin("0.01") @Digits(integer = 9, fraction = 2)
                BigDecimal amount,
        @Schema(description = "幂等请求编号") @NotBlank @Size(max = 64) String requestNo)
        implements Serializable {}
