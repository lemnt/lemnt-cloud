package com.lemnt.trade.application.config.dto;

import java.util.List;

/** 交易配置传输对象。 */
public record TradeConfigDTO(
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
) implements java.io.Serializable {
}
