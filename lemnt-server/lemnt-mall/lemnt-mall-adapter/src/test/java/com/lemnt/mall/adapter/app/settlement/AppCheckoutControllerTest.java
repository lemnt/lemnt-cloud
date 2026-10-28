package com.lemnt.mall.adapter.app.settlement;

import com.lemnt.mall.adapter.app.cart.AppCartController;
import com.lemnt.mall.adapter.app.coupon.AppCouponController;
import com.lemnt.mall.application.cart.executor.CartDeleteCmdExe;
import com.lemnt.mall.application.cart.executor.CartListQueryExe;
import com.lemnt.mall.application.cart.executor.CartSaveCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponListQueryExe;
import com.lemnt.mall.application.settlement.cmd.SettlementAbandonCmd;
import com.lemnt.mall.application.settlement.cmd.SettlementSubmitCmd;
import com.lemnt.mall.application.settlement.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.settlement.executor.SettlementAbandonCmdExe;
import com.lemnt.mall.application.settlement.executor.SettlementCreateCmdExe;
import com.lemnt.mall.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.mall.application.settlement.executor.SettlementRequestDetailQueryExe;
import com.lemnt.mall.application.settlement.executor.SettlementSubmitCmdExe;
import com.lemnt.mall.application.settlement.qry.SettlementRequestDetailQry;

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
    void quoteForwardsOwnedAddressAndPointChoiceAndRejectsMissingAddress() throws Exception {
        var create = mock(SettlementCreateCmdExe.class);
        var quote = new com.lemnt.mall.application.settlement.dto.SettlementQuoteDTO(List.of(),
                new java.math.BigDecimal("20.00"), new java.math.BigDecimal("0.00"),
                new java.math.BigDecimal("26.50"), null, "digest", 0L, 53L,
                new com.lemnt.mall.application.settlement.dto.SettlementDeliveryDTO(123L, 1, "收件人",
                        "13800000000", 110101L, "地址", new java.math.BigDecimal("6.50")));
        when(create.execute(any())).thenReturn(new com.lemnt.mall.application.settlement.dto.SettlementDTO(
                "12345678-1234-1234-1234-123456789abc", java.time.Instant.now().plusSeconds(900), quote));
        var mvc = MockMvcBuilders.standaloneSetup(new AppSettlementController(create,
                mock(SettlementSubmitCmdExe.class), mock(SettlementAbandonCmdExe.class)))
                .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
                .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
                .build();
        mvc.perform(post("/mall/app/checkout/quote").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\":[5],\"addressId\":123,\"pointStatus\":true,\"memberId\":999}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.delivery.addressId").value(123));
        verify(create).execute(new com.lemnt.mall.application.settlement.cmd.SettlementCreateCmd(
                7L, List.of(5L), null, true, 123L));
        mvc.perform(post("/mall/app/checkout/quote").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\":[5]}"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(create);
    }

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
                new AppSettlementController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(SettlementReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
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
        var receipt = mock(SettlementReceiptQueryExe.class);
        var controller = new AppOrderController(receipt, mock(SettlementRequestDetailQueryExe.class));
        assertNull(controller.order("FOREIGN").getData());
        verify(receipt).execute(7L, "FOREIGN");
    }

    @Test
    void cachedSubmitRejectsLegacyPayloadAndPassesOnlySettlementIdentity() throws Exception {
        var cached = mock(SettlementSubmitCmdExe.class);
        var controller =
        new Object[] {
                new AppCartController(mock(CartListQueryExe.class), mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppSettlementController(mock(SettlementCreateCmdExe.class), cached, mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(SettlementReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
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
        verifyNoInteractions(cached);
        mvc.perform(
                post("/mall/app/checkout")
                .header("Authorization", "Bearer member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                "{\"requestNo\":\"R1\",\"settlementId\":\"12345678-1234-1234-1234-123456789abc\",\"memberId\":999,\"amount\":0.01}"))
            .andExpect(status().isOk());
        verify(cached).execute(new SettlementSubmitCmd(7L, "12345678-1234-1234-1234-123456789abc", "R1"));
    }

    @Test
    void requestRecoveryUsesAuthenticatedMemberNotCallerIdentity() throws Exception {
        var cached = mock(SettlementRequestDetailQueryExe.class);
        var controller =
        new Object[] {
                new AppCartController(mock(CartListQueryExe.class), mock(CartSaveCmdExe.class), mock(CartDeleteCmdExe.class)),
                new AppCouponController(mock(CouponListQueryExe.class)),
                new AppSettlementController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), mock(SettlementAbandonCmdExe.class)),
                new AppOrderController(mock(SettlementReceiptQueryExe.class), cached)
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
                new AppSettlementController(mock(SettlementCreateCmdExe.class), mock(SettlementSubmitCmdExe.class), cached),
                new AppOrderController(mock(SettlementReceiptQueryExe.class), mock(SettlementRequestDetailQueryExe.class))
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
