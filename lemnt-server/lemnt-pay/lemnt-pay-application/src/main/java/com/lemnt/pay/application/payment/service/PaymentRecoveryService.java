package com.lemnt.pay.application.payment.service;

import com.lemnt.pay.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/** 回调和定时查单共用资金状态写入逻辑，查单只读渠道。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRecoveryService {
    private static final int BATCH = 50;

    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final PaymentChannelGateway paymentChannels;
    private final PaymentRefundChannelGateway refundChannels;
    private final PaymentNotificationService paymentResults;
    private final PaymentRefundService refundResults;

    private long paymentCursor;
    private String refundCursor = "";

    /** 每轮最多查一批，失败项在下次遍历时重试。 */
    public void syncPayments() {
        var batch = payments.waitingForQuery(paymentCursor, BATCH);
        for (var payment : batch) {
            try {
                var result = paymentChannels.query(payment);
                if (result != null) paymentResults.execute(result);
            } catch (RuntimeException error) {
                log.warn("渠道支付查单失败，支付单 {}", payment.paymentNo(), error);
            }
            paymentCursor = payment.id();
        }
        if (batch.size() < BATCH) paymentCursor = 0;
    }

    /** 查询原退款单，查询不可见时保留原状态。 */
    public void syncRefunds() {
        var batch = refunds.waitingForQuery(refundCursor, BATCH);
        for (var refund : batch) {
            try {
                var payment = payments.findByPaymentNo(refund.paymentNo());
                if (payment == null || !"SUCCESS".equals(payment.status())) {
                    throw new IllegalStateException("原成功支付单缺失");
                }
                var result = refundChannels.query(refund, payment);
                if (result != null && !"WAITING".equals(result.status())) {
                    refundResults.complete(refund, result);
                }
            } catch (RuntimeException error) {
                log.warn("渠道退款查单失败，退款单 {}", refund.refundNo(), error);
            }
            refundCursor = refund.refundNo();
        }
        if (batch.size() < BATCH) refundCursor = "";
    }

    /** 验签后以原应用和支付单核对退款归属，资金事务完成才应答。 */
    public void receiveRefund(String appKey, String channel, PaymentChannelNotification notification) {
        String normalized = channel.toUpperCase(Locale.ROOT);
        var result = refundChannels.verify(appKey, normalized, notification);
        var refund = refunds.find(result.refundNo());
        var payment = payments.findByPaymentNo(result.paymentNo());
        if (refund == null || payment == null || !"SUCCESS".equals(payment.status())
                || !appKey.equals(payment.appKey()) || !normalized.equals(payment.channel())
                || !payment.paymentNo().equals(refund.paymentNo())) {
            throw new IllegalStateException("退款回调与原应用、渠道或支付单不一致");
        }
        refundResults.complete(refund, result);
    }
}
