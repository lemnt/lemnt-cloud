package com.lemnt.pay.application.payment.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 在同一尝试行锁下串行微信预支付和渠道关单。 */
@Component
@RequiredArgsConstructor
public class PaymentAttemptService {
    private final TradePaymentGateway payments;
    private final PaymentChannelGateway channels;
    private final PaymentChannelCloseGateway closeChannel;

    /** 获取原尝试的预支付参数，到期后等待渠道关单再重试。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentPrepay prepay(PaymentRecord candidate, PayChannelConfig config, String openId) {
        var current = payments.lockForConfirmation(candidate.orderNo(), candidate.paymentNo());
        var order = payments.findPaymentOrder(candidate.orderNo());
        if (order == null
                || !"WAITING".equals(order.status())
                || current == null
                || !"WAITING".equals(current.status())
                || expired(current)) {
            throw new ServiceException("支付尝试已结束或过期，请稍后刷新支付状态", 400);
        }
        return channels.prepay(current, config, openId);
    }

    /** 只关闭渠道明确确认关闭的待支付尝试。 */
    @Transactional(rollbackFor = Exception.class)
    public void closeExpired(String paymentNo) {
        var current = payments.lockByPaymentNo(paymentNo);
        if (current != null
                && "WAITING".equals(current.status())
                && expired(current)
                && closeChannel.closeUnpaid(current)) {
            payments.markClosed(paymentNo);
        }
    }

    private boolean expired(PaymentRecord payment) {
        return payment.expiresAtEpochSecond() != null
                && payment.expiresAtEpochSecond() <= payments.databaseEpochSecond();
    }
}
