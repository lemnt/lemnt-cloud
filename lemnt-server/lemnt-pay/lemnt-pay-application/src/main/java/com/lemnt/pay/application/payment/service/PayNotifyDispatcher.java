package com.lemnt.pay.application.payment.service;

import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.client.event.PayRefundSuccessEvent;
import com.lemnt.pay.client.event.PaySuccessEvent;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PayNotifyTask;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 兼容存量支付通知任务，只登记资金事件；订单/售后权益不再由pay直接执行。 */
@Component
@RequiredArgsConstructor
public class PayNotifyDispatcher {
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final OrderPaymentGateway orders;
    private final MessageOutboxGateway outbox;

    @Transactional(rollbackFor = Exception.class)
    public void dispatch(PayNotifyTask task) {
        var order = orders.findByOrderNo(task.orderNo());
        if (order == null) throw new IllegalStateException("原订单归属缺失，请对账");
        if ("PAY".equals(task.type())) {
            var payment = payments.findByPaymentNo(task.bizNo());
            if (payment == null
                    || !"SUCCESS".equals(payment.status())
                    || !task.orderNo().equals(payment.orderNo()))
                throw new IllegalStateException("支付未成功或订单不符");
            outbox.enqueue(
                    CommerceEventTopics.PAY_SUCCESS,
                    payment.paymentNo(),
                    payment.orderNo(),
                    JsonUtils.toJsonStr(
                            new PaySuccessEvent(
                                    payment.paymentNo(),
                                    payment.orderNo(),
                                    order.memberId(),
                                    payment.channel(),
                                    payment.amount(),
                                    payment.channelTradeNo())));
        } else if ("REFUND".equals(task.type())) {
            var refund = refunds.find(task.bizNo());
            if (refund == null
                    || !"SUCCESS".equals(refund.status())
                    || !task.orderNo().equals(refund.orderNo()))
                throw new IllegalStateException("退款未成功或订单不符");
            outbox.enqueue(
                    CommerceEventTopics.PAY_REFUND_SUCCESS,
                    refund.refundNo(),
                    refund.orderNo(),
                    JsonUtils.toJsonStr(
                            new PayRefundSuccessEvent(
                                    refund.refundNo(),
                                    refund.afterSaleNo(),
                                    refund.paymentNo(),
                                    refund.orderNo(),
                                    order.memberId(),
                                    refund.channel(),
                                    refund.amount(),
                                    refund.channelRefundNo())));
        } else {
            throw new IllegalStateException("AFTER_SALE权益任务归order，须按切流清单迁移后重试");
        }
    }
}
