package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.ShipmentTrackGateway;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentTrack;
import com.lemnt.trade.infrastructure.fulfillment.tracking.ShipmentTrackClient;

import org.springframework.stereotype.Component;

import java.util.List;

/** 物流轨迹查询实现，不以模拟轨迹代替承运商结果。 */
@Component
public class ShipmentTrackGatewayImpl implements ShipmentTrackGateway {
    private final List<ShipmentTrackClient> clients;

    /** 注入已接入的承运商适配器。 */
    public ShipmentTrackGatewayImpl(List<ShipmentTrackClient> clients) {
        this.clients = List.copyOf(clients);
    }

    @Override
    public List<ShipmentTrack> tracks(Shipment shipment) {
        var matching = clients.stream().filter(client -> client.carrierCode().equals(shipment.carrierCode())).toList();
        if (matching.size() != 1) {
            throw new IllegalStateException("物流轨迹渠道未接入或配置重复");
        }
        var result = matching.get(0).tracks(shipment);
        if (result == null || result.stream().anyMatch(item -> item == null || item.time() == null
                || item.description() == null || item.description().isBlank())) {
            throw new IllegalStateException("物流轨迹响应不合法");
        }
        return List.copyOf(result);
    }
}
