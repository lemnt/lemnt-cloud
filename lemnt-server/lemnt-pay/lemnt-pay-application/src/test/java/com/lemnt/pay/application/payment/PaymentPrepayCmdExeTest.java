package com.lemnt.pay.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.cmd.PaymentPrepayCmd;
import com.lemnt.pay.application.payment.executor.PaymentPrepayCmdExe;
import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.PayAppGateway;
import com.lemnt.pay.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.payment.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.payment.model.PayApp;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 发起支付前的订单归属、状态、应用与渠道校验。 */
class PaymentPrepayCmdExeTest {
    final OrderPaymentGateway orders = mock(OrderPaymentGateway.class);
    final PayAppGateway apps = mock(PayAppGateway.class);
    final PayChannelConfigGateway channels = mock(PayChannelConfigGateway.class);
    final TradePaymentCreateCmdExe payments = mock(TradePaymentCreateCmdExe.class);
    final PaymentChannelGateway gateway = mock(PaymentChannelGateway.class);
    final PaymentPrepayCmdExe exe =
            new PaymentPrepayCmdExe(orders, apps, channels, payments, gateway);

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

    private PaymentPrepayCmd cmd() {
        return new PaymentPrepayCmd(7L, "TR1", "lemnt-mall", "sandbox");
    }

    private OrderPaymentSnapshot order(Long memberId, BigDecimal total) {
        return new OrderPaymentSnapshot("TR1", memberId, total, "PENDING_PAYMENT");
    }

    @Test
    void ownerPendingOrderCreatesPaymentAndPrepay() {
        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, new BigDecimal("8.00")));
        when(apps.findByAppKey("lemnt-mall")).thenReturn(app);
        when(channels.listByAppId(1L)).thenReturn(List.of(config));
        when(payments.execute("TR1", "SANDBOX", "lemnt-mall")).thenReturn(payment);
        when(gateway.prepay(payment, config))
                .thenReturn(
                        new PaymentPrepay(
                                "PAY1",
                                "SANDBOX",
                                null,
                                Map.of(
                                        "paymentNo",
                                        "PAY1",
                                        "channel",
                                        "SANDBOX",
                                        "appKey",
                                        "lemnt-mall")));

        var result = exe.execute(cmd());

        assertEquals("PAY1", result.paymentNo());
        assertEquals("SANDBOX", result.channel());
        assertEquals(new BigDecimal("8.00"), result.amount());
        assertEquals("lemnt-mall", result.params().get("appKey"));
    }

    @Test
    void foreignClosedOrZeroOrderIsRejectedBeforePaymentCreation() {
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
        when(orders.findByOrderNo("TR1")).thenReturn(order(8L, new BigDecimal("8.00")));
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, BigDecimal.ZERO));
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
        verify(payments, never()).execute(any(), any(), any());
    }

    @Test
    void disabledApplicationOrChannelFailsClosed() {
        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, new BigDecimal("8.00")));
        when(apps.findByAppKey("lemnt-mall"))
                .thenReturn(new PayApp(1L, "lemnt-mall", "商城", 1, null, null, null, null));
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));

        when(apps.findByAppKey("lemnt-mall")).thenReturn(app);
        when(channels.listByAppId(1L)).thenReturn(List.of());
        assertThrows(ServiceException.class, () -> exe.execute(cmd()));
        verifyNoInteractions(payments, gateway);
    }
}
