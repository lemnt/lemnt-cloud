package com.lemnt.order.application.dubbo;

import com.lemnt.order.application.event.service.PayRefundSuccessEventHandler;
import com.lemnt.order.application.event.service.PaySuccessEventHandler;
import com.lemnt.order.client.OrderPaymentResultClient;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/** 通知返回时订单或售后事务已提交。 */
@DubboService
@RequiredArgsConstructor
public class OrderPaymentResultClientImpl implements OrderPaymentResultClient {
    private final PaySuccessEventHandler payments;
    private final PayRefundSuccessEventHandler refunds;

    @Override
    public void paid(String orderNo, String paymentNo) {
        payments.confirm(orderNo, paymentNo);
    }

    @Override
    public void refunded(String orderNo, String afterSaleNo, String refundNo) {
        refunds.confirm(orderNo, afterSaleNo, refundNo);
    }
}
