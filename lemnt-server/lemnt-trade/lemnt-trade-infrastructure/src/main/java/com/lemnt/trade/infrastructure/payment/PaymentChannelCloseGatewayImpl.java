package com.lemnt.trade.infrastructure.payment;

import com.lemnt.trade.domain.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.model.PaymentRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentChannelCloseGatewayImpl implements PaymentChannelCloseGateway {
    private final List<PaymentChannelCloseClient> clients;

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
