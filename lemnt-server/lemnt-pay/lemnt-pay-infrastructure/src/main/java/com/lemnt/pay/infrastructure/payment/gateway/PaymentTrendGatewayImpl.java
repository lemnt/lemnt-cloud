package com.lemnt.pay.infrastructure.payment.gateway;

import com.lemnt.pay.domain.payment.gateway.PaymentTrendGateway;
import com.lemnt.pay.domain.payment.model.PaymentTrendDay;
import com.lemnt.pay.infrastructure.payment.mapper.PaymentTrendMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentTrendGatewayImpl implements PaymentTrendGateway {
    private final PaymentTrendMapper mapper;

    @Override
    public List<PaymentTrendDay> days(LocalDateTime from, LocalDateTime until) {
        return mapper.days(from, until);
    }
}
