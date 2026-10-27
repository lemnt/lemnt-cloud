package com.lemnt.auth.infrastructure.member;

import com.lemnt.auth.infrastructure.member.gateway.MemberAccountGatewayImpl;
import com.lemnt.member.client.MemberUserClient;
import com.lemnt.member.client.dto.MemberLoginInfoDTO;
import com.lemnt.member.client.dto.MemberUserDTO;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberAccountGatewayImplTest {
    @Test
    void delegatesMemberReadsWithoutLocalPersistenceAndRedactsPassword() {
        var client = mock(MemberUserClient.class);
        var gateway = new MemberAccountGatewayImpl();
        ReflectionTestUtils.setField(gateway, "memberUserClient", client);
        when(client.getByMobile("13800000001")).thenReturn(new MemberLoginInfoDTO(7L, "13800000001", 1, "private-hash"));
        when(client.getByMemberId(7L)).thenReturn(new MemberUserDTO(7L, "M7", "member", 1));
        var value = gateway.findByMobile("13800000001");
        assertEquals(7L, value.memberId());
        assertEquals("private-hash", value.passwordHash());
        assertFalse(value.toString().contains("private-hash"));
        assertEquals(1, gateway.findStatusByMemberId(7L));
        when(client.getByMemberId(7L)).thenReturn(new MemberUserDTO(7L, "M7", "member", 0));
        assertEquals(0, gateway.findStatusByMemberId(7L));
        assertNull(gateway.findStatusByMemberId(99L));
        assertNull(gateway.findByMobile("missing"));
        verify(client).getByMobile("13800000001");
    }
}
