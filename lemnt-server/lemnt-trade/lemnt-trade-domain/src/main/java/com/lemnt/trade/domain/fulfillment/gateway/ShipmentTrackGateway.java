package com.lemnt.trade.domain.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentTrack;

import java.util.List;

/** 物流服务查询边界。 */
public interface ShipmentTrackGateway {
    /** 按已登记的承运商与运单查询，不接受客户端替换运单。 */
    List<ShipmentTrack> tracks(Shipment shipment);
}
