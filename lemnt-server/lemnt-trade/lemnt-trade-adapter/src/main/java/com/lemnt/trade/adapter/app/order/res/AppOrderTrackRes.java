package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 会员订单物流轨迹节点。 */
@Schema(description = "会员订单物流轨迹")
public record AppOrderTrackRes(
        @Schema(description = "轨迹时间")
        LocalDateTime time,
        @Schema(description = "轨迹描述")
        String description
) implements Serializable {
}
