package com.lemnt.trade.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.cmd.PaymentNotifyReceiveCmd;
import com.lemnt.trade.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.trade.application.payment.executor.PaymentNotifyReceiveCmdExe;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.gateway.PaymentChannelGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 渠道通知先按应用验签，再绑定本地支付单，最后提交支付结果。 */
class PaymentNotifyReceiveCmdTest {
    final PayAppGateway apps = mock(PayAppGateway.class);
    final PayChannelConfigGateway channels = mock(PayChannelConfigGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentChannelGateway gateway = mock(PaymentChannelGateway.class);
    final PaymentNotifyCmdExe notify = mock(PaymentNotifyCmdExe.class);
    final PaymentNotifyReceiveCmdExe exe =
            new PaymentNotifyReceiveCmdExe(apps, channels, payments, gateway, notify);

    final PayApp app = new PayApp(1L, "lemnt-mall", "商城", 0, null, null, null, null);
    final PayChannelConfig config =
            new PayChannelConfig(
                    1L,
                    1L,
                    "SANDBOX",
                    0,
                    BigDecimal.ZERO,
                    null,
                    "{\"hmacSecret\":\"0123456789abcdef\"}",
                    null);
    final PaymentRecord payment =
            new PaymentRecord(
                    1L,
                    "PAY1",
                    "TR1",
                    "SANDBOX",
                    "lemnt-mall",
                    null,
                    new BigDecimal("8.00"),
                    "WAITING",
                    null);
    final VerifiedPaymentNotification verified =
            new VerifiedPaymentNotification(
                    "PAY1", "TR1", "SANDBOX", "SB-1", new BigDecimal("8.00"), "N-1");

    private PaymentNotifyReceiveCmd cmd() {
        return new PaymentNotifyReceiveCmd("lemnt-mall", "sandbox", "{}", "sign", "1");
    }

    @Test
    void verifiedNotificationIsSubmittedToPaymentPipeline() {
        when(apps.findByAppKey("lemnt-mall")).thenReturn(app);
        when(channels.listByAppId(1L)).thenReturn(List.of(config));
        when(gateway.verify("SANDBOX", config, "{}", "sign", "1")).thenReturn(verified);
        when(payments.findByPaymentNo("PAY1")).thenReturn(payment);

        assertEquals("PAY1", exe.execute(cmd()));
        verify(notify).execute(verified);
    }

    @Test
    void mismatchedApplicationOrChannelKeepsLocalPaymentUntouched() {
        when(apps.findByAppKey("lemnt-mall")).thenReturn(app);
        when(channels.listByAppId(1L)).thenReturn(List.of(config));
        when(gateway.verify(any(), any(), any(), any(), any())).thenReturn(verified);
        when(payments.findByPaymentNo("PAY1"))
                .thenReturn(
                        new PaymentRecord(
                                1L,
                                "PAY1",
                                "TR1",
                                "SANDBOX",
                                "other-app",
                                null,
                                new BigDecimal("8.00"),
                                "WAITING",
                                null));
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
        verify(notify, never()).execute(any());

        when(channels.listByAppId(1L)).thenReturn(List.of());
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
    }
}
