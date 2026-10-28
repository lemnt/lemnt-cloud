package com.lemnt.trade.adapter.web.shipment.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 物流单分页请求。 */
@Schema(description = "物流单分页请求")
public record ShipmentPageReq(
        @Schema(description = "订单编号")
        @Size(max = 40)
        String orderNo,
        @Schema(description = "承运商编码")
        @Size(max = 40)
        String carrierCode,
        @Schema(description = "运单编号")
        @Size(max = 100)
        String trackingNo,
        @Schema(description = "物流状态：SHIPPED已发出、RECEIVED已收货")
        @Size(max = 20)
        String status,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页数量，默认20")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {
}
