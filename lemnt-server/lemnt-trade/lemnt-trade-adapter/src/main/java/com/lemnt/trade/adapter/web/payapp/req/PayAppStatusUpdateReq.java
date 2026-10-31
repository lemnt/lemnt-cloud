package com.lemnt.trade.adapter.web.payapp.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** 启用或停用支付应用请求。 */
@Schema(description = "支付应用状态请求")
public record PayAppStatusUpdateReq(
        @Schema(description = "状态：0启用、1停用")
        @NotNull @Min(0) @Max(1) Integer status
) implements Serializable {
}
