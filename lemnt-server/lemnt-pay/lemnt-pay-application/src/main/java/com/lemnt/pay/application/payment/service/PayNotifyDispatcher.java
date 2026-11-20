package com.lemnt.pay.application.payment.service;

import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.PayAppNotifyGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PayNotifyTask;
import com.lemnt.pay.domain.payment.model.PayNotifyType;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 依据原资金事实通知订单或支付应用，分别确认下游处理结果。
 */
@Component
@RequiredArgsConstructor
public class PayNotifyDispatcher {
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final OrderPaymentGateway orders;
    private final PayAppNotifyGateway appNotify;

    public void dispatch(PayNotifyTask task) {
        var type = PayNotifyType.valueOf(task.type());
        if (type == PayNotifyType.PAY || type == PayNotifyType.PAY_HTTP) {
            var payment = payments.findByPaymentNo(task.bizNo());
            if (payment == null
                || !"SUCCESS".equals(payment.status())
                || !task.orderNo().equals(payment.orderNo()))
                throw new IllegalStateException("支付未成功或订单不符");
            var master = payments.findPaymentOrder(payment.orderNo());
            if (master != null && "RECONCILE_REQUIRED".equals(master.status())) {
                throw new IllegalStateException("支付主单待对账");
            }
            if (type == PayNotifyType.PAY) {
                orders.notifyPaid(payment.orderNo(), payment.paymentNo());
            } else {
                appNotify.post(
                        payment.notifyUrl(),
                        Map.of(
                                "orderNo",
                                payment.orderNo(),
                                "paymentNo",
                                payment.paymentNo(),
                                "status",
                                "SUCCESS"));
            }
        } else if (type != PayNotifyType.AFTER_SALE) {
            var refund = refunds.find(task.bizNo());
            boolean failed = type == PayNotifyType.REFUND_FAIL_HTTP;
            if (refund == null
                    || (!failed && !"SUCCESS".equals(refund.status()))
                    || !task.orderNo().equals(refund.orderNo()))
                throw new IllegalStateException("退款未成功或订单不符");
            if (type == PayNotifyType.REFUND) {
                orders.notifyRefunded(refund.orderNo(), refund.afterSaleNo(), refund.refundNo());
            } else {
                appNotify.post(
                        refund.notifyUrl(),
                        Map.of(
                                "orderNo",
                                refund.orderNo(),
                                "paymentNo",
                                refund.paymentNo(),
                                "afterSaleNo",
                                refund.afterSaleNo(),
                                "refundNo",
                                refund.refundNo(),
                                "status",
                                failed ? "FAILED" : "SUCCESS"));
            }
        } else {
            throw new IllegalStateException("AFTER_SALE权益任务归order，须按切流清单迁移后重试");
        }
    }
}
