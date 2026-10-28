package com.lemnt.trade.adapter.web.shipment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流轨迹事件。 */
@Schema(description = "物流轨迹事件")
public record ShipmentTrackRes(
        @Schema(description = "事件时间")
        LocalDateTime time,
        @Schema(description = "物流事件描述")
        String description
) implements Serializable {
}
