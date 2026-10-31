package com.lemnt.trade.application.fulfillment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流轨迹查询结果。 */
public record ShipmentTrackDTO(
        LocalDateTime time,
        String description
) implements Serializable {
}
