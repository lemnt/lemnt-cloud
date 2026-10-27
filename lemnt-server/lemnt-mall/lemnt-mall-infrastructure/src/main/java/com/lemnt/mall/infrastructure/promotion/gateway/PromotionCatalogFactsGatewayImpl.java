package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogFactsGateway;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogRules;
import com.lemnt.mall.domain.promotion.model.PromotionPricingFacts;
import com.lemnt.mall.domain.promotion.model.PromotionScope;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;

/** 促销引擎从商城本地目录读取计价事实及范围。 */
@Component
@RequiredArgsConstructor
public class PromotionCatalogFactsGatewayImpl implements PromotionCatalogFactsGateway {
    private final ProductGateway products;
    private final SpuGateway spus;

    @Override
    public List<PromotionPricingFacts> facts(List<Long> skuIds) {
        var ids = PromotionCatalogRules.ids(skuIds, 100);
        return products.facts(ids).stream().map(value -> new PromotionPricingFacts(value.skuId(),
                value.spuId(), value.productCode(), value.name(), value.unitPriceFen(),
                value.availableStock(), value.hash())).toList();
    }

    @Override
    public void validateScope(PromotionScope scope) {
        PricingException.require(scope != null);
        if (scope.kind() == PromotionScope.Kind.ALL) {
            return;
        }
        var ids = PromotionCatalogRules.ids(scope.ids(), 1000);
        var existing = new HashSet<>(scope.kind() == PromotionScope.Kind.SKU_IDS
                ? products.existingSkuIds(ids) : spus.existingSpuIds(ids));
        PricingException.require(existing.containsAll(ids));
    }
}
