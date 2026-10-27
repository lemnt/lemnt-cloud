package com.lemnt.mall.client;

import com.lemnt.mall.client.bo.PricingFactsBatchBo;
import com.lemnt.mall.client.bo.PromotionScopeValidationBo;
import com.lemnt.mall.client.dto.ProductPricingFactsDTO;
import com.lemnt.mall.client.dto.PromotionScopeValidationDTO;

import java.util.List;

/**
 * Versioned authoritative facts, independent of the legacy catalog interface.
 */
public interface PromotionCatalogClient {
    /**
     * 查找计价事实批量。
     */
    List<ProductPricingFactsDTO> findPricingFactsBatch(PricingFactsBatchBo input);

    /**
     * 校验范围。
     */
    PromotionScopeValidationDTO validateScope(PromotionScopeValidationBo input);
}
