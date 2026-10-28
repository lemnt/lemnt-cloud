package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.adapter.web.order.res.TradeOrderItemRes;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 真实HTTP序列化配置输出源规格列表，编号不能丢失精度。 */
class PropertySnapshotSerializationTest {
    @Test
    void structuredPropertyListKeepsIdentifiersAndNames() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var value = new TradeOrderItemRes(1L, "SKU", "商品", BigDecimal.TEN, 1, BigDecimal.TEN,
                2L, "SPU", List.of(new TradeOrderItemRes.Property(Long.MAX_VALUE, "颜色", Long.MAX_VALUE - 1, "黑色")), null);
        var json = mapper.readTree(mapper.writeValueAsString(value));
        assertTrue(json.get("properties").isArray());
        assertEquals("9223372036854775807", json.get("properties").get(0).get("propertyId").asString());
        assertEquals("9223372036854775806", json.get("properties").get(0).get("valueId").asString());
        assertEquals("颜色", json.get("properties").get(0).get("propertyName").asString());
        assertEquals("黑色", json.get("properties").get(0).get("valueName").asString());
    }
}
