package com.lemnt.trade.application.config.executor;

import com.lemnt.trade.application.config.converter.TradeConfigDTOConverter;
import com.lemnt.trade.application.config.dto.TradeConfigDTO;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;

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
