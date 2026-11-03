package com.lemnt.promotion.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.application.promotion.service.RewardGiftService;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 订单事实驱动本地权益，消费凭证与业务效果同事务。 */
@Component
@RequiredArgsConstructor
public class OrderPaidEventHandler {
    private final MessageInboxGateway inbox;
    private final PromotionResourceService resources;
    private final RewardGiftService gifts;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_PAID);
        var event = JsonUtils.toBean(envelope.payload(), OrderPaidEvent.class);
        if (event == null
                || !envelope.aggregateNo().equals(event.orderNo())
                || !envelope.businessKey().equals(event.orderNo()))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
        if (!inbox.begin(
                "promotion.order_paid.v1",
                envelope.eventNo(),
                envelope.eventType(),
                envelope.businessKey(),
                envelope.payload())) return;
        var plan = resources.plan(event.orderNo());
        if (!plan.memberId().equals(event.memberId())
                || event.amount() == null
                || event.amount().signum() < 0
                || event.usePoint() < 0
                || event.givePoint() < 0) throw new IllegalArgumentException("原订单支付权益不合法");
        resources.confirm(event.orderNo());
        gifts.issuePaid(event.memberId(), event.orderNo(), event.giveCouponTemplateCounts());
    }
}
