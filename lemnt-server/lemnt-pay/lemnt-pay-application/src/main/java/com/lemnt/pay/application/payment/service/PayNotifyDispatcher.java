package com.lemnt.pay.application.payment.service;

import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PayNotifyTask;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 依据真实资金记录通知订单。订单提交成功后，通知任务才确认完成。
 */
@Component
@RequiredArgsConstructor
public class PayNotifyDispatcher {
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final OrderPaymentGateway orders;

    public void dispatch(PayNotifyTask task) {
        if ("PAY".equals(task.type())) {
            var payment = payments.findByPaymentNo(task.bizNo());
            if (payment == null
                || !"SUCCESS".equals(payment.status())
                || !task.orderNo().equals(payment.orderNo()))
                throw new IllegalStateException("支付未成功或订单不符");
            var master = payments.findPaymentOrder(payment.orderNo());
            if (master != null && "RECONCILE_REQUIRED".equals(master.status())) {
                throw new IllegalStateException("支付主单待对账");
            }
            orders.notifyPaid(payment.orderNo(), payment.paymentNo());
        } else if ("REFUND".equals(task.type())) {
            var refund = refunds.find(task.bizNo());
            if (refund == null
                || !"SUCCESS".equals(refund.status())
                || !task.orderNo().equals(refund.orderNo()))
                throw new IllegalStateException("退款未成功或订单不符");
            orders.notifyRefunded(refund.orderNo(), refund.afterSaleNo(), refund.refundNo());
        } else {
            throw new IllegalStateException("AFTER_SALE权益任务归order，须按切流清单迁移后重试");
        }
    }
}
