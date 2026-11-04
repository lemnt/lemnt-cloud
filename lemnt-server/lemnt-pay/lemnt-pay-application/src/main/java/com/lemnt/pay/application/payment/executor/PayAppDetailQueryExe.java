package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.pay.application.payment.dto.PayAppDTO;
import com.lemnt.pay.domain.payment.gateway.PayAppGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 支付应用详情查询。
 */
@Component
@RequiredArgsConstructor
public class PayAppDetailQueryExe {
    private final PayAppGateway apps;

    public PayAppDTO execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("支付应用编号不合法");
        }
        return PayConfigDTOConverter.payApp(apps.find(id));
    }
}
