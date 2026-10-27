package com.lemnt.mall.infrastructure.promotion;

import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionScope;
import com.lemnt.mall.infrastructure.promotion.gateway.PromotionCatalogFactsGatewayImpl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromotionCatalogFactsGatewayTest {
    private final ProductGateway products = mock(ProductGateway.class);
    private final SpuGateway spus = mock(SpuGateway.class);
    private final PromotionCatalogFactsGatewayImpl gateway = new PromotionCatalogFactsGatewayImpl(products, spus);

    @Test
    void readsLocalFactsAndPreservesMissingSellableProducts() {
        var fact = new CatalogPricingFacts(1, 11, "SKU", "name", 101, 0);
        when(products.facts(List.of(1L, 2L))).thenReturn(List.of(fact));
        var values = gateway.facts(List.of(2L, 1L));
        assertEquals(1, values.size());
        assertEquals(101, values.get(0).unitPriceFen());
        assertEquals(fact.hash(), values.get(0).factsHash());
    }

    @Test
    void databaseFailureDoesNotReturnFallbackPrices() {
        when(products.facts(anyList())).thenThrow(new IllegalStateException("database unavailable"));
        assertThrows(IllegalStateException.class, () -> gateway.facts(List.of(1L)));
    }

    @Test
    void rejectsInvalidOrDuplicateInputBeforeQuery() {
        assertThrows(IllegalArgumentException.class, () -> gateway.facts(List.of(1L, 1L)));
        assertThrows(IllegalArgumentException.class, () -> gateway.facts(List.of(0L)));
        verifyNoInteractions(products, spus);
    }

    @Test
    void allScopeSkipsLookupAndMissingSkuRejects() {
        gateway.validateScope(new PromotionScope(PromotionScope.Kind.ALL, List.of()));
        verifyNoInteractions(products, spus);
        when(products.existingSkuIds(anyList())).thenReturn(List.of());
        assertThrows(PricingException.class, () -> gateway.validateScope(
                new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(1L))));
    }

    @Test
    void spuScopeUsesLocalSpuGateway() {
        when(spus.existingSpuIds(List.of(1L))).thenReturn(List.of(1L));
        gateway.validateScope(new PromotionScope(PromotionScope.Kind.SPU_IDS, List.of(1L)));
        verifyNoInteractions(products);
        when(spus.existingSpuIds(anyList())).thenReturn(List.of());
        assertThrows(PricingException.class, () -> gateway.validateScope(
                new PromotionScope(PromotionScope.Kind.SPU_IDS, List.of(1L))));
    }
}
