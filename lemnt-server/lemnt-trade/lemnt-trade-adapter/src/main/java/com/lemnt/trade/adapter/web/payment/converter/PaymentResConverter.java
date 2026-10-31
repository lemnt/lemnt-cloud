package com.lemnt.trade.adapter.web.payment.converter;

import com.lemnt.trade.adapter.web.payment.res.PaymentDetailRes;
import com.lemnt.trade.adapter.web.payment.res.PaymentPageRes;
import com.lemnt.trade.application.payment.dto.PaymentDTO;

/** 支付管理应用结果与接口响应转换。 */
public final class PaymentResConverter {
    private PaymentResConverter() {
    }

    /** 转换分页记录响应。 */
    public static PaymentPageRes page(PaymentDTO value) {
        return new PaymentPageRes(
                value.id(), value.paymentNo(), value.orderNo(), value.channel(),
                value.channelTradeNo(), value.amount(), value.status());
    }

    /** 转换详情响应。 */
    public static PaymentDetailRes detail(PaymentDTO value) {
        return new PaymentDetailRes(
                value.id(), value.paymentNo(), value.orderNo(), value.channel(),
                value.channelTradeNo(), value.amount(), value.status());
    }
}
