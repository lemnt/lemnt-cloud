package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.order.gateway.OrderRecoveryGateway;
import com.lemnt.order.infrastructure.order.mapper.OrderRecoveryMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 扫描只决定候选，执行时仍在原行锁下核验状态。 */
@Component
@RequiredArgsConstructor
public class OrderRecoveryGatewayImpl implements OrderRecoveryGateway {
    private final OrderRecoveryMapper mapper;

    private int limit(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    public List<String> pendingPaid(int size) {
        return mapper.pendingPaid(limit(size));
    }

    public List<String> pendingRefunds(int size) {
        return mapper.pendingRefunds(limit(size));
    }

    public List<Long> pendingWithdrawals(int size) {
        return mapper.pendingWithdrawals(limit(size));
    }
}
