package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.application.order.dto.OrderStatisticsDTO;
import com.lemnt.trade.application.order.executor.OrderAdjustAddressCmdExe;
import com.lemnt.trade.application.order.executor.OrderAdjustPriceCmdExe;
import com.lemnt.trade.application.order.executor.OrderRemarkDetailQueryExe;
import com.lemnt.trade.application.order.executor.OrderRemarkUpdateCmdExe;
import com.lemnt.trade.application.order.executor.OrderStatisticsQueryExe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderManagementHttpTest {
    @BeforeEach
    void identity() {
        com.lemnt.common.security.context.SecurityContextHolder.setUserId("9");
    }

    @AfterEach
    void clearIdentity() {
        com.lemnt.common.security.context.SecurityContextHolder.remove();
    }

    @Test
    void emptyStatisticsKeepExactTwoDecimalMoneyAndNoFabricatedRevenue() throws Exception {
        var query = mock(OrderStatisticsQueryExe.class);
        when(query.execute(null))
                .thenReturn(new OrderStatisticsDTO(0, BigDecimal.ZERO, 0, BigDecimal.ZERO));
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new OrderManagementController(
                                        query,
                                        mock(OrderRemarkDetailQueryExe.class),
                                        mock(OrderRemarkUpdateCmdExe.class),
                                        mock(OrderAdjustPriceCmdExe.class),
                                        mock(OrderAdjustAddressCmdExe.class)))
                        .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
                        .build();
        mvc.perform(get("/trade/admin/orders/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderCount").value(0))
                .andExpect(jsonPath("$.data.orderPayPrice").value("0.00"))
                .andExpect(jsonPath("$.data.afterSalePrice").value("0.00"));
        verify(query).execute(null);
    }

    @Test
    void existingPaymentReturnsConflictProblemDetails() throws Exception {
        var price = mock(OrderAdjustPriceCmdExe.class);
        when(price.execute(any()))
                .thenThrow(
                        new com.lemnt.common.core.exception.ServiceException(
                                "订单已生成支付单，不能调价，请取消订单后重新下单", 409));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new OrderManagementController(
                                        mock(OrderStatisticsQueryExe.class),
                                        mock(OrderRemarkDetailQueryExe.class),
                                        mock(OrderRemarkUpdateCmdExe.class),
                                        price,
                                        mock(OrderAdjustAddressCmdExe.class)))
                        .setControllerAdvice(
                                new com.lemnt.common.security.handler.GlobalExceptionHandler())
                        .build();
        mvc.perform(
                        post("/trade/admin/orders/TR1/price-adjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"totalAmount\":\"8.00\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.detail").value("订单已生成支付单，不能调价，请取消订单后重新下单"));
    }

    @Test
    void adjustsPriceAndAddressWithVerifiedOperator() throws Exception {
        var price = mock(OrderAdjustPriceCmdExe.class);
        var address = mock(OrderAdjustAddressCmdExe.class);
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new OrderManagementController(
                                        mock(OrderStatisticsQueryExe.class),
                                        mock(OrderRemarkDetailQueryExe.class),
                                        mock(OrderRemarkUpdateCmdExe.class),
                                        price,
                                        address))
                        .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
                        .build();

        mvc.perform(
                        post("/trade/admin/orders/TR1/price-adjustment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"totalAmount\":\"88.00\"}"))
                .andExpect(status().isOk());
        mvc.perform(
                        put("/trade/admin/orders/TR1/address")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"receiverName\":\"李四\",\"receiverMobile\":\"13900000000\","
                                                + "\"receiverAreaId\":110102,\"receiverDetailAddress\":\"新地址\"}"))
                .andExpect(status().isOk());

        verify(price)
                .execute(
                        new com.lemnt.trade.application.order.cmd.OrderAdjustPriceCmd(
                                "TR1", new BigDecimal("88.00"), 9L));
        verify(address)
                .execute(
                        new com.lemnt.trade.application.order.cmd.OrderAdjustAddressCmd(
                                "TR1", "李四", "13900000000", 110102L, "新地址", 9L));
    }
}
