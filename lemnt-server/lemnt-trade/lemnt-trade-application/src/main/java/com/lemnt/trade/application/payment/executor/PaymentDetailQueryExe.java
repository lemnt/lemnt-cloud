package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.PaymentDTOConverter;
import com.lemnt.trade.application.payment.dto.PaymentDTO;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付单管理查询执行器。 */
@Component
@RequiredArgsConstructor
public class PaymentDetailQueryExe {
    private final TradePaymentGateway gateway;

    /** 按交易编号查询详情。 */
    public PaymentDTO execute(String no) {
        return PaymentDTOConverter.toDTO(gateway.findByPaymentNo(OrderRules.text(no, 40)));
    }
}
