package com.lemnt.order.application.fulfillment.executor;

import com.lemnt.order.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.order.application.fulfillment.dto.ShipmentDTO;
import com.lemnt.order.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.order.domain.order.model.TradeOperator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 订单接收命令执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderReceiveCmdExe {

    private final OrderFulfillmentService orderFulfillmentService;

    /**
     * 执行订单接收命令。
     */
    public ShipmentDTO execute(String orderNo) {
        return FulfillmentDTOConverter.shipment(orderFulfillmentService.receive(orderNo));
    }

    /** 本人收货由领域操作明确记录会员身份。 */
    public ShipmentDTO executeForMember(Long memberId, String orderNo) {
        return FulfillmentDTOConverter.shipment(
                orderFulfillmentService.receive(orderNo, TradeOperator.member(memberId)));
    }
}
