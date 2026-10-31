package com.lemnt.trade.domain.fulfillment.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流轨迹事件。 */
public record ShipmentTrack(
        LocalDateTime time,
        String description
) implements Serializable {
}
