package com.lemnt.trade.domain.promotion.gateway;

import com.lemnt.trade.domain.promotion.model.*;
import java.util.List;

public interface PromotionCatalogFactsGateway {
    List<CatalogPricingFacts> facts(List<Long> skuIds);

    void validateScope(PromotionScope scope);
}
