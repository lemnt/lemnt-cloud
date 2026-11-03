package com.lemnt.order.application.config.executor;

import com.lemnt.order.application.config.converter.TradeConfigDTOConverter;
import com.lemnt.order.application.config.dto.TradeConfigDTO;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 交易配置详情执行器。 */
@Component
@RequiredArgsConstructor
public class TradeConfigDetailQueryExe {
    private final TradeConfigGateway gateway;

    /** 执行交易配置操作。 */
    public TradeConfigDTO execute() {
        return TradeConfigDTOConverter.toDTO(gateway.get());
    }
}
