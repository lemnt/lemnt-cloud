package com.lemnt.trade.adapter.web.checkout;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.executor.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CheckoutHttpContractTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void invalidCartQuantityAndMissingQuoteTokenRejectBeforeWrite() throws Exception {
        var save = mock(CartSaveCmdExe.class);
        var cart =
                new AdminCartController(
                        mock(CartListQueryExe.class), save, mock(CartDeleteCmdExe.class));
        var submit = mock(CheckoutSubmitCmdExe.class);
        var checkout =
                new AdminCheckoutController(
                        mock(CheckoutQuoteQueryExe.class),
                        submit,
                        mock(CheckoutReceiptQueryExe.class));
        var mvc =
                MockMvcBuilders.standaloneSetup(cart, checkout)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(
                        put("/trade/admin/members/1/cart/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\":0,\"selected\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/trade/admin/members/1/checkout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"requestNo\":\"R1\",\"productIds\":[\"5\"]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(save, submit);
    }

    @Test
    void normalizedStringIdsBindAndActualMoneyStringsRemainExact() throws Exception {
        var submit = mock(CheckoutSubmitCmdExe.class);
        when(submit.execute(any()))
                .thenReturn(
                        new CheckoutReceiptDTO(
                                "TR1",
                                1L,
                                "RESERVING",
                                new BigDecimal("20.00"),
                                new BigDecimal("3.00"),
                                new BigDecimal("17.00"),
                                8L,
                                List.of()));
        var controller =
                new AdminCheckoutController(
                        mock(CheckoutQuoteQueryExe.class),
                        submit,
                        mock(CheckoutReceiptQueryExe.class));
        var mvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(
                        post("/trade/admin/members/1/checkout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"requestNo\":\"R1\",\"productIds\":[\"5\"],\"couponId\":\"8\",\"quoteToken\":\""
                                                + "a".repeat(64)
                                                + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.payableAmount").value("17.00"));
        var command = org.mockito.ArgumentCaptor.forClass(CheckoutSubmitCmd.class);
        verify(submit).execute(command.capture());
        assertEquals(1L, command.getValue().memberId());
        assertEquals(List.of(5L), command.getValue().productIds());
    }

    @Test
    void couponValidityUsesExistingApplicationDateFormat() throws Exception {
        var create = mock(CouponTemplateCreateCmdExe.class);
        var controller =
                new AdminCouponController(
                        mock(CouponTemplateListQueryExe.class),
                        create,
                        mock(CouponTemplateEnableCmdExe.class),
                        mock(CouponIssueCmdExe.class),
                        mock(CouponListQueryExe.class));
        var mvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(
                        post("/trade/admin/coupon-templates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
{"code":"C1","name":"coupon","kind":"FIXED","threshold":"10.00","fixedAmount":"3.00","totalCount":100,"validFrom":"2026-10-25 00:00:00","validUntil":"2026-11-25 00:00:00"}
"""))
                .andExpect(status().isOk());
        var command =
                org.mockito.ArgumentCaptor.forClass(
                        com.lemnt.trade.application.checkout.cmd.CouponTemplateCreateCmd.class);
        verify(create).execute(command.capture());
        assertEquals(25, command.getValue().validFrom().getDayOfMonth());
    }

    @Test
    void allAdminCheckoutActionsDeclarePermissions() {
        for (var type :
                List.of(
                        AdminCartController.class,
                        AdminCouponController.class,
                        AdminCheckoutController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) continue;
                assertNotNull(method.getAnnotation(RequiresPermissions.class), method.toString());
            }
        }
    }
}
