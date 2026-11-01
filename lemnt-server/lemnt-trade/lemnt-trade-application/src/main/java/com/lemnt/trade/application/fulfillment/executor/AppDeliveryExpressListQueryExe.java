package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序退货可选快递公司，只返回启用状态。 */
@Component
@RequiredArgsConstructor
public class AppDeliveryExpressListQueryExe {
    private final DeliveryExpressGateway expresses;

    public List<DeliveryExpressDTO> execute() {
        return expresses.list(null, 0).stream()
                .map(value -> new DeliveryExpressDTO(value.id(), value.code(), value.name(), value.logo(),
                        value.sort(), value.status()))
                .toList();
    }
}
