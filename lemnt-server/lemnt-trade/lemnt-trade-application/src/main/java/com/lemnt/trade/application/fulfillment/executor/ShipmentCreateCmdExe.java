package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShipmentCreateCmdExe {
    private final OrderFulfillmentService service;

    public ShipmentDTO execute(ShipmentCreateCmd cmd) {
        return FulfillmentDTOConverter.shipment(service.ship(cmd));
    }
}
