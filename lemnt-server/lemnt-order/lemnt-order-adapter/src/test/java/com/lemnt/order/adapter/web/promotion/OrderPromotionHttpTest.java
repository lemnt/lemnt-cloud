package com.lemnt.order.adapter.web.promotion;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.order.adapter.app.order.AppOrderController;
import com.lemnt.order.adapter.web.fulfillment.FulfillmentOrderController;
import com.lemnt.order.application.promotion.dto.OrderPromotionDetailDTO;
import com.lemnt.order.application.promotion.executor.OrderPromotionDetailQueryExe;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 实际Controller及工程Jackson配置，校验金额字符串、分摊行和会员身份来源。 */
class OrderPromotionHttpTest {
    OrderPromotionDetailQueryExe query = mock(OrderPromotionDetailQueryExe.class);

    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    OrderPromotionDetailDTO result() {
        return new OrderPromotionDetailDTO(
                "T1",
                "COMPLETE",
                new OrderPromotionDetailDTO.Summary(
                        new BigDecimal("100.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("80.00")),
                List.of(
                        new OrderPromotionDetailDTO.Promotion(
                                "COUPON",
                                "满100减20",
                                new BigDecimal("20.00"),
                                new BigDecimal("0.00"),
                                0L,
                                0L,
                                Map.of(),
                                List.of(
                                        new OrderPromotionDetailDTO.Item(
                                                11L,
                                                new BigDecimal("20.00"),
                                                new BigDecimal("0.00"),
                                                0L,
                                                0L)))));
    }

    @Test
    void adminKeepsEnvelopeAndMoneyStrings() throws Exception {
        when(query.execute("T1")).thenReturn(result());
        var mvc =
                MockMvcBuilders.standaloneSetup(new FulfillmentOrderController(null, null, query))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/admin/orders/T1/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary.discountAmount").value("20.00"))
                .andExpect(jsonPath("$.data.promotions[0].items[0].orderItemId").value("11"));
    }

    @Test
    void memberIdentityComesFromContextNotQueryParameter() throws Exception {
        SecurityContextHolder.setUserId("7");
        try {
            when(query.execute(7L, "T1")).thenReturn(result());
            var mvc =
                    MockMvcBuilders.standaloneSetup(
                                    new AppOrderController(
                                            null, null, null, null, null, null, query))
                            .setMessageConverters(converter())
                            .build();
            mvc.perform(get("/trade/app/orders/T1/promotions").param("memberId", "8"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderNo").value("T1"));
            verify(query).execute(7L, "T1");
            verify(query, never()).execute(8L, "T1");
        } finally {
            SecurityContextHolder.remove();
        }
    }
}
