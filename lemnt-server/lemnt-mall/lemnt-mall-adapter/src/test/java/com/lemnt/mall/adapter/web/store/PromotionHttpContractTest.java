package com.lemnt.mall.adapter.web.store;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.app.store.AppCartAdditionController;
import com.lemnt.mall.adapter.app.store.AppCouponCenterController;
import com.lemnt.mall.adapter.web.store.req.CouponPublicClaimReq;
import com.lemnt.mall.application.store.cmd.CartAddCmd;
import com.lemnt.mall.application.store.dto.CartAdditionDTO;
import com.lemnt.mall.application.store.executor.CartAddCmdExe;
import com.lemnt.mall.application.store.executor.CouponClaimCmdExe;
import com.lemnt.mall.application.store.executor.CouponOffersPageQueryExe;
import com.lemnt.mall.application.store.executor.CouponPublicClaimCmdExe;
import com.lemnt.mall.application.store.executor.CouponPublicTemplateListQueryExe;
import com.lemnt.mall.application.store.executor.CouponWalletPageQueryExe;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PromotionHttpContractTest {
    CouponOffersPageQueryExe couponOffersPageQueryExe = mock(CouponOffersPageQueryExe.class);
    CouponWalletPageQueryExe couponWalletPageQueryExe = mock(CouponWalletPageQueryExe.class);
    CouponPublicTemplateListQueryExe couponPublicTemplateListQueryExe = mock(CouponPublicTemplateListQueryExe.class);
    CouponClaimCmdExe couponClaimCmdExe = mock(CouponClaimCmdExe.class);
    CartAddCmdExe cartAddCmdExe = mock(CartAddCmdExe.class);
    CouponPublicClaimCmdExe couponPublicClaimCmdExe = mock(CouponPublicClaimCmdExe.class);

    @org.junit.jupiter.api.BeforeEach
    void identity() { com.lemnt.common.security.context.SecurityContextHolder.setUserId("7"); }

    @org.junit.jupiter.api.AfterEach
    void clearIdentity() { com.lemnt.common.security.context.SecurityContextHolder.remove(); }

    @Test
    void cartAdditionAlwaysUsesAuthoritativeMemberAndRequestNumber() throws Exception {
        when(cartAddCmdExe.execute(new CartAddCmd(7L, 8L, 2, "ADD-1")))
            .thenReturn(new CartAdditionDTO("ADD-1", 8L, 2, 5));
        var mvc =
        MockMvcBuilders.standaloneSetup(
                new Object[] {
                new AppCouponCenterController(couponClaimCmdExe, couponOffersPageQueryExe, couponWalletPageQueryExe),
                new AppCartAdditionController(cartAddCmdExe)
        })
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build();
        mvc.perform(
                post("/mall/app/cart/8/add")
                .header("Authorization", "Bearer member")
                .header("memberId", "999")
                .contentType("application/json")
                .content(
                "{\"quantity\":2,\"requestNo\":\"ADD-1\",\"memberId\":999,\"price\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.resultingQuantity").value(5));
        verify(cartAddCmdExe).execute(new CartAddCmd(7L, 8L, 2, "ADD-1"));
    }

    @Test
    void invalidQuantityCannotReachWrite() throws Exception {
        var mvc =
        MockMvcBuilders.standaloneSetup(
                new Object[] {
                new AppCouponCenterController(couponClaimCmdExe, couponOffersPageQueryExe, couponWalletPageQueryExe),
                new AppCartAdditionController(cartAddCmdExe)
        })
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build();
        mvc.perform(
                post("/mall/app/cart/3/add")
                .contentType("application/json")
                .content("{\"quantity\":1000,\"requestNo\":\"R1\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(cartAddCmdExe, couponClaimCmdExe, couponPublicClaimCmdExe, couponOffersPageQueryExe, couponWalletPageQueryExe, couponPublicTemplateListQueryExe);
    }

    @Test
    void publicClaimSettingsHaveExistingAdminPermissions() throws Exception {
        assertEquals(
                "mall:coupon-template:list",
                PublicCouponController.class
                .getMethod("settings")
                .getAnnotation(RequiresPermissions.class)
                .value()[0]);
        assertEquals(
                "mall:coupon-template:edit",
                PublicCouponController.class
                .getMethod(
                "setting",
                Long.class,
                CouponPublicClaimReq.class)
                .getAnnotation(RequiresPermissions.class)
                .value()[0]);
    }
}
