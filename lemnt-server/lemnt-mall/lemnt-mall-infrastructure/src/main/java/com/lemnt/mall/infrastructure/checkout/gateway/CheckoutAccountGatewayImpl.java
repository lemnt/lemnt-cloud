package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.infrastructure.checkout.mapper.CheckoutAccountMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** CheckoutAccount 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CheckoutAccountGatewayImpl implements CheckoutAccountGateway {
    private final CheckoutAccountMapper checkoutAccountMapper;

    @Override
    public LocalDateTime now() {
        return checkoutAccountMapper.now();
    }

    @Override
    public void lockMember(Long memberId) {
        checkoutAccountMapper.ensureMember(memberId);
        if (checkoutAccountMapper.lockMember(memberId) == null) {
            throw new IllegalStateException("会员结算锁失败");
        }
    }
}
