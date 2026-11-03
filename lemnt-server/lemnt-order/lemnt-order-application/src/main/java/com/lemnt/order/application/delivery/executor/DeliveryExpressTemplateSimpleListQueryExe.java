package com.lemnt.order.application.delivery.executor;

import com.lemnt.order.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.order.application.delivery.dto.DeliveryExpressTemplateDTO;
import com.lemnt.order.domain.delivery.gateway.DeliveryExpressTemplateGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 配送选择列表执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateSimpleListQueryExe {
    private final DeliveryExpressTemplateGateway gateway;

    /** 执行配送操作。 */
    public List<DeliveryExpressTemplateDTO> execute() {
        return gateway.list(null).stream().map(DeliveryDTOConverter::toDTO).toList();
    }
}
