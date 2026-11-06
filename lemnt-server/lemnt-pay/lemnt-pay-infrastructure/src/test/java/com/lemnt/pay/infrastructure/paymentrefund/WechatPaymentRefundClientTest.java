package com.lemnt.pay.infrastructure.paymentrefund;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.infrastructure.payment.payment.WechatPayClientProvider;
import com.lemnt.pay.infrastructure.paymentrefund.channel.WechatPaymentRefundClient;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.Amount;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 微信退款以本地退款单号为幂等键，核验渠道单号与金额后映射状态。
 */
class WechatPaymentRefundClientTest {
    final WechatPayClientProvider clients = mock(WechatPayClientProvider.class);
    final WechatPayClient client = mock(WechatPayClient.class);
    final RefundService refundService = mock(RefundService.class);
    final WechatPaymentRefundClient target = new WechatPaymentRefundClient(clients);

    final PayChannelConfig config =
        new PayChannelConfig(9L, 1L, "WECHAT", 0, BigDecimal.ZERO, null, "{}", null);
    final PaymentRecord payment =
        new PaymentRecord(
            1L,
            "PAY1",
            "TR1",
            "WECHAT",
            "lemnt-mall",
            "WX-1",
            new BigDecimal("8.00"),
            "SUCCESS",
            "N-1");
    final PaymentRefund refund =
        new PaymentRefund(
            "PR1",
            "RF1",
            "PAY1",
            "TR1",
            "WECHAT",
            new BigDecimal("8.00"),
            "WAITING",
            null);

    @BeforeEach
    void setUp() {
        when(clients.get(config)).thenReturn(client);
        when(client.refund()).thenReturn(refundService);
    }

    @Test
    void refundSubmitsLocalRefundNoAndMapsSuccess() {
        when(refundService.create(any())).thenReturn(channelRefund(Status.SUCCESS, 800, "WR1"));

        var result = target.refund(refund, payment, config);

        ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
        verify(refundService).create(captor.capture());
        CreateRequest request = captor.getValue();
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("PR1", request.getOutRefundNo());
        assertEquals(800L, request.getAmount().getRefund());
        assertEquals(800L, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertEquals("SUCCESS", result.status());
        assertEquals(new BigDecimal("8.00"), result.amount());
        assertEquals("WR1", result.channelRefundNo());
        assertEquals("WECHAT", result.channel());
    }

    @Test
    void queryReturnsNullWhenChannelHasNoRefund() {
        when(refundService.queryByOutRefundNo(any())).thenThrow(serviceError("RESOURCE_NOT_EXISTS"));

        assertNull(target.query(refund, payment, config));
    }

    @Test
    void queryMapsProcessingWithoutChannelRefundNoToWaiting() {
        when(refundService.queryByOutRefundNo(any()))
            .thenReturn(channelRefund(Status.PROCESSING, 800, null));

        var result = target.query(refund, payment, config);

        assertEquals("WAITING", result.status());
        assertNull(result.channelRefundNo());
    }

    @Test
    void queryMapsAbnormalRefundToFailed() {
        when(refundService.queryByOutRefundNo(any()))
            .thenReturn(channelRefund(Status.ABNORMAL, 800, "WR1"));

        assertEquals("FAILED", target.query(refund, payment, config).status());
    }

    @Test
    void channelAmountMismatchIsRejected() {
        when(refundService.create(any())).thenReturn(channelRefund(Status.SUCCESS, 700, "WR1"));

        assertThrows(IllegalStateException.class, () -> target.refund(refund, payment, config));
    }

    @Test
    void mismatchedPaymentIsRejectedBeforeCallingChannel() {
        PaymentRefund otherChannel =
            new PaymentRefund(
                "PR1",
                "RF1",
                "PAY1",
                "TR1",
                "ALIPAY",
                new BigDecimal("8.00"),
                "WAITING",
                null);

        assertThrows(
            IllegalStateException.class, () -> target.refund(otherChannel, payment, config));
        verifyNoInteractions(refundService);
    }

    private Refund channelRefund(Status status, long refundFen, String refundId) {
        Refund value = new Refund();
        value.setOutRefundNo("PR1");
        value.setOutTradeNo("PAY1");
        value.setRefundId(refundId);
        value.setStatus(status);
        Amount amount = new Amount();
        amount.setRefund(refundFen);
        amount.setTotal(800L);
        value.setAmount(amount);
        return value;
    }

    private ServiceException serviceError(String code) {
        return new ServiceException(null, 404, "{\"code\":\"" + code + "\",\"message\":\"x\"}");
    }
}
