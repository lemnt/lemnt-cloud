package com.lemnt.trade.infrastructure.promotion;

import com.lemnt.trade.infrastructure.promotion.gateway.PromotionCatalogFactsGatewayImpl;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.mall.client.PromotionCatalogClient;
import com.lemnt.mall.client.dto.*;
import com.lemnt.mall.client.bo.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromotionCatalogFactsGatewayTest {
    PromotionCatalogClient client;
    PromotionCatalogFactsGatewayImpl gateway;

    @BeforeEach
    void setup() {
        client = mock(PromotionCatalogClient.class);
        gateway = new PromotionCatalogFactsGatewayImpl();
        ReflectionTestUtils.setField(gateway, "client", client);
    }

    ProductPricingFactsDTO fact(
            String protocol, Long sku, Long spu, long amount, int stock, String hash) {
        return new ProductPricingFactsDTO(protocol, sku, spu, "SKU", "name", amount, stock, hash);
    }

    @Test
    void convertsRealProtocolAndPreservesMissingSellableFacts() {
        when(client.findPricingFactsBatch(any()))
                .thenReturn(List.of(fact("MALL_PRICING_V1", 1L, 11L, 101, 0, "a".repeat(64))));
        var values = gateway.facts(List.of(2L, 1L));
        assertEquals(1, values.size());
        assertEquals(11, values.get(0).spuId());
        assertEquals(101, values.get(0).unitPriceFen());
        assertEquals(0, values.get(0).availableStock());
    }

    @Test
    void missingProviderAndTimeoutNeverFallback() {
        ReflectionTestUtils.setField(gateway, "client", null);
        assertThrows(PromotionCatalogUnavailableException.class, () -> gateway.facts(List.of(1L)));
        ReflectionTestUtils.setField(gateway, "client", client);
        when(client.findPricingFactsBatch(any()))
                .thenThrow(new IllegalStateException("RPC timeout"));
        assertThrows(PromotionCatalogUnavailableException.class, () -> gateway.facts(List.of(1L)));
    }

    @Test
    void malformedProtocolFactsAndDuplicateOrUnrequestedRowsReject() {
        var invalid =
                Arrays.asList(
                        null,
                        fact("OLD", 1L, 11L, 101, 0, "a".repeat(64)),
                        fact("MALL_PRICING_V1", 2L, 11L, 101, 0, "a".repeat(64)),
                        fact("MALL_PRICING_V1", 1L, null, 101, 0, "a".repeat(64)),
                        fact("MALL_PRICING_V1", 1L, 11L, 0, 0, "a".repeat(64)),
                        fact("MALL_PRICING_V1", 1L, 11L, 101, -1, "a".repeat(64)),
                        fact("MALL_PRICING_V1", 1L, 11L, 101, 0, "bad"));
        for (var f : invalid) {
            when(client.findPricingFactsBatch(any())).thenReturn(Arrays.asList(f));
            assertThrows(
                    PromotionCatalogUnavailableException.class, () -> gateway.facts(List.of(1L)));
        }
        var valid = fact("MALL_PRICING_V1", 1L, 11L, 101, 0, "a".repeat(64));
        when(client.findPricingFactsBatch(any())).thenReturn(List.of(valid, valid));
        assertThrows(PromotionCatalogUnavailableException.class, () -> gateway.facts(List.of(1L)));
    }

    @Test
    void allScopeSkipsRpcAndMissingTargetsAreInvalidInput() {
        gateway.validateScope(new PromotionScope(PromotionScope.Kind.ALL, List.of()));
        verifyNoInteractions(client);
        when(client.validateScope(any())).thenReturn(new PromotionScopeValidationDTO(List.of(1L)));
        assertThrows(
                PricingException.class,
                () ->
                        gateway.validateScope(
                                new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(1L))));
    }

    @Test
    void malformedScopeResultsOrMissingMethodFailClosed() {
        var scope = new PromotionScope(PromotionScope.Kind.SPU_IDS, List.of(1L));
        for (var invalid :
                Arrays.asList(
                        null,
                        new PromotionScopeValidationDTO(null),
                        new PromotionScopeValidationDTO(List.of(2L)),
                        new PromotionScopeValidationDTO(List.of(1L, 1L)))) {
            when(client.validateScope(any())).thenReturn(invalid);
            assertThrows(
                    PromotionCatalogUnavailableException.class, () -> gateway.validateScope(scope));
        }
        when(client.validateScope(any())).thenThrow(new IllegalStateException("method missing"));
        assertThrows(
                PromotionCatalogUnavailableException.class, () -> gateway.validateScope(scope));
    }
}
