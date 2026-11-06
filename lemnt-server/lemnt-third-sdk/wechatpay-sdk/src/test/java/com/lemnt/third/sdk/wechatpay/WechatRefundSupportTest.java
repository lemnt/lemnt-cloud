package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.Amount;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 退款：业务入参映射为微信请求，金额与状态归并为本地可用的结果。
 */
class WechatRefundSupportTest {
    final RefundService service = mock(RefundService.class);
    final WechatRefundCommand command =
        new WechatRefundCommand(
            "PR1", "PAY1", new BigDecimal("8.00"), new BigDecimal("8.00"), "售后RF1");

    @Test
    void createMapsBusinessFieldsAndReturnsYuanAmount() {
        when(service.create(any())).thenReturn(channelRefund("PR1", "WR1", 800, Status.SUCCESS));

        var result = WechatRefundSupport.create(service, command);

        ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
        verify(service).create(captor.capture());
        CreateRequest request = captor.getValue();
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("PR1", request.getOutRefundNo());
        assertEquals("售后RF1", request.getReason());
        assertEquals(800L, request.getAmount().getRefund());
        assertEquals(800L, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertEquals("PR1", result.outRefundNo());
        assertEquals("WR1", result.refundId());
        assertEquals("PAY1", result.outTradeNo());
        assertEquals(new BigDecimal("8.00"), result.refundAmount());
        assertEquals(WechatRefundStatus.SUCCESS, result.status());
    }

    @Test
    void queryReturnsNullWhenChannelHasNoRefund() {
        when(service.queryByOutRefundNo(any())).thenThrow(serviceError("RESOURCE_NOT_EXISTS"));

        assertNull(WechatRefundSupport.query(service, "PR1"));
    }

    @Test
    void queryMapsProcessingAndAbnormalStatuses() {
        when(service.queryByOutRefundNo(any()))
            .thenReturn(channelRefund("PR1", null, 800, Status.PROCESSING));
        assertEquals(WechatRefundStatus.PROCESSING, WechatRefundSupport.query(service, "PR1").status());

        when(service.queryByOutRefundNo(any()))
            .thenReturn(channelRefund("PR1", "WR1", 800, Status.ABNORMAL));
        assertEquals(WechatRefundStatus.FAILED, WechatRefundSupport.query(service, "PR1").status());
    }

    @Test
    void queryPropagatesUnknownChannelError() {
        when(service.queryByOutRefundNo(any())).thenThrow(serviceError("SYSTEM_ERROR"));

        assertThrows(ServiceException.class, () -> WechatRefundSupport.query(service, "PR1"));
    }

    @Test
    void queryUsesLocalRefundNoAsIdempotencyKey() {
        when(service.queryByOutRefundNo(any()))
            .thenReturn(channelRefund("PR1", "WR1", 800, Status.SUCCESS));

        WechatRefundSupport.query(service, "PR1");

        ArgumentCaptor<QueryByOutRefundNoRequest> captor =
            ArgumentCaptor.forClass(QueryByOutRefundNoRequest.class);
        verify(service).queryByOutRefundNo(captor.capture());
        assertEquals("PR1", captor.getValue().getOutRefundNo());
    }

    private Refund channelRefund(String outRefundNo, String refundId, long fen, Status status) {
        Refund value = new Refund();
        value.setOutRefundNo(outRefundNo);
        value.setOutTradeNo("PAY1");
        value.setRefundId(refundId);
        value.setStatus(status);
        Amount amount = new Amount();
        amount.setRefund(fen);
        amount.setTotal(800L);
        value.setAmount(amount);
        return value;
    }

    private ServiceException serviceError(String code) {
        return new ServiceException(null, 400, "{\"code\":\"" + code + "\",\"message\":\"x\"}");
    }
}
