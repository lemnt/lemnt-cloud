package com.lemnt.auth.adapter.app.member;

import com.lemnt.auth.application.member.executor.MemberAuthCmdExe;
import com.lemnt.auth.application.member.service.MemberAuthenticationService;
import com.lemnt.auth.domain.member.model.MemberToken;
import com.lemnt.common.security.handler.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppMemberAuthControllerTest {
    private MemberAuthenticationService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(MemberAuthenticationService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AppMemberAuthController(new MemberAuthCmdExe(service)))
            .setControllerAdvice(new MemberAuthExceptionHandler()).build();
    }

    @Test
    void originalLoginPathUsesLocalAuthAndPreservesResponse() throws Exception {
        when(service.login("13800000001", "Member-password-2026"))
            .thenReturn(new MemberToken(7L, "member.jwt.signature", 2592000L));
        mvc.perform(post("/member/app/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"13800000001\",\"password\":\"Member-password-2026\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.memberId").value(7))
            .andExpect(jsonPath("$.data.accessToken").value("member.jwt.signature"))
            .andExpect(jsonPath("$.data.expiresIn").value(2592000));
        verify(service).login("13800000001", "Member-password-2026");
    }

    @Test
    void refreshAndLogoutKeepOriginalContracts() throws Exception {
        when(service.refresh("member.jwt.signature")).thenReturn(new MemberToken(7L, "member.jwt.signature", 2592000L));
        when(service.logout("Bearer mbr_a_test")).thenReturn(true);
        mvc.perform(post("/member/app/auth/refresh-token").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer member.jwt.signature"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").value("member.jwt.signature"));
        mvc.perform(post("/member/app/auth/logout").header("Authorization", "Bearer mbr_a_test"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        verify(service).logout("Bearer mbr_a_test");
    }

    @Test
    void invalidBodiesDoNotInvokeAuthentication() throws Exception {
        mvc.perform(post("/member/app/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void authenticationErrorsPreserveHttpStatus() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new AppMemberAuthController(new MemberAuthCmdExe(service)))
            .setControllerAdvice(new GlobalExceptionHandler(),
                new MemberAuthExceptionHandler()).build();
        when(service.refresh("expired")).thenThrow(new SecurityException("会员会话无效"));
        mvc.perform(post("/member/app/auth/refresh-token").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer expired"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("会员会话无效"));
        when(service.login("invalid", "password")).thenThrow(new IllegalArgumentException("手机号不合法"));
        mvc.perform(post("/member/app/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"invalid\",\"password\":\"password\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test
    void trustedGatewayHeaderBecomesLoginAuditContext() throws Exception {
        when(service.login("13800000001","Member-password-2026","203.0.113.7"))
                .thenReturn(new MemberToken(7L,"member.jwt.signature",2592000L));
        mvc.perform(post("/member/app/auth/login").header("X-Lemnt-Login-Ip","203.0.113.7")
                .contentType(MediaType.APPLICATION_JSON).content("{\"mobile\":\"13800000001\",\"password\":\"Member-password-2026\"}"))
                .andExpect(status().isOk());
        verify(service).login("13800000001","Member-password-2026","203.0.113.7");
    }

}
