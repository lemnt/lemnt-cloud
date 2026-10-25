package com.lemnt.trade.adapter.web.app.converter;

import com.lemnt.trade.application.checkout.dto.SettlementDTO;
import com.lemnt.trade.adapter.web.app.res.SettlementRes;
import com.lemnt.trade.adapter.web.checkout.converter.CheckoutResConverter;

public final class SettlementResConverter {
    private SettlementResConverter() {}

    public static SettlementRes convert(SettlementDTO value) {
        var q = value.quote();
        return new SettlementRes(
                value.settlementId(),
                value.expiresAt().toString(),
                q.items().stream().map(CheckoutResConverter::checkoutItem).toList(),
                q.originalAmount(),
                q.discountAmount(),
                q.payableAmount(),
                q.couponId());
    }
}
