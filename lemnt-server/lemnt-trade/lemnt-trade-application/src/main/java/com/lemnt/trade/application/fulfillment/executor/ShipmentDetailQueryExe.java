package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.ShipmentDTO;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 物流单详情查询。 */
@Component
@RequiredArgsConstructor
public class ShipmentDetailQueryExe {
    private final ShipmentGateway shipments;

    /** 单独授权的详情可查询收件人，找不到时返回空。 */
    public ShipmentDTO execute(String orderNo) {
        return FulfillmentDTOConverter.shipment(shipments.shipment(OrderRules.text(orderNo, 40)));
    }
}
