package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.service.PaymentNotificationService;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 已验签通知交由支付服务登记资金事实及后续通知。 */
@Component
@RequiredArgsConstructor
public class PaymentNotifyCmdExe {
    private final PaymentNotificationService notifications;

    public void execute(VerifiedPaymentNotification notification) {
        notifications.execute(notification);
    }
}
