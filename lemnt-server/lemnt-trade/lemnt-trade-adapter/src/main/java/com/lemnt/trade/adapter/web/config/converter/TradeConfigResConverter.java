package com.lemnt.trade.adapter.web.config.converter;

import com.lemnt.trade.adapter.web.config.res.TradeConfigDetailRes;
import com.lemnt.trade.application.config.dto.TradeConfigDTO;

/** config Res 接口边界转换。 */
public final class TradeConfigResConverter {
    private TradeConfigResConverter() {
    }

    /** 转换详情响应。 */
    public static TradeConfigDetailRes tradeConfigDetail(TradeConfigDTO value) {
        if (value == null) {
            return null;
        }
        return new TradeConfigDetailRes(
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
