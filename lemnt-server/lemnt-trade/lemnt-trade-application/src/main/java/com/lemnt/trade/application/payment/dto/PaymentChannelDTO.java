package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;

/** 支持的支付渠道。 */
public record PaymentChannelDTO(
        String code,
        String name
) implements Serializable {
}
