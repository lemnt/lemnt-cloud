package com.lemnt.trade.infrastructure.member;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MemberSessionGatewayImplTest {
    @Test
    void missingOrAmbiguousAuthorityFailsClosed() {
        assertNull(new MemberSessionGatewayImpl(List.of()).authenticate("admin-token"));
        assertNull(
                new MemberSessionGatewayImpl(List.of(token -> 1L, token -> 2L))
                        .authenticate("member-token"));
    }

    @Test
    void singleAuthorityIsUsedWithoutIdentityFallback() {
        assertEquals(
                7L,
                new MemberSessionGatewayImpl(List.of(token -> "member".equals(token) ? 7L : null))
                        .authenticate("member"));
        assertNull(new MemberSessionGatewayImpl(List.of(token -> null)).authenticate("admin"));
    }
}
