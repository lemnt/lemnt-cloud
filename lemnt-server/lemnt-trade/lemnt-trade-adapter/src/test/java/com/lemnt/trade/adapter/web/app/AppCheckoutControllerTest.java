package com.lemnt.trade.adapter.web.app;

import com.lemnt.trade.application.checkout.service.MemberSessionService;
import com.lemnt.trade.application.checkout.executor.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppCheckoutControllerTest {
    @Test
    void rejectedSessionCannotReachCart() throws Exception {
        var session = mock(MemberSessionService.class);
        var list = mock(CartListQueryExe.class);
        when(session.requireMember(null)).thenThrow(new SecurityException());
        var controller =
                new AppCheckoutController(
                        session,
                        list,
                        mock(CartSaveCmdExe.class),
                        mock(CartDeleteCmdExe.class),
                        mock(CouponListQueryExe.class),
                        mock(CheckoutQuoteQueryExe.class),
                        mock(CheckoutSubmitCmdExe.class),
                        mock(CheckoutReceiptQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/trade/app/cart").header("memberId", "999"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(list);
    }

    @Test
    void cartUsesSessionMemberInsteadOfQueryOrHeaderIdentity() throws Exception {
        var session = mock(MemberSessionService.class);
        var list = mock(CartListQueryExe.class);
        when(session.requireMember("Bearer member")).thenReturn(7L);
        when(list.execute(7L)).thenReturn(List.of());
        var controller =
                new AppCheckoutController(
                        session,
                        list,
                        mock(CartSaveCmdExe.class),
                        mock(CartDeleteCmdExe.class),
                        mock(CouponListQueryExe.class),
                        mock(CheckoutQuoteQueryExe.class),
                        mock(CheckoutSubmitCmdExe.class),
                        mock(CheckoutReceiptQueryExe.class));
        MockMvcBuilders.standaloneSetup(controller)
                .build()
                .perform(
                        get("/trade/app/cart")
                                .header("Authorization", "Bearer member")
                                .header("memberId", "999")
                                .param("memberId", "999"))
                .andExpect(status().isOk());
        verify(list).execute(7L);
        verify(list, never()).execute(999L);
    }

    @Test
    void orderQueryPassesSessionIdentityToOwnershipQuery() {
        var session = mock(MemberSessionService.class);
        when(session.requireMember("Bearer member")).thenReturn(7L);
        var receipt = mock(CheckoutReceiptQueryExe.class);
        var controller =
                new AppCheckoutController(
                        session,
                        mock(CartListQueryExe.class),
                        mock(CartSaveCmdExe.class),
                        mock(CartDeleteCmdExe.class),
                        mock(CouponListQueryExe.class),
                        mock(CheckoutQuoteQueryExe.class),
                        mock(CheckoutSubmitCmdExe.class),
                        receipt);
        assertNull(controller.order("Bearer member", "FOREIGN").getData());
        verify(receipt).execute(7L, "FOREIGN");
    }
}
