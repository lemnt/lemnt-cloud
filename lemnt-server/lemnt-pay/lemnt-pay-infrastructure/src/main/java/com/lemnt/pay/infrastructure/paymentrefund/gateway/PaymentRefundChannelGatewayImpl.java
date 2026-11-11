package com.lemnt.pay.infrastructure.paymentrefund.gateway;

import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 退款渠道网关：先按原退款单号查渠道，查不到才提交新退款。
 */
@Component
@RequiredArgsConstructor
public class PaymentRefundChannelGatewayImpl implements PaymentRefundChannelGateway {
    private final PaymentChannelFactory channels;
    private final PayChannelConfigResolver configs;

    @Override
    public PaymentRefundResult refund(PaymentRefund refund, PaymentRecord payment) {
        AbstractChannelAdapter adapter = channels.getChannel(refund.channel());
        PayChannelConfig config = configs.resolve(payment.appKey(), payment.channel());
        if (config == null) {
            throw new IllegalStateException("退款渠道配置未启用或不唯一");
        }
        var result = adapter.refundQuery(refund, payment, config);
        if (result != null) {
            return result;
        }
        // 渠道已受理但查询暂不可见时保留待退款，不能重新发起另一笔交易。
        if (refund.channelRefundNo() != null) {
            return new PaymentRefundResult(
                refund.refundNo(),
                refund.paymentNo(),
                refund.channel(),
                refund.amount(),
                "WAITING",
                refund.channelRefundNo());
        }
        return adapter.refund(refund, payment, config);
    }
}
