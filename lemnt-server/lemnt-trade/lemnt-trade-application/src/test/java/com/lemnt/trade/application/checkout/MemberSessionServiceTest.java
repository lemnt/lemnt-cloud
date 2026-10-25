package com.lemnt.trade.application.checkout;

import com.lemnt.trade.application.checkout.service.*;
import com.lemnt.trade.domain.gateway.MemberSessionGateway;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberSessionServiceTest {
    @Test
    void rejectsMissingMalformedAndAdminTokensBeforeBusiness() {
        var gateway = mock(MemberSessionGateway.class);
        var members = mock(CheckoutMemberService.class);
        var service = new MemberSessionService(gateway, members);
        for (String token :
                new String[] {
                    null, "", "Bearer ", "Basic token", "Bearer a b", "Bearer " + "x".repeat(4097)
                }) assertThrows(SecurityException.class, () -> service.requireMember(token));
        verifyNoInteractions(gateway, members);
        assertThrows(SecurityException.class, () -> service.requireMember("Bearer admin-token"));
        verifyNoInteractions(members);
    }

    @Test
    void authoritativeSessionStillRequiresActiveMember() {
        var gateway = mock(MemberSessionGateway.class);
        var members = mock(CheckoutMemberService.class);
        var service = new MemberSessionService(gateway, members);
        when(gateway.authenticate("member-token")).thenReturn(7L);
        assertEquals(7L, service.requireMember("Bearer member-token"));
        verify(members).requireActive(7L);
        doThrow(new IllegalArgumentException("disabled")).when(members).requireActive(7L);
        assertThrows(
                IllegalArgumentException.class, () -> service.requireMember("Bearer member-token"));
    }
}
