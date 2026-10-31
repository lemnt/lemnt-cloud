package com.lemnt.trade.application.paymentrefund.converter;

import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;

/** 支付退款应用输出转换。 */
public final class PaymentRefundDTOConverter {
    private PaymentRefundDTOConverter() {
    }

    /** 将领域结果转换为应用输出。 */
    public static PaymentRefundDTO toDTO(PaymentRefund value) {
        return value == null ? null : new PaymentRefundDTO(
                value.refundNo(), value.afterSaleNo(), value.paymentNo(), value.orderNo(),
                value.channel(), value.amount(), value.status(), value.channelRefundNo());
    }
}
