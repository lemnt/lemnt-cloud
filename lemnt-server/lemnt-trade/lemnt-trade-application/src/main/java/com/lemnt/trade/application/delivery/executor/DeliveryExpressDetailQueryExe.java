package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送详情执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressDetailQueryExe {
    private final DeliveryExpressGateway gateway;

    /** 执行配送操作。 */
    public DeliveryExpressDTO execute(Long id) {
        return DeliveryDTOConverter.toDTO(gateway.find(id));
    }
}
