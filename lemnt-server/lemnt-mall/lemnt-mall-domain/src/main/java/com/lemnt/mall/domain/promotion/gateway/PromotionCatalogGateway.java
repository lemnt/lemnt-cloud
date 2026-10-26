package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;
import java.util.List;

public interface PromotionCatalogGateway {
    List<CatalogPricingFacts> facts(List<Long> ids);

    List<Long> existingSkuIds(List<Long> ids);

    List<Long> existingSpuIds(List<Long> ids);
}
