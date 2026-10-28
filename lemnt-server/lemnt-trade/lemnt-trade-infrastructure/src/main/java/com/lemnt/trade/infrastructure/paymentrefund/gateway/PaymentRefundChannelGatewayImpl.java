package com.lemnt.trade.infrastructure.paymentrefund.gateway;

import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.trade.infrastructure.paymentrefund.channel.PaymentRefundClient;

import org.springframework.stereotype.Component;

import java.util.List;

/** 按原支付渠道路由退款请求。 */
@Component
public class PaymentRefundChannelGatewayImpl implements PaymentRefundChannelGateway {
    private final List<PaymentRefundClient> clients;

    public PaymentRefundChannelGatewayImpl(List<PaymentRefundClient> clients) {
        this.clients = List.copyOf(clients);
    }

    @Override
    public PaymentRefundResult refund(PaymentRefund refund, PaymentRecord payment) {
        var matching = clients.stream().filter(client -> client.channel().equals(refund.channel())).toList();
        if (matching.size() != 1) {
            throw new IllegalStateException("退款渠道未接入或存在重复适配器");
        }
        var client = matching.get(0);
        var result = client.query(refund, payment);
        if (result != null) {
            return result;
        }
        // 渠道已受理但查询暂不可见时保留待退款，不能重新发起另一笔交易。
        if (refund.channelRefundNo() != null) {
            return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(),
                    refund.amount(), "WAITING", refund.channelRefundNo());
        }
        return client.refund(refund, payment);
    }
}
