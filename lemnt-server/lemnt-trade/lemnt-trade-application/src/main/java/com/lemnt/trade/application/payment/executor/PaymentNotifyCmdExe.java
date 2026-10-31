package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.service.PayNotifyTaskService;
import com.lemnt.trade.application.payment.service.PaymentNotificationService;
import com.lemnt.trade.domain.payment.model.PayNotifyType;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付通知先提交真实支付状态，再通过持久化任务确认库存与核券。 */
@Component
@RequiredArgsConstructor
public class PaymentNotifyCmdExe {
    private final PaymentNotificationService notifications;
    private final PayNotifyTaskService tasks;

    /**
     * 下游派发失败时向调用方返回错误并保留失败任务，调度器会继续重试；
     * 支付与订单状态已提交，不回滚真实支付结果。
     */
    public void execute(VerifiedPaymentNotification notification) {
        notifications.execute(notification);
        if (!tasks.enqueueAndAttempt(PayNotifyType.PAY.name(), notification.paymentNo(), notification.orderNo())) {
            throw new IllegalStateException("支付后处理失败，已登记重试任务");
        }
    }
}
