package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.order.service.OrderRemoteService;
import com.lemnt.trade.application.payment.service.PaymentNotificationService;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付通知先提交真实支付状态，再同步确认库存与核券。 */
@Component
@RequiredArgsConstructor
public class PaymentNotifyCmdExe {
    private final PaymentNotificationService notifications;
    private final OrderRemoteService remotes;

    /** 核券失败向调用方返回错误，原通知重试继续处理，不撤销真实支付结果。 */
    public void execute(VerifiedPaymentNotification notification) {
        notifications.execute(notification);
        remotes.paid(notification.orderNo());
    }
}
