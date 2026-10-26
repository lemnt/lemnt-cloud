package com.lemnt.trade.infrastructure.promotion.gateway;

import com.lemnt.mall.client.PromotionCatalogClient;
import com.lemnt.mall.client.bo.*;
import com.lemnt.trade.domain.promotion.gateway.PromotionCatalogFactsGateway;
import com.lemnt.trade.domain.promotion.model.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PromotionCatalogFactsGatewayImpl implements PromotionCatalogFactsGateway {
    @DubboReference(check = false, timeout = 3000, retries = 0)
    private PromotionCatalogClient client;

    public List<CatalogPricingFacts> facts(List<Long> skuIds) {
        var ids = ids(skuIds, 100);
        try {
            var values = client.findPricingFactsBatch(new PricingFactsBatchBo(ids));
            if (values == null || values.size() > ids.size()) throw unavailable();
            var seen = new HashSet<Long>();
            var result = new ArrayList<CatalogPricingFacts>();
            for (var f : values) {
                if (f == null
                        || !"MALL_PRICING_V1".equals(f.factsProtocol())
                        || f.skuId() == null
                        || !ids.contains(f.skuId())
                        || !seen.add(f.skuId())
                        || f.spuId() == null
                        || f.spuId() <= 0
                        || f.unitPriceFen() <= 0
                        || f.availableStock() < 0
                        || f.productCode() == null
                        || f.productCode().isBlank()
                        || f.productCode().length() > 64
                        || f.name() == null
                        || f.name().isBlank()
                        || f.name().length() > 200
                        || f.factsHash() == null
                        || !f.factsHash().matches("[a-f0-9]{64}")) throw unavailable();
                result.add(
                        new CatalogPricingFacts(
                                f.skuId(),
                                f.spuId(),
                                f.productCode(),
                                f.name(),
                                f.unitPriceFen(),
                                f.availableStock(),
                                f.factsHash()));
            }
            // Missing sellable facts are preserved as absence; callers must reject missing
            // purchases.
            return result.stream()
                    .sorted(Comparator.comparingLong(CatalogPricingFacts::skuId))
                    .toList();
        } catch (RuntimeException ex) {
            throw unavailable();
        }
    }

    public void validateScope(PromotionScope scope) {
        PricingException.require(scope != null);
        if (scope.kind() == PromotionScope.Kind.ALL) return;
        var ids = ids(scope.ids(), 1000);
        boolean missing;
        try {
            var value =
                    client.validateScope(new PromotionScopeValidationBo(scope.kind().name(), ids));
            if (value == null
                    || value.missingIds() == null
                    || value.missingIds().size() > ids.size()
                    || value.missingIds().stream().anyMatch(id -> id == null || !ids.contains(id))
                    || new HashSet<>(value.missingIds()).size() != value.missingIds().size())
                throw unavailable();
            missing = !value.missingIds().isEmpty();
        } catch (RuntimeException ex) {
            throw unavailable();
        }
        PricingException.require(!missing);
    }

    private static List<Long> ids(List<Long> ids, int max) {
        PricingException.require(
                ids != null
                        && !ids.isEmpty()
                        && ids.size() <= max
                        && ids.stream().allMatch(id -> id != null && id > 0)
                        && new HashSet<>(ids).size() == ids.size());
        return ids.stream().sorted().toList();
    }

    private static PromotionCatalogUnavailableException unavailable() {
        return new PromotionCatalogUnavailableException();
    }
}
