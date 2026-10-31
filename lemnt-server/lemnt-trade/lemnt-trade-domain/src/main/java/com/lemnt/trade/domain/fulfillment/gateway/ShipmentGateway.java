package com.lemnt.trade.domain.fulfillment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentFilter;

import java.time.LocalDateTime;

/** Shipment 表领域存储接口。 */
public interface ShipmentGateway {
    /** 分页查询物流单。 */
    PageResult<Shipment> page(ShipmentFilter filter);

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
