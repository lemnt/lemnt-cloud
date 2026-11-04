package com.lemnt.order.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.client.event.PaySuccessEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Set;

/** 资金事实核对成功后才提交订单支付状态和权益事件，迟到资金留待对账，不能重新打开关单。 */
@Component
@RequiredArgsConstructor
public class PaySuccessEventHandler {
    private final MessageInboxGateway inbox;
    private final OrderPersistenceService persistence;
    private final TradeOrderGateway orders;
    private final TradePaymentGateway payments;
    private final OrderLogService logs;
    private final ActivityOrderLifecycleService activities;
    private final OrderEventGateway events;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.PAY_SUCCESS);
        var v = JsonUtils.toBean(envelope.payload(), PaySuccessEvent.class);
        if (v == null
                || !envelope.aggregateNo().equals(v.orderNo())
                || !envelope.businessKey().equals(v.paymentNo()))
            throw new IllegalArgumentException("支付事件标识不一致");
        if (!inbox.begin(
                "order.pay_success.v1",
                envelope.eventNo(),
                envelope.eventType(),
                envelope.businessKey(),
                envelope.payload())) return;
        var order = persistence.findByOrderNoForUpdate(v.orderNo());
        var paid =
                payments.findByOrderNo(v.orderNo()).stream()
                        .filter(p -> "SUCCESS".equals(p.status()))
                        .toList();
        if (order == null
                || !order.memberId().equals(v.memberId())
                || v.amount() == null
                || v.amount().signum() <= 0
                || v.amount().compareTo(order.totalAmount()) != 0
                || paid.size() != 1
                || !paid.get(0).paymentNo().equals(v.paymentNo())
                || !Objects.equals(paid.get(0).channel(), v.channel())
                || !Objects.equals(paid.get(0).channelTradeNo(), v.channelTradeNo())
                || paid.get(0).amount().compareTo(v.amount()) != 0)
            throw new IllegalStateException("资金事实与原订单不一致，请先对账");
        if (Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status()))
            throw new IllegalStateException("订单已支付但无原消费凭证，请核对历史迁移");
        if (!"PENDING_PAYMENT".equals(order.status()))
            throw new IllegalStateException("真实资金到达时订单状态不允许支付，请对账处理");
        if (!orders.transitionStatus(order.orderNo(), "PENDING_PAYMENT", "PAID"))
            throw new IllegalStateException("订单支付状态冲突");
        activities.paid(order.orderNo());
        events.paid(order);
        logs.record(order, "PENDING_PAYMENT", "PAID", 10, TradeOperator.system(), "原支付资金已核验");
    }
}
