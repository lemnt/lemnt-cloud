package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayChannelConfigDTO;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 查询应用下全部渠道配置，供选择使用。 */
@Component
@RequiredArgsConstructor
public class PayChannelListByAppQueryExe {
    private final PayChannelConfigGateway channels;

    public List<PayChannelConfigDTO> execute(Long appId) {
        if (appId == null || appId <= 0) {
            throw new IllegalArgumentException("所属应用不合法");
        }
        return channels.listByAppId(appId).stream().map(PayConfigDTOConverter::payChannel).toList();
    }
}
