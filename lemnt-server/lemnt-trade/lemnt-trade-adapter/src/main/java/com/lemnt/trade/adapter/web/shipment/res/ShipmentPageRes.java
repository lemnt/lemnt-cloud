package com.lemnt.trade.adapter.web.shipment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流单分页记录。 */
@Schema(description = "物流单分页记录")
public record ShipmentPageRes(
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "承运商编码")
        String carrierCode,
        @Schema(description = "运单编号")
        String trackingNo,
        @Schema(description = "发货时间")
        LocalDateTime shippedAt,
        @Schema(description = "收货时间，未收货为空")
        LocalDateTime receivedAt
) implements Serializable {
}
