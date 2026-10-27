package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.client.PromotionCatalogClient;
import com.lemnt.mall.client.bo.PricingFactsBatchBo;
import com.lemnt.mall.client.bo.PromotionScopeValidationBo;
import com.lemnt.mall.client.dto.ProductPricingFactsDTO;
import com.lemnt.mall.client.dto.PromotionScopeValidationDTO;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogRules;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 营销目录客户端实现。
 */
@DubboService
@RequiredArgsConstructor
public class PromotionCatalogClientImpl implements PromotionCatalogClient {
    private final ProductGateway productGateway;
    private final SpuGateway spuGateway;

    /**
     * 查找计价事实批量。
     */
    public List<ProductPricingFactsDTO> findPricingFactsBatch(PricingFactsBatchBo input) {
        if (input == null) {
            throw new IllegalArgumentException("Missing input");
        }
        var ids = PromotionCatalogRules.ids(input.skuIds(), 100);
        return productGateway.facts(ids).stream()
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

    /**
     * 校验范围。
     */
    public PromotionScopeValidationDTO validateScope(PromotionScopeValidationBo input) {
        if (input == null
                || !Set.of("SKU_IDS", "SPU_IDS")
                .contains(input.scopeType() == null ? "" : input.scopeType())) {
            throw new IllegalArgumentException("Unsupported scope");
        }
        var ids = PromotionCatalogRules.ids(input.ids(), 1000);
        var existing =
                new HashSet<>(
                "SKU_IDS".equals(input.scopeType())
                ? productGateway.existingSkuIds(ids)
                : spuGateway.existingSpuIds(ids));
        return new PromotionScopeValidationDTO(
                ids.stream().filter(id -> !existing.contains(id)).toList());
    }
}
