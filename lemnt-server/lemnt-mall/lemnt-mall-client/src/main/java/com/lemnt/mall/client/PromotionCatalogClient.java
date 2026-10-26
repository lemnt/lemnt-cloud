package com.lemnt.mall.client;

import com.lemnt.mall.client.bo.*;
import com.lemnt.mall.client.dto.*;

/** Versioned authoritative facts, independent of the legacy catalog interface. */
public interface PromotionCatalogClient {
    java.util.List<ProductPricingFactsDTO> findPricingFactsBatch(PricingFactsBatchBo input);

    PromotionScopeValidationDTO validateScope(PromotionScopeValidationBo input);
}
