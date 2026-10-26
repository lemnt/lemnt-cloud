package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.client.PromotionCatalogClient;
import com.lemnt.mall.client.bo.*;
import com.lemnt.mall.client.dto.*;
import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogGateway;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogRules;
import java.util.*;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@lombok.RequiredArgsConstructor
public class PromotionCatalogClientImpl implements PromotionCatalogClient {
    private final PromotionCatalogGateway gateway;

    public List<ProductPricingFactsDTO> findPricingFactsBatch(PricingFactsBatchBo input) {
        if (input == null) throw new IllegalArgumentException("Missing input");
        var ids = PromotionCatalogRules.ids(input.skuIds(), 100);
        return gateway.facts(ids).stream()
                .map(
                        f ->
                                new ProductPricingFactsDTO(
                                        "MALL_PRICING_V1",
                                        f.skuId(),
                                        f.spuId(),
                                        f.productCode(),
                                        f.name(),
                                        f.unitPriceFen(),
                                        f.availableStock(),
                                        f.hash()))
                .toList();
    }

    public PromotionScopeValidationDTO validateScope(PromotionScopeValidationBo input) {
        if (input == null
                || !Set.of("SKU_IDS", "SPU_IDS")
                        .contains(input.scopeType() == null ? "" : input.scopeType()))
            throw new IllegalArgumentException("Unsupported scope");
        var ids = PromotionCatalogRules.ids(input.ids(), 1000);
        var existing =
                new HashSet<>(
                        "SKU_IDS".equals(input.scopeType())
                                ? gateway.existingSkuIds(ids)
                                : gateway.existingSpuIds(ids));
        return new PromotionScopeValidationDTO(
                ids.stream().filter(id -> !existing.contains(id)).toList());
    }
}
