package com.lemnt.promotion.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.product.client.event.StockPreReservedEvent;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 库存预占事实只做资源对应校验，不再次扣减活动或商品库存。
 */
@Component
@RequiredArgsConstructor
public class StockPreReservedEventHandler {
    private final MessageInboxGateway inbox;
    private final PromotionResourceService resources;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void execute(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.STOCK_PRE_RESERVED);
        var event = JsonUtils.toBean(envelope.payload(), StockPreReservedEvent.class);
        if (event == null
            || !envelope.aggregateNo().equals(event.reservationNo())
            || !envelope.businessKey().equals(event.reservationNo() + ":" + event.skuId())
            || event.quantity() < 1) throw new IllegalArgumentException("预占事件不合法");
        if (!inbox.begin(
            "promotion.stock_pre_reserved.v1",
            envelope.eventNo(),
            envelope.eventType(),
            envelope.businessKey(),
            envelope.payload())) return;
        var plan = resources.plan(event.reservationNo());
        if (plan.lines().stream()
            .filter(i -> event.skuId().equals(i.skuId()))
            .mapToInt(i -> i.quantity())
            .sum()
            != event.quantity()) throw new IllegalArgumentException("库存预占与原订单资源数量不一致");
        if ("NOT_LOCKED".equals(resources.query(event.reservationNo()).status()))
            throw new IllegalStateException("营销资源尚未冻结，请重试预占事实");
    }
}
