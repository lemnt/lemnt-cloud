package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付渠道关闭领域网关实现。
 */
@Component
public class PaymentChannelCloseGatewayImpl implements PaymentChannelCloseGateway {
    private final List<PaymentChannelCloseClient> clients;
    private final PayChannelConfigResolver configs;

    /**
     * 创建支付渠道关闭领域网关实现实例。
     */
    public PaymentChannelCloseGatewayImpl(
        List<PaymentChannelCloseClient> clients, PayChannelConfigResolver configs) {
        this.clients = List.copyOf(clients);
        this.configs = configs;
    }

    @Override
    public boolean closeUnpaid(PaymentRecord payment) {
        var matching =
            clients.stream()
                .filter(client -> client.channel().equals(payment.channel()))
                .toList();
        PayChannelConfig config = configs.resolve(payment.appKey(), payment.channel());

        // Missing, ambiguous or unconfigured adapters cannot authorize inventory release.
        return matching.size() == 1
            && config != null
            && matching.get(0).closeUnpaid(payment, config);
    }
}
