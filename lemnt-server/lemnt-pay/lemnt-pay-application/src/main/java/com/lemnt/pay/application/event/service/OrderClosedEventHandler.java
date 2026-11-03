package com.lemnt.pay.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/** 关单仅关闭仍待支付的本地尝试；真实已收款不能被关单覆盖。 */
@Component
@RequiredArgsConstructor
public class OrderClosedEventHandler {
    private final MessageInboxGateway inbox;
    private final OrderPaymentGateway orders;
    private final TradePaymentGateway payments;
    private final PaymentChannelCloseGateway channels;

    @Transactional(rollbackFor = Exception.class)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_CLOSED);
        var event = JsonUtils.toBean(envelope.payload(), OrderClosedEvent.class);
        if (event == null
                || !envelope.aggregateNo().equals(event.orderNo())
                || !event.orderNo().equals(envelope.businessKey())) {
            throw new IllegalArgumentException("关单事件的订单标识不一致");
        }
        inbox.begin("pay.order.fence.v1",event.orderNo(),"PAYMENT_ORDER_FENCE",event.orderNo(),
                JsonUtils.toJsonStr(Map.of("orderNo",event.orderNo())));
        if (!inbox.begin(
                "pay.order.closed.v1",
                envelope.eventNo(),
                envelope.eventType(),
                envelope.businessKey(),
                envelope.payload())) return;
        var order = orders.findByOrderNo(event.orderNo());
        if (order == null
                || !"CLOSED".equals(order.status())
                || !order.memberId().equals(event.memberId())) {
            throw new IllegalStateException("原订单尚未关闭或归属不一致");
        }
        for (var payment : payments.findByOrderNo(event.orderNo())) {
            if ("WAITING".equals(payment.status())) {
                if(!channels.closeUnpaid(payment)) throw new IllegalStateException("渠道未确认关闭原支付尝试，请重试核对");
                payments.markClosed(payment.paymentNo());
            }
        }
    }
}
