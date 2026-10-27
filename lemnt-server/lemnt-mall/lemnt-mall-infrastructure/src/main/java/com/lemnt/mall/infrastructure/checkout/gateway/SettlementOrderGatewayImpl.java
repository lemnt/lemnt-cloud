package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway;
import com.lemnt.mall.infrastructure.checkout.mapper.SettlementOrderMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementOrder 持久化实现。 */
@Component
@RequiredArgsConstructor
public class SettlementOrderGatewayImpl implements SettlementOrderGateway {
    private final SettlementOrderMapper settlementOrderMapper;

    /**
     * 订单编号。
     */
    public String orderNo(Long memberId, String id) {
        return settlementOrderMapper.orderNo(memberId, id);
    }

    /**
     * 绑定。
     */
    public void bind(Long memberId, String id, String orderNo) {
        settlementOrderMapper.bind(memberId, id, orderNo);
    }
}
