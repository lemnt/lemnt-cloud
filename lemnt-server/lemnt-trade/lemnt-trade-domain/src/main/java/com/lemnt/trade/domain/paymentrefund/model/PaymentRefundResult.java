package com.lemnt.trade.domain.paymentrefund.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 渠道验签并核验商户后的退款结果。 */
public record PaymentRefundResult(
        String refundNo,
        String paymentNo,
        String channel,
        BigDecimal amount,
        String status,
        String channelRefundNo
) implements Serializable {
}
