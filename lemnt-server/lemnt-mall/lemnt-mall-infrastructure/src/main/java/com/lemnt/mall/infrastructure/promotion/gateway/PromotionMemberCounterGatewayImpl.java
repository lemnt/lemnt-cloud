package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionMemberCounterGateway;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionMemberCounterMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionMemberCounter 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionMemberCounterGatewayImpl implements PromotionMemberCounterGateway {
    private final PromotionMemberCounterMapper promotionMemberCounterMapper;

    @Override
    /**
     * 最大值会员使用。
     */
    public long largestMemberUsage(long id) {
        Long n = promotionMemberCounterMapper.largestMemberUsage(id);
        return n == null ? 0 : n;
    }

    @Override
    /**
     * 锁定会员计数器。
     */
    public void lockMemberCounter(long id, long m) {
        promotionMemberCounterMapper.ensureMemberCounter(id, m);
        if (promotionMemberCounterMapper.lockMemberCounter(id, m) == null) {
            throw new IllegalStateException("Missing member counter");
        }
    }

    @Override
    /**
     * 获取会员。
     */
    public boolean takeMember(long id, long m, Long l) {
        return promotionMemberCounterMapper.takeMember(id, m, l) == 1;
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 结束会员占用额度。 */
    @Override
    public void finish(long id, long memberId, String state) {
        one(promotionMemberCounterMapper.finishMember(id, memberId, state));
    }
}
