package com.lemnt.product.adapter.web.product;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.product.adapter.web.product.req.ProductSkuCreateReq;
import com.lemnt.product.adapter.web.product.res.ProductSkuRes;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import jakarta.validation.Validation;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogSerializationTest {
    @Test
    void actualApplicationCustomizerPreservesDecimalAndLargeIdentifierPrecision() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var value =
            new ProductSkuRes(
                Long.MAX_VALUE,
                2L,
                "SKU1",
                "sku",
                new BigDecimal("9.25"),
                0,
                null,
                List.of(),
                "ON_SHELF");
        var json = mapper.readTree(mapper.writeValueAsString(value));
        assertEquals(Long.toString(Long.MAX_VALUE), json.get("id").asString());
        assertEquals("9.25", json.get("price").asString());
        assertFalse(json.has("image"));
    }

    /**
     * 真实Jackson配置保留编号精度，Bean Validation验证独立规格对象。
     */
    @Test
    void dictionaryPropertyListBindsLongIdsAndRejectsInvalidIdentifiers() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var request =
            mapper.readValue(
                """
                    {"code":"SKU","name":"商品","price":"10.00","stock":1,
                     "specs":[{"specId":"9223372036854775807","specValueId":"9223372036854775806"}]}
                    """,
                ProductSkuCreateReq.class);
        assertEquals(Long.MAX_VALUE - 1, request.specs().get(0).specValueId());
        var value =
            new ProductSkuRes(
                1L,
                2L,
                "SKU",
                "商品",
                BigDecimal.TEN,
                1,
                null,
                List.of(
                    new ProductSkuRes.Spec(
                        Long.MAX_VALUE, "颜色", Long.MAX_VALUE - 1, "黑色")),
                "ON_SHELF",
                null,
                null);
        var json = mapper.readTree(mapper.writeValueAsString(value));
        assertEquals("9223372036854775806", json.get("specs").get(0).get("specValueId").asString());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
            var invalid =
                mapper.readValue(
                    """
                        {"code":"SKU","name":"商品","price":"10.00","stock":1,"specs":[{"specId":0,"specValueId":-1}]}
                        """,
                    ProductSkuCreateReq.class);
            assertFalse(factory.getValidator().validate(invalid).isEmpty());
        }
    }
}
