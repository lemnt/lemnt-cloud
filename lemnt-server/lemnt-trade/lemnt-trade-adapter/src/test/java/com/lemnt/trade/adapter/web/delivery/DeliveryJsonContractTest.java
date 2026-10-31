package com.lemnt.trade.adapter.web.delivery;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplateUpdateReq;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryChargeRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplateDetailRes;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 使用生产 Jackson 配置验证运费页面的大编号、精确数量和整数分契约。 */
class DeliveryJsonContractTest {

    @Test
    void acceptsEditorStringsAndReturnsExactDecimalStringsAndRuleIdentity() throws Exception {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var request = mapper.readValue("""
                {"name":"重量模板","chargeMode":2,"sort":0,"charges":[{
                  "id":"9223372036854775807","areaIds":[110101],
                  "startCount":"0.000001","extraCount":"1.01","startPrice":29,"extraPrice":101
                }],"frees":[]}
                """, DeliveryExpressTemplateUpdateReq.class);
        var row = request.charges().get(0);
        assertEquals(Long.MAX_VALUE, row.id());
        assertEquals(new BigDecimal("0.000001"), row.startCount());
        assertEquals(new BigDecimal("1.01"), row.extraCount());
        assertEquals(29, row.startPrice());
        assertTrue(request.frees().isEmpty());

        var response = new DeliveryExpressTemplateDetailRes(9L, request.name(), 2, 0,
                List.of(new DeliveryChargeRes(row.id(), 9L, row.areaIds(), 2,
                        row.startCount(), row.startPrice(), row.extraCount(), row.extraPrice())), List.of());
        String json = mapper.writeValueAsString(response);
        assertTrue(json.contains("\"id\":\"9223372036854775807\""));
        assertTrue(json.contains("\"startCount\":\"0.000001\""));
        assertTrue(json.contains("\"startPrice\":29"));
        assertTrue(json.contains("\"frees\":[]"));
    }
}
