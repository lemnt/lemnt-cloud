package com.lemnt.trade.adapter.web.payapp.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 新增支付应用请求。 */
@Schema(description = "新增支付应用请求")
public record PayAppCreateReq(
        @Schema(description = "应用标识，创建后不可修改")
        @NotBlank @Size(min = 4, max = 64) String appKey,
        @Schema(description = "应用名")
        @NotBlank @Size(max = 64) String name,
        @Schema(description = "状态：0启用、1停用")
        @NotNull @Min(0) @Max(1) Integer status,
        @Schema(description = "备注")
        @Size(max = 255) String remark,
        @Schema(description = "支付结果回调地址")
        @Size(max = 255) String orderNotifyUrl,
        @Schema(description = "退款结果回调地址")
        @Size(max = 255) String refundNotifyUrl
) implements Serializable {
}
