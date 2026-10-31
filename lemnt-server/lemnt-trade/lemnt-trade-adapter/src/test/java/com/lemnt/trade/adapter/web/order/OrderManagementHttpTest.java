package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.application.order.dto.OrderStatisticsDTO;
import com.lemnt.trade.application.order.executor.OrderStatisticsQueryExe;
import com.lemnt.trade.application.order.executor.OrderRemarkDetailQueryExe;
import com.lemnt.trade.application.order.executor.OrderRemarkUpdateCmdExe;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderManagementHttpTest {
    @Test
    void emptyStatisticsKeepExactTwoDecimalMoneyAndNoFabricatedRevenue() throws Exception {
        var query = mock(OrderStatisticsQueryExe.class);
        when(query.execute(null)).thenReturn(new OrderStatisticsDTO(0, BigDecimal.ZERO, 0, BigDecimal.ZERO));
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mvc = MockMvcBuilders.standaloneSetup(new OrderManagementController(query,
                        mock(OrderRemarkDetailQueryExe.class), mock(OrderRemarkUpdateCmdExe.class)))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build())).build();
        mvc.perform(get("/trade/admin/orders/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderCount").value(0))
                .andExpect(jsonPath("$.data.orderPayPrice").value("0.00"))
                .andExpect(jsonPath("$.data.afterSalePrice").value("0.00"));
        verify(query).execute(null);
    }
}
