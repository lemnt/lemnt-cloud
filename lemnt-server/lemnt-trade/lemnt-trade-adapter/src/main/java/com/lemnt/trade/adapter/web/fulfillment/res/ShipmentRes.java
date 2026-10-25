package com.lemnt.trade.adapter.web.fulfillment.res;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单履约ShipmentRes")
public record ShipmentRes(
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "幂等请求编号") String requestNo,
        @Schema(description = "承运商编码") String carrierCode,
        @Schema(description = "运单编号") String trackingNo,
        @Schema(description = "收件人姓名") String recipientName,
        @Schema(description = "收件联系电话") String phone,
        @Schema(description = "收件详细地址") String address,
        @Schema(description = "发货时间，数据库时间") java.time.LocalDateTime shippedAt,
        @Schema(description = "收货时间，未收货为空") java.time.LocalDateTime receivedAt)
        implements Serializable {}
