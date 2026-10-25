package com.lemnt.mall.domain.catalog;

import com.lemnt.mall.domain.catalog.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogRulesTest {
    CatalogSku sku(String code, Map<String, String> properties) {
        return new CatalogSku(
                null, null, code, "name", new BigDecimal("12.50"), 5, null, properties, null);
    }

    CatalogSpu spu(CatalogSku... skus) {
        return new CatalogSpu(null, " SP1 ", " goods ", 1L, 2L, null, null, null, List.of(skus));
    }

    @Test
    void canonicalVariantIgnoresIterationOrderAndTrimsValues() {
        var first = new LinkedHashMap<String, String>();
        first.put("颜色", " 红色 ");
        first.put("尺寸", "M");
        var second = new LinkedHashMap<String, String>();
        second.put("尺寸", "M");
        second.put(" 颜色 ", "红色");
        assertEquals(CatalogRules.variantKey(first), CatalogRules.variantKey(second));
        assertNotEquals(
                CatalogRules.variantKey(Map.of("ab", "c")),
                CatalogRules.variantKey(Map.of("a", "bc")));
    }

    @Test
    void duplicateVariantOrCodeAndInconsistentPropertyNamesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CatalogRules.newSpu(spu(sku("A", Map.of()), sku("B", Map.of()))));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CatalogRules.newSpu(
                                spu(sku("A", Map.of("颜色", "红")), sku(" A ", Map.of("颜色", "蓝")))));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CatalogRules.newSpu(
                                spu(sku("A", Map.of("颜色", "红")), sku("B", Map.of("尺寸", "M")))));
    }

    @Test
    void priceNeverSilentlyRoundsOrOverflowsStoragePrecision() {
        assertEquals(new BigDecimal("1.20"), CatalogRules.price(new BigDecimal("1.2")));
        for (String value : List.of("0", "-1", "1.001", "100000000000000000.00")) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> CatalogRules.price(new BigDecimal(value)));
        }
    }

    @Test
    void newSpuStartsOffShelfAndHasNormalizedImmutableVariants() {
        var result = CatalogRules.newSpu(spu(sku(" SKU1 ", Map.of("颜色", "红"))));
        assertEquals("SP1", result.code());
        assertEquals("OFF_SHELF", result.status());
        assertEquals("SKU1", result.skus().get(0).code());
        assertThrows(UnsupportedOperationException.class, () -> result.skus().clear());
    }

    @Test
    void invalidAndNormalizedDuplicatePropertiesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CatalogRules.properties(Map.of("a", "1", " a ", "2")));
        assertThrows(
                IllegalArgumentException.class, () -> CatalogRules.properties(Map.of("a", " ")));
        assertThrows(IllegalArgumentException.class, () -> CatalogRules.newSpu(spu()));
    }
}
