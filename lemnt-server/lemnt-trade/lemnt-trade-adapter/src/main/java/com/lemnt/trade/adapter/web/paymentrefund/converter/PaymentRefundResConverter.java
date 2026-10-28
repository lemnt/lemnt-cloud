package com.lemnt.trade.adapter.web.paymentrefund.converter;

import com.lemnt.trade.adapter.web.paymentrefund.res.PaymentRefundCreateRes;
import com.lemnt.trade.adapter.web.paymentrefund.res.PaymentRefundDetailRes;
import com.lemnt.trade.adapter.web.paymentrefund.res.PaymentRefundPageRes;
import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;

/** 支付管理应用结果与接口响应转换。 */
public final class PaymentRefundResConverter {
    private PaymentRefundResConverter() {
    }

    /** 转换分页记录响应。 */
    public static PaymentRefundPageRes page(PaymentRefundDTO value) {
        return new PaymentRefundPageRes(
                value.refundNo(), value.afterSaleNo(), value.paymentNo(), value.orderNo(),
                value.channel(), value.amount(), value.status(), value.channelRefundNo());
    }

    /** 转换详情响应。 */
    public static PaymentRefundDetailRes detail(PaymentRefundDTO value) {
        return new PaymentRefundDetailRes(
                value.refundNo(), value.afterSaleNo(), value.paymentNo(), value.orderNo(),
                value.channel(), value.amount(), value.status(), value.channelRefundNo());
    }

    /** 转换发起退款响应。 */
    public static PaymentRefundCreateRes create(PaymentRefundDTO value) {
        return new PaymentRefundCreateRes(
                value.refundNo(), value.afterSaleNo(), value.paymentNo(), value.orderNo(),
                value.channel(), value.amount(), value.status(), value.channelRefundNo());
    }
}
