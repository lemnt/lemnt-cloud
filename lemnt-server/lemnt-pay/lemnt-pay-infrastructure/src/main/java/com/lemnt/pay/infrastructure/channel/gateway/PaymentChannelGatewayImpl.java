package com.lemnt.pay.infrastructure.channel.gateway;

import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 支付渠道网关：只做领域接口到渠道实现的转发，渠道路由由工厂负责。
 */
@Component
@RequiredArgsConstructor
public class PaymentChannelGatewayImpl implements PaymentChannelGateway {
    private final PaymentChannelFactory channels;

    @Override
    public PaymentPrepay prepay(
        PaymentRecord payment, PayChannelConfig config, String payerOpenId) {
        return channels.getChannel(payment.channel()).payment(payment, config, payerOpenId);
    }

    @Override
    public VerifiedPaymentNotification verify(
        String channel,
        PayChannelConfig config,
        PaymentChannelNotification notification) {
        return channels.getChannel(channel).paymentCallback(config, notification);
    }
}
