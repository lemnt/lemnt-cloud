package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.PaymentDTOConverter;
import com.lemnt.pay.application.payment.dto.PaymentDTO;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付单管理查询执行器。 */
@Component
@RequiredArgsConstructor
public class PaymentDetailQueryExe {
    private final TradePaymentGateway gateway;

    /** 按交易编号查询详情。 */
    public PaymentDTO execute(String no) {
        return PaymentDTOConverter.toDTO(gateway.findByPaymentNo(PaymentRules.text(no, 40)));
    }
}
