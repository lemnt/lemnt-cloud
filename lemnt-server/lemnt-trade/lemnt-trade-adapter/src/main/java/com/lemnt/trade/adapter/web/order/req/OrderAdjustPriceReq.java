package com.lemnt.trade.adapter.web.order.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/** 后台订单调价请求。 */
@Schema(description = "后台订单调价请求")
public record OrderAdjustPriceReq(
        @Schema(description = "新的应付金额，人民币元", example = "99.00")
                @NotNull
                @DecimalMin(value = "0.01")
                @Digits(integer = 7, fraction = 2)
                BigDecimal totalAmount)
        implements Serializable {}
