package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogGateway;
import com.lemnt.mall.domain.promotion.model.*;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionCatalogMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@lombok.RequiredArgsConstructor
public class PromotionCatalogGatewayImpl implements PromotionCatalogGateway {
    private final PromotionCatalogMapper mapper;

    public List<CatalogPricingFacts> facts(List<Long> ids) {
        return mapper.facts(PromotionCatalogRules.ids(ids, 100)).stream()
                .map(
                        r -> {
                            long fen = r.getPrice().movePointRight(2).longValueExact();
                            if (fen <= 0
                                    || r.getStock() == null
                                    || r.getStock() < 0
                                    || r.getSpuId() == null
                                    || r.getSpuId() <= 0
                                    || r.getProductCode() == null
                                    || r.getName() == null)
                                throw new IllegalStateException("Invalid catalog facts");
                            return new CatalogPricingFacts(
                                    r.getSkuId(),
                                    r.getSpuId(),
                                    r.getProductCode(),
                                    r.getName(),
                                    fen,
                                    r.getStock());
                        })
                .toList();
    }

    public List<Long> existingSkuIds(List<Long> ids) {
        return mapper.existingSkuIds(PromotionCatalogRules.ids(ids, 1000));
    }

    public List<Long> existingSpuIds(List<Long> ids) {
        return mapper.existingSpuIds(PromotionCatalogRules.ids(ids, 1000));
    }
}
