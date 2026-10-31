package com.lemnt.trade.application.payment.service;

import com.lemnt.trade.application.order.service.OrderRemoteService;
import com.lemnt.trade.application.paymentrefund.service.RefundBenefitsService;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;
import com.lemnt.trade.domain.payment.model.PayNotifyType;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 按任务类型派发下游：支付确认库存核券，退款回退权益。派发目标自身幂等。 */
@Component
@RequiredArgsConstructor
public class PayNotifyDispatcher {
    private final OrderRemoteService orders;
    private final PaymentRefundGateway refunds;
    private final RefundBenefitsService benefits;

    public void dispatch(PayNotifyTask task) {
        if (PayNotifyType.PAY.name().equals(task.type())) {
            orders.paid(task.orderNo());
            return;
        }
        if (PayNotifyType.REFUND.name().equals(task.type())) {
            var refund = refunds.find(task.bizNo());
            if (refund == null || !"SUCCESS".equals(refund.status())) {
                throw new IllegalStateException("退款单不存在或未成功，不能派发退款权益");
            }
            benefits.complete(refund);
            return;
        }
        throw new IllegalArgumentException("未知派发类型");
    }
}
