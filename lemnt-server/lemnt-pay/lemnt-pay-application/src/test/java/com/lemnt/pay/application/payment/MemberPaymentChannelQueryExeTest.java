package com.lemnt.pay.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.executor.MemberPaymentChannelQueryExe;
import com.lemnt.pay.domain.channel.gateway.PayAppGateway;
import com.lemnt.pay.domain.channel.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.channel.model.PayApp;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 收银台只展示应用下已启用的渠道，未知编码不返回。
 */
class MemberPaymentChannelQueryExeTest {
    final PayAppGateway apps = mock(PayAppGateway.class);
    final PayChannelConfigGateway channels = mock(PayChannelConfigGateway.class);
    final PaymentChannelGateway gateway = mock(PaymentChannelGateway.class);
    final MemberPaymentChannelQueryExe exe =
            new MemberPaymentChannelQueryExe(apps, channels, gateway);

    private PayChannelConfig config(String code, int status) {
        return new PayChannelConfig(1L, 1L, code, status, BigDecimal.ZERO, null, null, null);
    }

    @Test
    void enabledChannelsAreDistinctAndUnknownCodesSkipped() {
        when(apps.findByAppKey("lemnt-mall"))
            .thenReturn(new PayApp(1L, "lemnt-mall", "商城", 0, null, null, null, null));
        when(channels.listByAppId(1L))
                .thenReturn(
                        List.of(
                                config("WECHAT", 0),
                                config("WALLET", 0),
                                config("WALLET", 0),
                                config("SANDBOX", 0),
                                config("ALIPAY", 0),
                                config("UNKNOWN", 0)));
        when(gateway.supports("WECHAT")).thenReturn(true);
        var result = exe.execute("lemnt-mall");
        assertEquals(List.of("WECHAT", "WALLET"), result.stream().map(v -> v.code()).toList());
    }

    @Test
    void missingOrDisabledAppIsRejected() {
        assertThrows(ServiceException.class, () -> exe.execute(""));
        when(apps.findByAppKey("lemnt-mall"))
            .thenReturn(new PayApp(1L, "lemnt-mall", "商城", 1, null, null, null, null));
        assertThrows(ServiceException.class, () -> exe.execute("lemnt-mall"));
        verifyNoInteractions(channels);
    }
}
