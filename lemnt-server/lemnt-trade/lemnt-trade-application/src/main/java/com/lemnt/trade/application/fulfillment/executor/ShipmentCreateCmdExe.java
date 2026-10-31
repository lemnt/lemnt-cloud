package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.ShipmentDTO;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 发货创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ShipmentCreateCmdExe {

    private final OrderFulfillmentService orderFulfillmentService;

    /**
     * 执行发货创建命令。
     */
    public ShipmentDTO execute(ShipmentCreateCmd cmd) {
        return FulfillmentDTOConverter.shipment(orderFulfillmentService.ship(cmd));
    }
}
