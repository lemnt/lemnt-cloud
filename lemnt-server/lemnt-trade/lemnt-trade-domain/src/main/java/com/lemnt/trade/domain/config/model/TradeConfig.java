package com.lemnt.trade.domain.config.model;

import java.util.List;

/** trade_config 业务对象，金额以分为单位。 */
public record TradeConfig(
        Long id,
        List<String> afterSaleRefundReasons,
        List<String> afterSaleReturnReasons,
        Boolean deliveryExpressFreeEnabled,
        Integer deliveryExpressFreePrice,
        Boolean deliveryPickUpEnabled,
        Boolean brokerageEnabled,
        Integer brokerageEnabledCondition,
        Integer brokerageBindMode,
        List<String> brokeragePosterUrls,
        Integer brokerageFirstPercent,
        Integer brokerageSecondPercent,
        Integer brokerageWithdrawMinPrice,
        Integer brokerageWithdrawFeePercent,
        Integer brokerageFrozenDays,
        List<Integer> brokerageWithdrawTypes
) {
}
