package com.lemnt.pay.infrastructure.paymentrefund.gateway;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.payment.payment.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.paymentrefund.channel.PaymentRefundClient;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按原支付渠道路由退款请求。
 */
@Component
public class PaymentRefundChannelGatewayImpl implements PaymentRefundChannelGateway {
    private final List<PaymentRefundClient> clients;
    private final PayChannelConfigResolver configs;

    public PaymentRefundChannelGatewayImpl(
        List<PaymentRefundClient> clients, PayChannelConfigResolver configs) {
        this.clients = List.copyOf(clients);
        this.configs = configs;
    }

    @Override
    public PaymentRefundResult refund(PaymentRefund refund, PaymentRecord payment) {
        var matching =
            clients.stream()
                .filter(client -> client.channel().equals(refund.channel()))
                .toList();
        if (matching.size() != 1) {
            throw new IllegalStateException("退款渠道未接入或存在重复适配器");
        }
        PayChannelConfig config = configs.resolve(payment.appKey(), payment.channel());
        if (config == null) {
            throw new IllegalStateException("退款渠道配置未启用或不唯一");
        }
        var client = matching.get(0);
        var result = client.query(refund, payment, config);
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
        return client.refund(refund, payment, config);
    }
}
