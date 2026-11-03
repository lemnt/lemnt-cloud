package com.lemnt.pay.adapter.app.payment;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.pay.application.payment.dto.PaymentChannelDTO;
import com.lemnt.pay.application.payment.executor.MemberPaymentChannelQueryExe;
import com.lemnt.pay.application.payment.executor.SandboxPaymentCmdExe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 小程序收银台渠道与沙箱支付 HTTP 契约。 */
class AppPaymentChannelControllerTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @AfterEach
    void clearIdentity() {
        SecurityContextHolder.remove();
    }

    @Test
    void channelsAreScopedToAppKey() throws Exception {
        var query = mock(MemberPaymentChannelQueryExe.class);
        when(query.execute("lemnt-mall"))
                .thenReturn(List.of(new PaymentChannelDTO("SANDBOX", "沙箱")));
        var mvc =
                MockMvcBuilders.standaloneSetup(new AppPaymentChannelController(query))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/app/payment-channels").param("appKey", "lemnt-mall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("SANDBOX"))
                .andExpect(jsonPath("$.data[0].name").value("沙箱"));
        mvc.perform(get("/trade/app/payment-channels")).andExpect(status().isBadRequest());
    }

    @Test
    void sandboxPayIsDisabledWhenAdapterIsOff() throws Exception {
        SecurityContextHolder.setUserId("7");
        @SuppressWarnings("unchecked")
        ObjectProvider<SandboxPaymentCmdExe> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        var mvc =
                MockMvcBuilders.standaloneSetup(new AppSandboxPaymentController(provider))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(post("/trade/app/payments/PAY1/sandbox-pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("沙箱支付未启用"));
    }

    @Test
    void sandboxPayUsesSessionMember() throws Exception {
        SecurityContextHolder.setUserId("7");
        var exe = mock(SandboxPaymentCmdExe.class);
        when(exe.execute(7L, "PAY1")).thenReturn("PAY1");
        @SuppressWarnings("unchecked")
        ObjectProvider<SandboxPaymentCmdExe> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(exe);
        var mvc =
                MockMvcBuilders.standaloneSetup(new AppSandboxPaymentController(provider))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(post("/trade/app/payments/PAY1/sandbox-pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("PAY1"));
    }
}
