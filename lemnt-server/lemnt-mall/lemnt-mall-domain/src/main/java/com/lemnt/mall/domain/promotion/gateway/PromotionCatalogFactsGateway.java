package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionPricingFacts;
import com.lemnt.mall.domain.promotion.model.PromotionScope;

import java.util.List;

/**
 * 营销目录事实领域网关。
 */
public interface PromotionCatalogFactsGateway {
    /**
     * 事实。
     */
    List<PromotionPricingFacts> facts(List<Long> skuIds);

    /**
     * 校验范围。
     */
    void validateScope(PromotionScope scope);
}
