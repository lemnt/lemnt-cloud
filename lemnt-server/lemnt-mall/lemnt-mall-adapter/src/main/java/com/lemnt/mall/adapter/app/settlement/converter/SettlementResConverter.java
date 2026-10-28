package com.lemnt.mall.adapter.app.settlement.converter;

import com.lemnt.mall.adapter.app.settlement.res.SettlementAbandonRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementRes;
import com.lemnt.mall.application.settlement.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.settlement.dto.SettlementDTO;

/**
 * 结算响应转换器。
 */
public final class SettlementResConverter {
    private SettlementResConverter() {}

    /**
     * 转换。
     */
    public static SettlementRes convert(SettlementDTO value) {
        var q = value.quote();
        return new SettlementRes(
                value.settlementId(),
                value.expiresAt().toString(),
                q.items().stream().map(SettlementQuoteResConverter::checkoutItem).toList(),
                q.originalAmount(),
                q.discountAmount(),
                q.payableAmount(),
                q.couponId(), q.usePoint(), q.givePoint(), SettlementQuoteResConverter.delivery(q.delivery()));
    }

    /**
     * 放弃。
     */
    public static SettlementAbandonRes abandon(
            SettlementAbandonDTO value
    ) {
        return new SettlementAbandonRes(
                value.abandoned(), SettlementQuoteResConverter.checkoutReceipt(value.order()));
    }
}
