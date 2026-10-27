package com.lemnt.mall.adapter.app.checkout;

import com.lemnt.mall.adapter.app.checkout.AppCartController;
import com.lemnt.mall.adapter.app.checkout.AppCheckoutController;
import com.lemnt.mall.adapter.app.checkout.AppCouponController;
import com.lemnt.mall.adapter.app.checkout.AppOrderController;
import com.lemnt.mall.application.checkout.cmd.SettlementAbandonCmd;
import com.lemnt.mall.application.checkout.cmd.SettlementSubmitCmd;
import com.lemnt.mall.application.checkout.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.checkout.executor.CartDeleteCmdExe;
import com.lemnt.mall.application.checkout.executor.CartListQueryExe;
import com.lemnt.mall.application.checkout.executor.CartSaveCmdExe;
import com.lemnt.mall.application.checkout.executor.CheckoutReceiptQueryExe;
import com.lemnt.mall.application.checkout.executor.CheckoutSubmitCmdExe;
import com.lemnt.mall.application.checkout.executor.CouponListQueryExe;
import com.lemnt.mall.application.checkout.executor.SettlementAbandonCmdExe;
import com.lemnt.mall.application.checkout.executor.SettlementCreateCmdExe;
import com.lemnt.mall.application.checkout.executor.SettlementRequestDetailQueryExe;
import com.lemnt.mall.application.checkout.executor.SettlementSubmitCmdExe;
import com.lemnt.mall.application.checkout.qry.SettlementRequestDetailQry;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppCheckoutControllerTest {
    @org.junit.jupiter.api.BeforeEach
    void identity() { com.lemnt.common.security.context.SecurityContextHolder.setUserId("7"); }

    @org.junit.jupiter.api.AfterEach
    void clearIdentity() { com.lemnt.common.security.context.SecurityContextHolder.remove(); }

    @Test
    void gatewayIdentityWorksWithoutBackendTokenAuthentication() throws Exception {
        var list = mock(CartListQueryExe.class);
        when(list.execute(7L)).thenReturn(List.of());
        MockMvcBuilders.standaloneSetup(new AppCartController(list, mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)))
                .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
                .build().perform(get("/mall/app/cart")
                        .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7")
                        .header("memberId", "999"))
                .andExpect(status().isOk());
        verify(list).execute(7L);
        verify(list, never()).execute(999L);
    }

    @Test
    void cartUsesSessionMemberInsteadOfQueryOrHeaderIdentity() throws Exception {
        var list = mock(CartListQueryExe.class);
        when(list.execute(7L)).thenReturn(List.of());
        var controller =
        new Object[] {
                new AppCartController(list, mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppCheckoutController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(CheckoutReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
        };
        MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build()
            .perform(
                get("/mall/app/cart")
                .header("Authorization", "Bearer member")
                .header("memberId", "999")
                .param("memberId", "999"))
            .andExpect(status().isOk());
        verify(list).execute(7L);
        verify(list, never()).execute(999L);
    }

    @Test
    void orderQueryPassesSessionIdentityToOwnershipQuery() {
        var receipt = mock(CheckoutReceiptQueryExe.class);
        var controller = new AppOrderController(receipt, mock(SettlementRequestDetailQueryExe.class));
        assertNull(controller.order("FOREIGN").getData());
        verify(receipt).execute(7L, "FOREIGN");
    }

    @Test
    void cachedSubmitRejectsLegacyPayloadAndPassesOnlySettlementIdentity() throws Exception {
        var cached = mock(SettlementSubmitCmdExe.class);
        var legacy = mock(CheckoutSubmitCmdExe.class);
        var controller =
        new Object[] {
                new AppCartController(mock(CartListQueryExe.class), mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppCheckoutController(mock(SettlementCreateCmdExe.class), cached, mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(CheckoutReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
        };
        var mvc = MockMvcBuilders.standaloneSetup(controller).addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build();
        mvc.perform(
                post("/mall/app/checkout")
                .header("Authorization", "Bearer member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\"R1\",\"productIds\":[\"5\"],\"quoteToken\":\"old\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(cached, legacy);
        mvc.perform(
                post("/mall/app/checkout")
                .header("Authorization", "Bearer member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\"R1\",\"settlementId\":\"12345678-1234-1234-1234-123456789abc\",\"memberId\":999,\"amount\":0.01}"))
            .andExpect(status().isOk());
        verify(cached).execute(new SettlementSubmitCmd(7L, "12345678-1234-1234-1234-123456789abc", "R1"));
        verifyNoInteractions(legacy);
    }

    @Test
    void requestRecoveryUsesAuthenticatedMemberNotCallerIdentity() throws Exception {
        var cached = mock(SettlementRequestDetailQueryExe.class);
        var controller =
        new Object[] {
                new AppCartController(mock(CartListQueryExe.class), mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppCheckoutController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(CheckoutReceiptQueryExe.class), cached)
        };
        MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build()
            .perform(
                get("/mall/app/checkout/requests/R1")
                .header("Authorization", "Bearer member")
                .header("memberId", "999")
                .param("memberId", "999"))
            .andExpect(status().isOk());
        verify(cached).execute(new SettlementRequestDetailQry(7L, "R1"));
        verify(cached, never()).execute(new SettlementRequestDetailQry(999L, "R1"));
    }

    @Test
    void abandonUsesSessionMemberAndOnlySettlementIdentity() throws Exception {
        var cached = mock(SettlementAbandonCmdExe.class);
        when(cached.execute(new SettlementAbandonCmd(7L, "12345678-1234-1234-1234-123456789abc", "R1")))
            .thenReturn(
                new SettlementAbandonDTO(
                true, null));
        var controller =
        new Object[] {
                new AppCartController(mock(CartListQueryExe.class), mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppCheckoutController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), cached),
                new AppOrderController(mock(CheckoutReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
        };
        MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build()
            .perform(
                post("/mall/app/checkout/abandon")
                .header("Authorization", "Bearer member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"settlementId\":\"12345678-1234-1234-1234-123456789abc\",\"requestNo\":\"R1\",\"memberId\":999}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.abandoned").value(true));
        verify(cached).execute(new SettlementAbandonCmd(7L, "12345678-1234-1234-1234-123456789abc", "R1"));
    }
}
