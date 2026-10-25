package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.application.fulfillment.executor.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FulfillmentHttpContractTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void missingShippingDataAndInvalidPagingRejectBeforeExecutor() throws Exception {
        var page = mock(OrderPageQueryExe.class);
        var ship = mock(ShipmentCreateCmdExe.class);
        var controller =
                new AdminFulfillmentController(
                        page,
                        mock(OrderDetailQueryExe.class),
                        ship,
                        mock(OrderReceiveCmdExe.class),
                        mock(RefundRequestCmdExe.class));
        var mvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(
                        post("/trade/admin/orders/TR1/shipment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"requestNo\":\"S1\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/trade/admin/orders").param("pageSize", "101"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(page, ship);
    }

    @Test
    void refundBoundaryAcceptsOnlyRequestIdentityAndReasonAndReturnsExactMoney() throws Exception {
        var refund = mock(RefundRequestCmdExe.class);
        when(refund.execute(any()))
                .thenReturn(
                        new RefundRequestDTO(
                                "RF1",
                                "TR1",
                                "R1",
                                1L,
                                "PAY1",
                                new BigDecimal("17.00"),
                                "原因",
                                "REQUESTED",
                                null,
                                LocalDateTime.of(2026, 10, 25, 10, 0),
                                null));
        var controller =
                new AdminFulfillmentController(
                        mock(OrderPageQueryExe.class),
                        mock(OrderDetailQueryExe.class),
                        mock(ShipmentCreateCmdExe.class),
                        mock(OrderReceiveCmdExe.class),
                        refund);
        var mvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(
                        post("/trade/admin/orders/TR1/refund-requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"requestNo\":\"R1\",\"reason\":\"原因\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value("17.00"))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        verify(refund).execute(new RefundRequestCmd("TR1", "R1", "原因"));
    }

    @Test
    void orderListHasNoRecipientFieldsAndAllHttpActionsDeclarePermissions() throws Exception {
        var page = mock(OrderPageQueryExe.class);
        when(page.execute(any()))
                .thenReturn(
                        new OrderPageDTO(
                                List.of(
                                        new OrderSummaryDTO(
                                                "TR1",
                                                1L,
                                                new BigDecimal("17.00"),
                                                "SHIPPED",
                                                LocalDateTime.now(),
                                                true,
                                                false,
                                                "SF",
                                                "T1",
                                                LocalDateTime.now(),
                                                null)),
                                1));
        var controller =
                new AdminFulfillmentController(
                        page,
                        mock(OrderDetailQueryExe.class),
                        mock(ShipmentCreateCmdExe.class),
                        mock(OrderReceiveCmdExe.class),
                        mock(RefundRequestCmdExe.class));
        var mvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].totalAmount").value("17.00"))
                .andExpect(jsonPath("$.data.records[0].recipientName").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].phone").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].address").doesNotExist());
        for (var type :
                List.of(AdminFulfillmentController.class, AdminRefundRequestController.class))
            for (var method : type.getDeclaredMethods())
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                    assertNotNull(
                            method.getAnnotation(RequiresPermissions.class), method.toString());
    }
}
