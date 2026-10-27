package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionUsageGateway;
import com.lemnt.mall.domain.promotion.model.PromotionUsage;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionUsageMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionUsage 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionUsageGatewayImpl implements PromotionUsageGateway {
    private final PromotionUsageMapper promotionUsageMapper;

    @Override
    /**
     * 锁定使用。
     */
    public PromotionUsage lockUsage(String n, long id) {
        return PromotionConverter.usage(promotionUsageMapper.lockUsage(n, id));
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 保存促销占用记录。 */
    @Override
    public void insertUsage(PromotionUsage value, long now) {
        one(promotionUsageMapper.insertUsage(PromotionConverter.row(value), now));
    }

    /** 完成促销占用记录。 */
    @Override
    public void finishUsage(PromotionUsage value, PromotionUsage.State state, long now) {
        one(promotionUsageMapper.finishUsage(value.orderNo(), value.rootId(), state.name(), now));
    }
}
