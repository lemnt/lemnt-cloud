package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 会员本人订单的物流与收货快照。 */
@Schema(description = "会员订单物流信息")
public record AppOrderShipmentRes(
        @Schema(description = "承运商编码")
        String carrierCode,
        @Schema(description = "运单编号")
        String trackingNo,
        @Schema(description = "收件人")
        String recipientName,
        @Schema(description = "联系电话")
        String phone,
        @Schema(description = "收货地址")
        String address,
        @Schema(description = "发货时间")
        LocalDateTime shippedAt,
        @Schema(description = "收货时间")
        LocalDateTime receivedAt
) implements Serializable {
}
