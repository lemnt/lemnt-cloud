package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayChannelConfigDTO;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 支付渠道配置详情查询。 */
@Component
@RequiredArgsConstructor
public class PayChannelDetailQueryExe {
    private final PayChannelConfigGateway channels;

    public PayChannelConfigDTO execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("渠道配置编号不合法");
        }
        return PayConfigDTOConverter.payChannel(channels.find(id));
    }
}
