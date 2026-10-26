package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.client.bo.*;
import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogGateway;
import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromotionCatalogClientTest {
    @Test
    void providerValidatesSortsAndDoesNotIncludeStockInHash() {
        var g = mock(PromotionCatalogGateway.class);
        when(g.facts(List.of(1L, 2L)))
                .thenReturn(List.of(new CatalogPricingFacts(1, 11, "SKU1", "商品", 101, 0)));
        var client = new PromotionCatalogClientImpl(g);
        var value = client.findPricingFactsBatch(new PricingFactsBatchBo(List.of(2L, 1L))).get(0);
        assertEquals("MALL_PRICING_V1", value.factsProtocol());
        assertEquals(101, value.unitPriceFen());
        assertEquals(0, value.availableStock());
        assertEquals(
                new CatalogPricingFacts(1, 11, "SKU1", "商品", 101, 9).hash(), value.factsHash());
        assertNotEquals(
                new CatalogPricingFacts(1, 12, "SKU1", "商品", 101, 9).hash(), value.factsHash());
    }

    @Test
    void providerRejectsNullDuplicatesBoundsAndUnsupportedScope() {
        var g = mock(PromotionCatalogGateway.class);
        var c = new PromotionCatalogClientImpl(g);
        assertThrows(IllegalArgumentException.class, () -> c.findPricingFactsBatch(null));
        for (var ids :
                List.of(
                        List.<Long>of(),
                        List.of(1L, 1L),
                        List.of(-1L),
                        Collections.nCopies(101, 1L)))
            assertThrows(
                    IllegalArgumentException.class,
                    () -> c.findPricingFactsBatch(new PricingFactsBatchBo(ids)));
        assertThrows(
                IllegalArgumentException.class,
                () -> c.validateScope(new PromotionScopeValidationBo("ALL", List.of(1L))));
        verifyNoInteractions(g);
    }

    @Test
    void scopeExistenceIsBoundedIndependentOfAvailability() {
        var g = mock(PromotionCatalogGateway.class);
        when(g.existingSpuIds(List.of(1L, 2L))).thenReturn(List.of(1L));
        var c = new PromotionCatalogClientImpl(g);
        assertEquals(
                List.of(2L),
                c.validateScope(new PromotionScopeValidationBo("SPU_IDS", List.of(2L, 1L)))
                        .missingIds());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        c.validateScope(
                                new PromotionScopeValidationBo(
                                        "SKU_IDS", Collections.nCopies(1001, 1L))));
    }
}
