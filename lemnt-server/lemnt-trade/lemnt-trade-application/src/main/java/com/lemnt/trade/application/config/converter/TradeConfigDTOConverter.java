package com.lemnt.trade.application.config.converter;

import com.lemnt.trade.application.config.dto.TradeConfigDTO;
import com.lemnt.trade.domain.config.model.TradeConfig;

/** config 应用输出转换。 */
public final class TradeConfigDTOConverter {
    private TradeConfigDTOConverter() {
    }

    /** 转换 交易配置。 */
    public static TradeConfigDTO toDTO(TradeConfig value) {
        if (value == null) {
            return null;
        }
        return new TradeConfigDTO(
                value.id(),
                value.afterSaleRefundReasons(),
                value.afterSaleReturnReasons(),
                value.deliveryExpressFreeEnabled(),
                value.deliveryExpressFreePrice(),
                value.deliveryPickUpEnabled(),
                value.brokerageEnabled(),
                value.brokerageEnabledCondition(),
                value.brokerageBindMode(),
                value.brokeragePosterUrls(),
                value.brokerageFirstPercent(),
                value.brokerageSecondPercent(),
                value.brokerageWithdrawMinPrice(),
                value.brokerageWithdrawFeePercent(),
                value.brokerageFrozenDays(),
                value.brokerageWithdrawTypes());
    }
}
