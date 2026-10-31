package com.lemnt.trade.infrastructure.paymentrefund.converter;

import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.infrastructure.paymentrefund.dataobject.PaymentRefundDO;

/** 支付退款领域对象与数据对象转换。 */
public final class PaymentRefundConverter {
    private PaymentRefundConverter() {
    }

    /** 转换为领域交易。 */
    public static PaymentRefund toModel(PaymentRefundDO value) {
        return value == null ? null : new PaymentRefund(value.getRefundNo(), value.getAfterSaleNo(),
                value.getPaymentNo(), value.getOrderNo(), value.getChannel(), value.getAmount(),
                value.getStatus(), value.getChannelRefundNo());
    }

    /** 转换为持久化对象。 */
    public static PaymentRefundDO toDO(PaymentRefund value) {
        return new PaymentRefundDO().setRefundNo(value.refundNo()).setAfterSaleNo(value.afterSaleNo())
                .setPaymentNo(value.paymentNo()).setOrderNo(value.orderNo()).setChannel(value.channel())
                .setAmount(value.amount()).setStatus(value.status()).setChannelRefundNo(value.channelRefundNo());
    }
}
