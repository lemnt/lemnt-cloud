package com.lemnt.order.adapter.web.aftersale.converter;

import com.lemnt.order.adapter.web.aftersale.res.AfterSaleRefundRes;
import com.lemnt.order.application.paymentrefund.dto.PaymentRefundDTO;

/**
 * 售后发起支付退款的独立响应转换。
 */
public final class AfterSalePaymentResConverter {
    private AfterSalePaymentResConverter() {
    }

    /**
     * 转换实际支付退款交易结果。
     */
    public static AfterSaleRefundRes toRes(PaymentRefundDTO value) {
        return new AfterSaleRefundRes(
            value.refundNo(),
            value.afterSaleNo(),
            value.paymentNo(),
            value.orderNo(),
            value.channel(),
            value.amount(),
            value.status(),
            value.channelRefundNo());
    }
}
