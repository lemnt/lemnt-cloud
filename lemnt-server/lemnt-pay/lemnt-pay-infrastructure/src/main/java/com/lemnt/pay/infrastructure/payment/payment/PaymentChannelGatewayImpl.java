package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按渠道路由适配器；缺失或重复适配器都明确失败，不模拟支付成功。
 */
@Component
public class PaymentChannelGatewayImpl implements PaymentChannelGateway {
    private final List<PaymentChannelAdapter> adapters;

    public PaymentChannelGatewayImpl(List<PaymentChannelAdapter> adapters) {
        this.adapters = List.copyOf(adapters);
    }

    @Override
    public PaymentPrepay prepay(
        PaymentRecord payment, PayChannelConfig config, String payerOpenId) {
        return adapter(payment.channel()).prepay(payment, config, payerOpenId);
    }

    @Override
    public VerifiedPaymentNotification verify(
        String channel,
        PayChannelConfig config,
        PaymentChannelNotification notification) {
        return adapter(channel).verify(config, notification);
    }

    private PaymentChannelAdapter adapter(String channel) {
        var matching = adapters.stream().filter(value -> value.channel().equals(channel)).toList();
        if (matching.size() != 1) {
            throw new ServiceException("支付渠道未接入", 400);
        }
        return matching.get(0);
    }
}
