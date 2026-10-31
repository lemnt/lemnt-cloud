package com.lemnt.trade.adapter.web.paychannel.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/** 修改支付渠道配置请求；配置留空表示保留原值。 */
@Schema(description = "修改支付渠道配置请求")
public record PayChannelUpdateReq(
        @Schema(description = "状态：0启用、1停用")
        @NotNull @Min(0) @Max(1) Integer status,
        @Schema(description = "渠道费率百分比，0-100")
        @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal feeRate,
        @Schema(description = "备注")
        @Size(max = 255) String remark,
        @Schema(description = "渠道配置 JSON 文本，留空保留原值")
        @Size(max = 4000) String config
) implements Serializable {
}
