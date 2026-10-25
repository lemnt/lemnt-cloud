package com.lemnt.trade.adapter.web.store;

import com.lemnt.trade.application.checkout.service.MemberSessionService;
import com.lemnt.trade.application.store.executor.*;
import com.lemnt.trade.application.store.cmd.*;
import com.lemnt.trade.application.store.dto.CartAdditionDTO;
import com.lemnt.common.security.annotation.RequiresPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PromotionHttpContractTest {
    @Test
    void cartAdditionAlwaysUsesAuthoritativeMemberAndRequestNumber() throws Exception {
        var sessions = mock(MemberSessionService.class);
        when(sessions.requireMember("Bearer member")).thenReturn(7L);
        var commands = mock(StorefrontCmdExe.class);
        var queries = mock(PromotionQueryExe.class);
        when(commands.execute(new CartAddCmd(7L, 8L, 2, "ADD-1")))
                .thenReturn(new CartAdditionDTO("ADD-1", 8L, 2, 5));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppPromotionController(sessions, commands, queries))
                        .build();
        mvc.perform(
                        post("/trade/app/cart/8/add")
                                .header("Authorization", "Bearer member")
                                .header("memberId", "999")
                                .contentType("application/json")
                                .content(
                                        "{\"quantity\":2,\"requestNo\":\"ADD-1\",\"memberId\":999,\"price\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resultingQuantity").value(5));
        verify(commands).execute(new CartAddCmd(7L, 8L, 2, "ADD-1"));
    }

    @Test
    void unauthenticatedClaimAndInvalidQuantityCannotReachWrite() throws Exception {
        var sessions = mock(MemberSessionService.class);
        when(sessions.requireMember(null)).thenThrow(new SecurityException());
        var commands = mock(StorefrontCmdExe.class);
        var queries = mock(PromotionQueryExe.class);
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppPromotionController(sessions, commands, queries))
                        .build();
        mvc.perform(post("/trade/app/coupon-center/3/claim").header("memberId", "999"))
                .andExpect(status().isUnauthorized());
        mvc.perform(
                        post("/trade/app/cart/3/add")
                                .contentType("application/json")
                                .content("{\"quantity\":1000,\"requestNo\":\"R1\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(commands, queries);
    }

    @Test
    void publicClaimSettingsHaveExistingAdminPermissions() throws Exception {
        assertEquals(
                "trade:coupon-template:list",
                AdminPublicCouponController.class
                        .getMethod("settings")
                        .getAnnotation(RequiresPermissions.class)
                        .value()[0]);
        assertEquals(
                "trade:coupon-template:edit",
                AdminPublicCouponController.class
                        .getMethod(
                                "setting",
                                Long.class,
                                com.lemnt.trade.adapter.web.store.req.CouponPublicClaimReq.class)
                        .getAnnotation(RequiresPermissions.class)
                        .value()[0]);
    }
}
