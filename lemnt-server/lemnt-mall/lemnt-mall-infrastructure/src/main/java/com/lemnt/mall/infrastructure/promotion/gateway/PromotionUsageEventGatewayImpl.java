package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionUsageEventGateway;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionUsageEventMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** PromotionUsageEvent 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class PromotionUsageEventGatewayImpl implements PromotionUsageEventGateway {
    private final PromotionUsageEventMapper promotionUsageEventMapper;

    /** 追加促销占用状态事件。 */
    @Override
    public void append(String orderNo, long rootId, String from, String to, String reason, long now) {
        if (promotionUsageEventMapper.event(orderNo, rootId, from, to, reason, now) != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }
}
