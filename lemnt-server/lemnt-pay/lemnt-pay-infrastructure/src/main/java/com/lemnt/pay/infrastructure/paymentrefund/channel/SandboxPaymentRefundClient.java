package com.lemnt.pay.infrastructure.paymentrefund.channel;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 沙箱退款：本地直接确认成功，不发起真实渠道交易；仅在显式开启时注册。
 */
@Component
@ConditionalOnProperty(prefix = "payment.sandbox", name = "enabled", havingValue = "true")
public class SandboxPaymentRefundClient implements PaymentRefundClient {
    private static final int CHANNEL_NO_MAX = 128;

    @Override
    public String channel() {
        return PaymentChannel.SANDBOX.name();
    }

    /**
     * 沙箱没有远端退款单，返回空表示本地尚无渠道结果。
     */
    @Override
    public PaymentRefundResult query(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireSandbox(refund, payment);
        return null;
    }

    /**
     * 原退款编号生成稳定的渠道退款编号，重复调用得到同一结果。
     */
    @Override
    public PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireSandbox(refund, payment);
        String channelRefundNo = "SBR-" + refund.refundNo();
        if (channelRefundNo.length() > CHANNEL_NO_MAX) {
            throw new IllegalStateException("沙箱渠道退款编号过长");
        }
        return new PaymentRefundResult(
            refund.refundNo(),
            refund.paymentNo(),
            refund.channel(),
            refund.amount(),
            "SUCCESS",
            channelRefundNo);
    }

    private void requireSandbox(PaymentRefund refund, PaymentRecord payment) {
        if (refund == null
            || payment == null
            || !PaymentChannel.SANDBOX.name().equals(refund.channel())
            || !PaymentChannel.SANDBOX.name().equals(payment.channel())
            || !refund.paymentNo().equals(payment.paymentNo())
            || refund.amount() == null
            || payment.amount() == null
            || refund.amount().compareTo(payment.amount()) > 0
            || !"SUCCESS".equals(payment.status())
            || payment.channelTradeNo() == null
            || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("沙箱退款与原支付不一致");
        }
    }
}
