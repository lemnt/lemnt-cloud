package com.lemnt.pay.client;

import com.lemnt.pay.client.dto.PaymentRefundResultDTO;
import com.lemnt.pay.client.dto.PaymentRefundTotalDTO;
import com.lemnt.pay.client.dto.PaymentResultDTO;

import java.util.List;

/** 订单服务核对支付/退款的只读可信接口。 */
public interface PaymentQueryClient {
    PaymentRefundTotalDTO refundTotal(String orderNo);

    List<PaymentResultDTO> findByOrderNo(String orderNo);

    PaymentResultDTO find(String paymentNo);

    PaymentRefundResultDTO refundByAfterSale(String afterSaleNo);
}
