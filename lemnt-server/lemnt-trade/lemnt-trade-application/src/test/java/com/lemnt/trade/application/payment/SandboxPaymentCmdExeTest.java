package com.lemnt.trade.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.trade.application.payment.executor.SandboxPaymentCmdExe;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 沙箱支付确认只允许本人待支付支付单，成功重试幂等。 */
class SandboxPaymentCmdExeTest {
    final OrderPersistenceService orders = mock(OrderPersistenceService.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentNotifyCmdExe notify = mock(PaymentNotifyCmdExe.class);
    final SandboxPaymentCmdExe exe = new SandboxPaymentCmdExe(orders, payments, notify);

    private TradeOrder order(Long memberId, String status) {
        return new TradeOrder(
                1L, "TR1", memberId, new BigDecimal("8.00"), status, List.of(), null, 0, 0, null);
    }

    private PaymentRecord payment(String status) {
        return new PaymentRecord(
                1L,
                "PAY1",
                "TR1",
                "SANDBOX",
                "lemnt-mall",
                null,
                new BigDecimal("8.00"),
                status,
                null);
    }

    @Test
    void ownerWaitingPaymentIsSubmittedOnce() {
        when(payments.findByPaymentNo("PAY1")).thenReturn(payment("WAITING"));
        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, "PENDING_PAYMENT"));
        assertEquals("PAY1", exe.execute(7L, "PAY1"));
        verify(notify).execute(any());
    }

    @Test
    void successfulPaymentIsIdempotentWithoutSecondNotification() {
        when(payments.findByPaymentNo("PAY1")).thenReturn(payment("SUCCESS"));
        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, "PAID"));
        assertEquals("PAY1", exe.execute(7L, "PAY1"));
        verifyNoInteractions(notify);
    }

    @Test
    void foreignClosedOrNonSandboxPaymentIsRejected() {
        when(payments.findByPaymentNo("PAY1")).thenReturn(payment("WAITING"));
        when(orders.findByOrderNo("TR1")).thenReturn(order(8L, "PENDING_PAYMENT"));
        assertThrows(ServiceException.class, () -> exe.execute(7L, "PAY1"));

        when(orders.findByOrderNo("TR1")).thenReturn(order(7L, "PENDING_PAYMENT"));
        when(payments.findByPaymentNo("PAY1")).thenReturn(payment("CLOSED"));
        assertThrows(ServiceException.class, () -> exe.execute(7L, "PAY1"));

        when(payments.findByPaymentNo("PAY1"))
                .thenReturn(
                        new PaymentRecord(
                                1L,
                                "PAY1",
                                "TR1",
                                "ALIPAY",
                                "lemnt-mall",
                                null,
                                new BigDecimal("8.00"),
                                "WAITING",
                                null));
        assertThrows(ServiceException.class, () -> exe.execute(7L, "PAY1"));
        verifyNoInteractions(notify);
    }
}
