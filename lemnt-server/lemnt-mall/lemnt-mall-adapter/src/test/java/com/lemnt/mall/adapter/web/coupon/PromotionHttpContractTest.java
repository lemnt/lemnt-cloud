package com.lemnt.mall.adapter.web.coupon;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.app.cart.AppCartController;
import com.lemnt.mall.adapter.app.coupon.AppCouponCenterController;
import com.lemnt.mall.adapter.web.coupon.req.CouponPublicClaimReq;
import com.lemnt.mall.application.cart.cmd.CartSaveCmd;
import com.lemnt.mall.application.cart.dto.CartItemDTO;
import com.lemnt.mall.application.cart.executor.CartSaveCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponClaimCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponOffersPageQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponPublicClaimCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponPublicTemplateListQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponWalletPageQueryExe;

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
    CartSaveCmdExe cartAddCmdExe = mock(CartSaveCmdExe.class);
    CouponPublicClaimCmdExe couponPublicClaimCmdExe = mock(CouponPublicClaimCmdExe.class);

    @org.junit.jupiter.api.BeforeEach
    void identity() { com.lemnt.common.security.context.SecurityContextHolder.setUserId("7"); }

    @org.junit.jupiter.api.AfterEach
    void clearIdentity() { com.lemnt.common.security.context.SecurityContextHolder.remove(); }

    @Test
    void cartQuantityAlwaysUsesAuthoritativeMember() throws Exception {
        when(cartAddCmdExe.execute(new CartSaveCmd(7L, 8L, 2, true)))
            .thenReturn(new CartItemDTO(1L, 8L, 2, true, 0L, "商品", "SKU", java.math.BigDecimal.TEN, 10, true));
        var mvc =
        MockMvcBuilders.standaloneSetup(
                new Object[] {
                new AppCouponCenterController(couponClaimCmdExe, couponOffersPageQueryExe, couponWalletPageQueryExe),
                new AppCartController(mock(com.lemnt.mall.application.cart.executor.CartListQueryExe.class), cartAddCmdExe, mock(com.lemnt.mall.application.cart.executor.CartDeleteCmdExe.class))
        })
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build();
        mvc.perform(
                put("/mall/app/cart/8")
                .header("Authorization", "Bearer member")
                .header("memberId", "999")
                .contentType("application/json")
                .content(
                "{\"quantity\":2,\"selected\":true,\"memberId\":999,\"price\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.quantity").value(2));
        verify(cartAddCmdExe).execute(new CartSaveCmd(7L, 8L, 2, true));
    }

    @Test
    void invalidQuantityCannotReachWrite() throws Exception {
        var mvc =
        MockMvcBuilders.standaloneSetup(
                new Object[] {
                new AppCouponCenterController(couponClaimCmdExe, couponOffersPageQueryExe, couponWalletPageQueryExe),
                new AppCartController(mock(com.lemnt.mall.application.cart.executor.CartListQueryExe.class), cartAddCmdExe, mock(com.lemnt.mall.application.cart.executor.CartDeleteCmdExe.class))
        })
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor())
            .defaultRequest(get("/").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7"))
            .build();
        mvc.perform(
                put("/mall/app/cart/3")
                .contentType("application/json")
                .content("{\"quantity\":1000}"))
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
