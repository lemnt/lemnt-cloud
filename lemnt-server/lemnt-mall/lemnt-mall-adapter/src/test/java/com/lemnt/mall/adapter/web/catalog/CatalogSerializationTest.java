package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.mall.adapter.web.catalog.res.SkuRes;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogSerializationTest {
    @Test
    void actualApplicationCustomizerPreservesDecimalAndLargeIdentifierPrecision() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var value =
        new SkuRes(
                Long.MAX_VALUE,
                2L,
                "SKU1",
                "sku",
                new BigDecimal("9.25"),
                0,
                null,
                Map.of(),
                "ON_SHELF");
        var json = mapper.readTree(mapper.writeValueAsString(value));
        assertEquals(Long.toString(Long.MAX_VALUE), json.get("id").asString());
        assertEquals("9.25", json.get("price").asString());
        assertFalse(json.has("image"));
    }
}
