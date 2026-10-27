package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionCounterGateway;
import com.lemnt.mall.domain.promotion.model.PromotionCounter;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionCounterMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionCounter 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionCounterGatewayImpl implements PromotionCounterGateway {
    private final PromotionCounterMapper promotionCounterMapper;

    @Override
    /**
     * 锁定计数器。
     */
    public PromotionCounter lockCounter(long id) {
        var r = promotionCounterMapper.lockCounter(id);
        if (r == null) {
            throw new IllegalStateException("Missing promotion counter");
        }
        return new PromotionCounter(
                r.getHeldCount(), r.getUsedCount(), r.getGlobalLimit(), r.getMemberLimit());
    }

    @Override
    /**
     * 设置限制。
     */
    public void setLimits(long id, Long g, Long m) {
        one(promotionCounterMapper.setLimits(id, g, m));
    }

    @Override
    /**
     * 获取全局预算。
     */
    public boolean takeGlobal(long id) {
        return promotionCounterMapper.takeGlobal(id) == 1;
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 初始化促销额度计数器。 */
    @Override
    public void initialize(long id) {
        one(promotionCounterMapper.insertCounter(id));
    }

    /** 结束占用的全局额度。 */
    @Override
    public void finish(long id, String state) {
        one(promotionCounterMapper.finishGlobal(id, state));
    }
}
