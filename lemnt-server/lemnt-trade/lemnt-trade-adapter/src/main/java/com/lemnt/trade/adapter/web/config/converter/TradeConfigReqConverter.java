package com.lemnt.trade.adapter.web.config.converter;

import com.lemnt.trade.adapter.web.config.req.TradeConfigUpdateReq;
import com.lemnt.trade.application.config.cmd.TradeConfigUpdateCmd;

/** config Req 接口边界转换。 */
public final class TradeConfigReqConverter {
    private TradeConfigReqConverter() {
    }

    /** 转换修改请求。 */
    public static TradeConfigUpdateCmd tradeConfigUpdate(TradeConfigUpdateReq req) {
        return new TradeConfigUpdateCmd(
                req.afterSaleRefundReasons(),
                req.afterSaleReturnReasons(),
                req.deliveryExpressFreeEnabled(),
                req.deliveryExpressFreePrice(),
                req.deliveryPickUpEnabled(),
                req.brokerageEnabled(),
                req.brokerageEnabledCondition(),
                req.brokerageBindMode(),
                req.brokeragePosterUrls(),
                req.brokerageFirstPercent(),
                req.brokerageSecondPercent(),
                req.brokerageWithdrawMinPrice(),
                req.brokerageWithdrawFeePercent(),
                req.brokerageFrozenDays(),
                req.brokerageWithdrawTypes());
    }
}
