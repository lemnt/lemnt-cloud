package com.lemnt.trade.infrastructure.payment.payment;

import com.lemnt.trade.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 支付渠道关闭领域网关实现。
 */
@Component
public class PaymentChannelCloseGatewayImpl implements PaymentChannelCloseGateway {
    private final List<PaymentChannelCloseClient> clients;

    /**
     * 创建支付渠道关闭领域网关实现实例。
     */
    public PaymentChannelCloseGatewayImpl(List<PaymentChannelCloseClient> clients) {
        this.clients = List.copyOf(clients);
    }

    @Override
    public boolean closeUnpaid(PaymentRecord payment) {
        var matching = clients.stream().filter(client -> client.channel().equals(payment.channel())).toList();

        // Missing or ambiguous adapters cannot authorize inventory release.
        return matching.size() == 1 && matching.get(0).closeUnpaid(payment);
    }
}
