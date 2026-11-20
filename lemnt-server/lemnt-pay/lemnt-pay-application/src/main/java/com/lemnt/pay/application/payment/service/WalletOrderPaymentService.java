package com.lemnt.pay.application.payment.service;

import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.domain.channel.model.PaymentChannel;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.gateway.WalletOrderLockGateway;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.payment.model.WalletBizType;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 钱包扣款、支付事实与通知任务在同一支付库事务提交。 */
@Component
@RequiredArgsConstructor
public class WalletOrderPaymentService {
    private final WalletOrderLockGateway lock;
    private final OrderPaymentGateway orders;
    private final TradePaymentCreateCmdExe payments;
    private final TradePaymentGateway records;
    private final WalletService wallet;
    private final PaymentNotificationService notification;

    /** 只从本人订单取原金额，钱包流水以支付单号幂等。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentRecord pay(String orderNo, Long memberId, String appKey) {
        lock.lock(orderNo);
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !memberId.equals(order.memberId())) {
            throw new IllegalStateException("订单不允许使用钱包支付");
        }
        String channel = PaymentChannel.WALLET.name();
        var existing = records.findByOrderNoAndChannel(orderNo, channel);
        if (existing != null && "SUCCESS".equals(existing.status())) {
            if (!appKey.equals(existing.appKey())) throw new IllegalStateException("钱包支付单已绑定其他应用");
            return existing;
        }
        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new IllegalStateException("订单不允许使用钱包支付");
        }
        var payment = payments.execute(orderNo, channel, appKey);
        if (!appKey.equals(payment.appKey())
                || (!"WAITING".equals(payment.status()) && !"SUCCESS".equals(payment.status()))) {
            throw new IllegalStateException("钱包支付单的应用或状态不一致");
        }
        wallet.debit(
                memberId, WalletBizType.PAYMENT, payment.paymentNo(), payment.amount(), "订单余额支付");
        notification.execute(
                new VerifiedPaymentNotification(
                        payment.paymentNo(),
                        orderNo,
                        channel,
                        payment.paymentNo(),
                        payment.amount(),
                        payment.paymentNo()));
        return records.findByPaymentNo(payment.paymentNo());
    }
}
