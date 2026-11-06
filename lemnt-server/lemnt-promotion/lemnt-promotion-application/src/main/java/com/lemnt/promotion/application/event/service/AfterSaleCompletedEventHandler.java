package com.lemnt.promotion.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 订单事实驱动本地权益，消费凭证与业务效果同事务。
 */
@Component
@RequiredArgsConstructor
public class AfterSaleCompletedEventHandler {
    private final MessageInboxGateway inbox;
    private final PromotionResourceService resources;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.AFTER_SALE_COMPLETED);
        var event = JsonUtils.toBean(envelope.payload(), AfterSaleCompletedEvent.class);
        if (event == null
            || !envelope.aggregateNo().equals(event.orderNo())
            || !envelope.businessKey().equals(event.afterSaleNo()))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
        if (!inbox.begin(
            "promotion.after_sale_completed.v1",
            envelope.eventNo(),
            envelope.eventType(),
            envelope.businessKey(),
            envelope.payload())) return;
        if (event.ruleVersion() != 1
            || event.quantity() < 1
            || event.skuId() == null
            || event.amount() == null
            || event.amount().signum() < 0
            || event.returnUsePoint() < 0
            || event.deductGivePoint() < 0) throw new IllegalArgumentException("售后冻结权益不合法");
        var plan = resources.plan(event.orderNo());
        if (!plan.memberId().equals(event.memberId()))
            throw new IllegalArgumentException("售后会员与原订单不符");
        resources.refund(
            event.orderNo(),
            event.afterSaleNo(),
            event.skuId(),
            event.quantity(),
            event.fullyRefunded());
    }
}
