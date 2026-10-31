package com.lemnt.trade.adapter.web.payapp.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 支付应用响应。 */
@Schema(description = "支付应用响应")
public record PayAppRes(
        @Schema(description = "应用编号")
        Long id,
        @Schema(description = "应用标识")
        String appKey,
        @Schema(description = "应用名")
        String name,
        @Schema(description = "状态：0启用、1停用")
        Integer status,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "支付结果回调地址")
        String orderNotifyUrl,
        @Schema(description = "退款结果回调地址")
        String refundNotifyUrl,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}
