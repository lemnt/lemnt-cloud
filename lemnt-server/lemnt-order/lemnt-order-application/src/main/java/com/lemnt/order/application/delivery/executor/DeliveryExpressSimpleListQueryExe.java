package com.lemnt.order.application.delivery.executor;

import com.lemnt.order.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.order.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.order.domain.delivery.gateway.DeliveryExpressGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 配送选择列表执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressSimpleListQueryExe {
    private final DeliveryExpressGateway gateway;

    /** 执行配送操作。 */
    public List<DeliveryExpressDTO> execute() {
        return gateway.list(null, 0).stream().map(DeliveryDTOConverter::toDTO).toList();
    }
}
