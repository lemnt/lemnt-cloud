package com.lemnt.trade.adapter.web.config.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 交易配置详情响应。 */
@Schema(description = "交易配置详情响应")
public record TradeConfigDetailRes(
        @Schema(description = "单例配置编号")
        Long id,
        @Schema(description = "仅退款原因")
        List<String> afterSaleRefundReasons,
        @Schema(description = "退货退款原因")
        List<String> afterSaleReturnReasons,
        @Schema(description = "是否全场包邮")
        Boolean deliveryExpressFreeEnabled,
        @Schema(description = "全场包邮金额门槛，分")
        Integer deliveryExpressFreePrice,
        @Schema(description = "是否开启自提")
        Boolean deliveryPickUpEnabled,
        @Schema(description = "是否启用分佣")
        Boolean brokerageEnabled,
        @Schema(description = "分佣模式")
        Integer brokerageEnabledCondition,
        @Schema(description = "分销绑定模式")
        Integer brokerageBindMode,
        @Schema(description = "分销海报地址")
        List<String> brokeragePosterUrls,
        @Schema(description = "一级返佣百分比")
        Integer brokerageFirstPercent,
        @Schema(description = "二级返佣百分比")
        Integer brokerageSecondPercent,
        @Schema(description = "最低提现金额，分")
        Integer brokerageWithdrawMinPrice,
        @Schema(description = "提现手续费百分比")
        Integer brokerageWithdrawFeePercent,
        @Schema(description = "佣金冻结天数")
        Integer brokerageFrozenDays,
        @Schema(description = "提现方式")
        List<Integer> brokerageWithdrawTypes
) implements Serializable {
}
