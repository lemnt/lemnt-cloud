package com.lemnt.pay.application.dubbo;

import com.lemnt.pay.client.PaymentQueryClient;
import com.lemnt.pay.client.dto.PaymentRefundResultDTO;
import com.lemnt.pay.client.dto.PaymentRefundTotalDTO;
import com.lemnt.pay.client.dto.PaymentResultDTO;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 只读原支付及退款，不推算订单状态或权益完成。
 */
@DubboService
@RequiredArgsConstructor
public class PaymentQueryClientImpl implements PaymentQueryClient {
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;

    @Override
    public PaymentRefundTotalDTO refundTotal(String no) {
        var value = refunds.total(no);
        return new PaymentRefundTotalDTO(value.refundCount(), value.refundAmount());
    }

    @Override
    public List<PaymentResultDTO> findByOrderNo(String no) {
        return payments.findByOrderNo(no).stream().map(this::from).toList();
    }

    @Override
    public PaymentResultDTO find(String no) {
        return from(payments.findByPaymentNo(no));
    }

    @Override
    public PaymentRefundResultDTO refundByAfterSale(String no) {
        var result = refunds.findByAfterSaleNo(no);
        return result == null
            ? null
            : new PaymentRefundResultDTO(
            result.refundNo(),
            result.afterSaleNo(),
            result.paymentNo(),
            result.orderNo(),
            result.channel(),
            result.amount(),
            result.status(),
            result.channelRefundNo());
    }

    private PaymentResultDTO from(PaymentRecord value) {
        return value == null
            ? null
            : new PaymentResultDTO(
            value.paymentNo(),
            value.orderNo(),
            value.channel(),
            value.appKey(),
            value.channelTradeNo(),
            value.amount(),
            value.status());
    }
}
