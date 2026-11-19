package com.lemnt.promotion.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.domain.event.gateway.InboundEventGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 关单按原订单会员和资源终态处理，释放重放沿用资源围栏。
 */
@Component
@RequiredArgsConstructor
public class OrderClosedEventHandler {
    private final PromotionResourceService resources;
    private final InboundEventGateway inboundEvents;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_CLOSED);
        var event = inboundEvents.closedFacts(envelope.payload());
        if (event == null
            || !envelope.aggregateNo().equals(event.orderNo())
            || !envelope.businessKey().equals(event.orderNo()))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
        var plan = resources.plan(event.orderNo());
        if (!plan.memberId().equals(event.memberId()))
            throw new IllegalArgumentException("关单会员与原订单不符");
        resources.rollback(event.orderNo());
    }
}
