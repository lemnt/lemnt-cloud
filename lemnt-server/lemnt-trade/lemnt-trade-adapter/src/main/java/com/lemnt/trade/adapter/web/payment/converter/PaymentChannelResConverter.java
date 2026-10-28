package com.lemnt.trade.adapter.web.payment.converter;

import com.lemnt.trade.adapter.web.payment.res.PaymentChannelRes;
import com.lemnt.trade.application.payment.dto.PaymentChannelDTO;

/** 支付渠道展示转换。 */
public final class PaymentChannelResConverter {
    private PaymentChannelResConverter() {
    }

    /** 转换渠道展示字段。 */
    public static PaymentChannelRes toRes(PaymentChannelDTO value) {
        return new PaymentChannelRes(value.code(), value.name());
    }
}
