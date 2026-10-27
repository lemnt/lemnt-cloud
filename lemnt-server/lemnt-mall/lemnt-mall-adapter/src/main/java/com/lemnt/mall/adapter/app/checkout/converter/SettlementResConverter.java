package com.lemnt.mall.adapter.app.checkout.converter;

import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.checkout.res.SettlementAbandonRes;
import com.lemnt.mall.adapter.app.checkout.res.SettlementRes;
import com.lemnt.mall.application.checkout.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.checkout.dto.SettlementDTO;

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
                q.items().stream().map(CheckoutResConverter::checkoutItem).toList(),
                q.originalAmount(),
                q.discountAmount(),
                q.payableAmount(),
                q.couponId());
    }

    /**
     * 放弃。
     */
    public static SettlementAbandonRes abandon(
            SettlementAbandonDTO value) {
        return new SettlementAbandonRes(
                value.abandoned(), CheckoutResConverter.checkoutReceipt(value.order()));
    }
}
