package com.lemnt.pay.infrastructure.channel.gateway;

import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 支付渠道关闭网关：按渠道实现与配置确认关闭，未配置一律视为未确认。
 */
@Component
@RequiredArgsConstructor
public class PaymentChannelCloseGatewayImpl implements PaymentChannelCloseGateway {
    private final PaymentChannelFactory channels;
    private final PayChannelConfigResolver configs;

    @Override
    public boolean closeUnpaid(PaymentRecord payment) {
        AbstractChannelAdapter adapter = channels.getChannel(payment.channel());
        PayChannelConfig config = configs.resolve(payment.appKey(), payment.channel());

        // Missing configuration cannot authorize inventory release.
        return config != null && adapter.close(payment, config);
    }
}
