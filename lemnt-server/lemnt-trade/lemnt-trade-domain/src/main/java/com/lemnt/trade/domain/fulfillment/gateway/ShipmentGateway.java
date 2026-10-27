package com.lemnt.trade.domain.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.model.Shipment;

import java.time.LocalDateTime;

/** Shipment 表领域存储接口。 */
public interface ShipmentGateway {
    /**
     * 发货。
     */
    Shipment shipment(String orderNo);

    /**
     * 保存发货。
     */
    void saveShipment(Shipment shipment);

    /**
     * 接收。
     */
    boolean receive(String orderNo, LocalDateTime time);
}
