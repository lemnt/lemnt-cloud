package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.trade.infrastructure.fulfillment.mapper.ShipmentMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** Shipment 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class ShipmentGatewayImpl implements ShipmentGateway {
    private final ShipmentMapper shipmentMapper;

    @Override
    /**
     * 发货。
     */
    public Shipment shipment(String no) {
        return FulfillmentConverter.shipment(shipmentMapper.shipment(no));
    }

    @Override
    /**
     * 保存发货。
     */
    public void saveShipment(Shipment value) {
        shipmentMapper.insertShipment(FulfillmentConverter.shipment(value));
    }

    @Override
    /**
     * 接收。
     */
    public boolean receive(String no, LocalDateTime time) {
        return shipmentMapper.receive(no, time) == 1;
    }
}
