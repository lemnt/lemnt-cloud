package com.lemnt.trade.application.paymentrefund.executor;

import com.lemnt.trade.application.paymentrefund.converter.PaymentRefundDTOConverter;
import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付退款管理查询执行器。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundDetailQueryExe {
    private final PaymentRefundGateway gateway;

    /** 按交易编号查询详情。 */
    public PaymentRefundDTO execute(String no) {
        return PaymentRefundDTOConverter.toDTO(gateway.find(OrderRules.text(no, 40)));
    }
}
