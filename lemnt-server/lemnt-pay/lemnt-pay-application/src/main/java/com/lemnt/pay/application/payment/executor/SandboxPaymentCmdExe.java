package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 沙箱收银台：会员确认后本地模拟渠道成功通知，仅在沙箱开关开启时可用。
 */
@Component
@ConditionalOnProperty(prefix = "payment.sandbox", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SandboxPaymentCmdExe {
    private final OrderPaymentGateway orders;
    private final TradePaymentGateway payments;
    private final PaymentNotifyCmdExe notify;

    public String execute(Long memberId, String paymentNo) {
        if (memberId == null
            || memberId <= 0
            || paymentNo == null
            || paymentNo.isBlank()
            || paymentNo.length() > 40) {
            throw new ServiceException("支付单编号不合法", 400);
        }
        var payment = payments.findByPaymentNo(paymentNo);
        if (payment == null || !PaymentChannel.SANDBOX.name().equals(payment.channel())) {
            throw new ServiceException("支付单不存在", 400);
        }
        var order = orders.findByOrderNo(payment.orderNo());
        if (order == null || !memberId.equals(order.memberId())) {
            throw new ServiceException("支付单不存在", 400);
        }
        if ("SUCCESS".equals(payment.status())
            && Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status())) {
            return paymentNo;
        }
        if (!"WAITING".equals(payment.status())
            || !"PENDING_PAYMENT".equals(order.status())
            || payment.amount() == null
            || payment.amount().signum() <= 0) {
            throw new ServiceException("支付单或订单状态不允许支付", 400);
        }
        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT)
                .substring(0, 24);
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                payment.orderNo(),
                PaymentChannel.SANDBOX.name(),
                "SBAPP-" + suffix,
                payment.amount(),
                "NAPP-" + suffix));
        return paymentNo;
    }
}
