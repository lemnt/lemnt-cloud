package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.dto.ShipmentTrackDTO;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentTrackGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 已登记运单的物流轨迹查询。 */
@Component
@RequiredArgsConstructor
public class ShipmentTrackListQueryExe {
    private final ShipmentGateway shipments;
    private final ShipmentTrackGateway tracks;

    /** 只按本地运单查询，不接受外部运单号覆盖。 */
    public List<ShipmentTrackDTO> execute(String orderNo) {
        var shipment = shipments.shipment(OrderRules.text(orderNo, 40));
        if (shipment == null) {
            throw new IllegalArgumentException("物流单不存在");
        }
        return tracks.tracks(shipment).stream().map(value ->
                new ShipmentTrackDTO(value.time(), value.description())).toList();
    }
}
