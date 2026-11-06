package com.lemnt.promotion.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderCompletedEvent;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 订单事实驱动本地权益，消费凭证与业务效果同事务。
 */
@Component
@RequiredArgsConstructor
public class OrderCompletedEventHandler {
    private final MessageInboxGateway inbox;
    private final PromotionResourceService resources;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_COMPLETED);
        var event = JsonUtils.toBean(envelope.payload(), OrderCompletedEvent.class);
        if (event == null
            || !envelope.aggregateNo().equals(event.orderNo())
            || !envelope.businessKey().equals(event.orderNo()))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
        if (!inbox.begin(
            "promotion.order_completed.v1",
            envelope.eventNo(),
            envelope.eventType(),
            envelope.businessKey(),
            envelope.payload())) return;
        var plan = resources.plan(event.orderNo());
        if (!plan.memberId().equals(event.memberId())
            || !Set.of("COMPLETED", "REFUNDED").contains(plan.status())
            || !"PAID".equals(resources.query(event.orderNo()).status()))
            throw new IllegalStateException("订单完成前置状态不一致");
        // 完成事实由本库凭证保存；原赠券在支付时发放，不另造完成奖励规则。
    }
}
