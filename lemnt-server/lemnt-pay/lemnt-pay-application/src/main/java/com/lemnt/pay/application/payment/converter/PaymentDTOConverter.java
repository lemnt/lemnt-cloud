package com.lemnt.pay.application.payment.converter;

import com.lemnt.pay.application.payment.dto.PaymentDTO;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

/**
 * Payment 应用输出转换。
 */
public final class PaymentDTOConverter {
    private PaymentDTOConverter() {
    }

    /**
     * 将领域结果转换为应用输出。
     */
    public static PaymentDTO toDTO(PaymentRecord value) {
        return value == null
            ? null
            : new PaymentDTO(
            value.id(),
            value.paymentNo(),
            value.orderNo(),
            value.channel(),
            value.channelTradeNo(),
            value.amount(),
            value.status());
    }
}
