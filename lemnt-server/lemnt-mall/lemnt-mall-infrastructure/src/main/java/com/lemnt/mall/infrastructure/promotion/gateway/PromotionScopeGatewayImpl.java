package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionScopeGateway;
import com.lemnt.mall.domain.promotion.model.PublishedPromotion;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionScopeMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** PromotionScope 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class PromotionScopeGatewayImpl implements PromotionScopeGateway {
    private final PromotionScopeMapper promotionScopeMapper;

    /** 写入促销版本的范围集合。 */
    @Override
    public void saveScope(PublishedPromotion value) {
        for (long id : value.draft().scope().ids()) {
            if (promotionScopeMapper.insertScope(value.rootId(),
                    value.version(),
                    value.draft().scope().kind().name(),
                    id) != 1) {
                throw new IllegalStateException("Promotion persistence CAS/integrity failure");
            }
        }
    }
}
