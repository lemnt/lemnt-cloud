package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.dto.OrderStatisticsDTO;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台订单统计，复用芋道的四项统计含义并保持人民币元精度。 */
@Component
@RequiredArgsConstructor
public class OrderStatisticsQueryExe {
    private final TradeOrderGateway orders;
    private final PaymentRefundGateway refunds;

    /** 应付金额不是净收款，退款仅统计渠道已确认成功的支付退单。 */
    public OrderStatisticsDTO execute(String orderNo) {
        String no = orderNo == null || orderNo.isBlank() ? null : OrderRules.text(orderNo, 40);
        var ordered = orders.total(no);
        var refunded = refunds.total(no);
        return new OrderStatisticsDTO(ordered.orderCount(), ordered.orderPayPrice(),
                refunded.refundCount(), refunded.refundAmount());
    }
}
