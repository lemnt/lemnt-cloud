package com.lemnt.order.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.order.application.aftersale.service.AfterSaleRefundCoordinator;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.event.gateway.InboundEventGateway;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 支付退款消息只触发原冻结售后完成，不能从消息或当前配置重新决定退款金额。
 */
@Component
@RequiredArgsConstructor
public class PayRefundSuccessEventHandler {
    private final MessageInboxGateway inbox;
    private final PaymentRefundGateway refunds;
    private final AfterSaleGateway afterSales;
    private final AfterSaleRefundCoordinator coordinator;
    private final InboundEventGateway inboundEvents;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.PAY_REFUND_SUCCESS);
        var v = inboundEvents.refundSucceededFacts(envelope.payload());
        if (v == null
            || !envelope.aggregateNo().equals(v.orderNo())
            || !envelope.businessKey().equals(v.refundNo()))
            throw new IllegalArgumentException("现金退款事件标识不一致");
        if (!inbox.begin(
            "order.pay_refund_success.v1",
            envelope.eventNo(),
            envelope.eventType(),
            envelope.businessKey(),
            envelope.payload())) return;
        var af = afterSales.afterSale(v.afterSaleNo());
        var fact = refunds.findByAfterSaleNo(v.afterSaleNo());
        if (af == null
            || !af.memberId().equals(v.memberId())
            || fact == null
            || !"SUCCESS".equals(fact.status())
            || !Objects.equals(v.refundNo(), fact.refundNo())
            || !Objects.equals(v.orderNo(), fact.orderNo())
            || !Objects.equals(v.paymentNo(), fact.paymentNo())
            || !Objects.equals(v.channel(), fact.channel())
            || !Objects.equals(v.channelRefundNo(), fact.channelRefundNo())
            || v.amount() == null
            || v.amount().compareTo(fact.amount()) != 0)
            throw new IllegalStateException("现金退款事实不一致，请先对账");
        coordinator.cashSucceeded(fact);
    }
}
