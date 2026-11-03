package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 沙箱支付没有远端渠道交易，关单时直接视为已撤销。 */
@Component
@ConditionalOnProperty(prefix = "payment.sandbox", name = "enabled", havingValue = "true")
public class SandboxPaymentChannelCloseClient implements PaymentChannelCloseClient {
    @Override
    public String channel() {
        return PaymentChannel.SANDBOX.name();
    }

    @Override
    public boolean closeUnpaid(PaymentRecord payment) {
        return PaymentChannel.SANDBOX.name().equals(payment.channel());
    }
}
