package com.lemnt.trade.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 交易配置修改请求。 */
@Schema(description = "交易配置修改请求")
public record TradeConfigUpdateReq(
        @Schema(description = "仅退款原因")
        @NotNull @Size(max = 100)
        List<String> afterSaleRefundReasons,
        @Schema(description = "退货退款原因")
        @NotNull @Size(max = 100)
        List<String> afterSaleReturnReasons,
        @Schema(description = "是否全场包邮")
        @NotNull
        Boolean deliveryExpressFreeEnabled,
        @Schema(description = "全场包邮金额门槛，分")
        @NotNull
        @DecimalMin("0")
        Integer deliveryExpressFreePrice,
        @Schema(description = "是否开启自提")
        @NotNull
        Boolean deliveryPickUpEnabled,
        @Schema(description = "是否启用分佣")
        @NotNull
        Boolean brokerageEnabled,
        @Schema(description = "分佣模式")
        @NotNull
        @DecimalMin("0")
        Integer brokerageEnabledCondition,
        @Schema(description = "分销绑定模式")
        @NotNull
        @DecimalMin("0")
        Integer brokerageBindMode,
        @Schema(description = "分销海报地址")
        @NotNull @Size(max = 100)
        List<String> brokeragePosterUrls,
        @Schema(description = "一级返佣百分比")
        @NotNull
        @DecimalMin("0")
        Integer brokerageFirstPercent,
        @Schema(description = "二级返佣百分比")
        @NotNull
        @DecimalMin("0")
        Integer brokerageSecondPercent,
        @Schema(description = "最低提现金额，分")
        @NotNull
        @DecimalMin("0")
        Integer brokerageWithdrawMinPrice,
        @Schema(description = "提现手续费百分比")
        @NotNull
        @DecimalMin("0")
        Integer brokerageWithdrawFeePercent,
        @Schema(description = "佣金冻结天数")
        @NotNull
        @DecimalMin("0")
        Integer brokerageFrozenDays,
        @Schema(description = "提现方式")
        @NotNull @Size(max = 100)
        List<Integer> brokerageWithdrawTypes
) implements Serializable {
}
