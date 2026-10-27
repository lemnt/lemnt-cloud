package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.client.bo.PricingFactsBatchBo;
import com.lemnt.mall.client.bo.PromotionScopeValidationBo;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromotionCatalogClientTest {
    @Test
    void providerValidatesSortsAndDoesNotIncludeStockInHash() {
        ProductGateway productGateway = mock(ProductGateway.class);
    SpuGateway spuGateway = mock(SpuGateway.class);
        when(productGateway.facts(List.of(1L, 2L)))
            .thenReturn(List.of(new CatalogPricingFacts(1, 11, "SKU1", "商品", 101, 0)));
        var client = new PromotionCatalogClientImpl(productGateway, spuGateway);
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
        ProductGateway productGateway = mock(ProductGateway.class);
    SpuGateway spuGateway = mock(SpuGateway.class);
        var c = new PromotionCatalogClientImpl(productGateway, spuGateway);
        assertThrows(IllegalArgumentException.class, () -> c.findPricingFactsBatch(null));
        for (var ids :
                List.of(
                List.<Long>of(),
                List.of(1L, 1L),
                List.of(-1L),
                Collections.nCopies(101, 1L))) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> c.findPricingFactsBatch(new PricingFactsBatchBo(ids)));
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> c.validateScope(new PromotionScopeValidationBo("ALL", List.of(1L))));
        verifyNoInteractions(productGateway, spuGateway);
    }

    @Test
    void scopeExistenceIsBoundedIndependentOfAvailability() {
        ProductGateway productGateway = mock(ProductGateway.class);
    SpuGateway spuGateway = mock(SpuGateway.class);
        when(spuGateway.existingSpuIds(List.of(1L, 2L))).thenReturn(List.of(1L));
        var c = new PromotionCatalogClientImpl(productGateway, spuGateway);
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
