package com.lemnt.order.client;

/** 支付结果通知只传业务号，订单服务自行核验原资金事实。 */
public interface OrderPaymentResultClient {
    void paid(String orderNo, String paymentNo);

    void refunded(String orderNo, String afterSaleNo, String refundNo);
}
