package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway;
import com.lemnt.mall.infrastructure.checkout.mapper.SettlementAbandonMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementAbandon 持久化实现。 */
@Component
@RequiredArgsConstructor
public class SettlementAbandonGatewayImpl implements SettlementAbandonGateway {
    private final SettlementAbandonMapper settlementAbandonMapper;

    /**
     * 已放弃。
     */
    public boolean abandoned(Long memberId, String id) {
        return settlementAbandonMapper.abandoned(memberId, id) > 0;
    }

    /**
     * 放弃。
     */
    public void abandon(Long memberId, String id) {
        settlementAbandonMapper.abandon(memberId, id);
    }
}
