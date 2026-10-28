package com.lemnt.trade.adapter.web.fulfillment.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 发货创建请求。
 */
@Schema(description = "ShipmentCreate请求")
public record ShipmentCreateReq(
        @Schema(description = "幂等请求编号")
        @NotBlank
        @Size(max = 64)
        String requestNo,
        @Schema(description = "承运商编码")
        @NotBlank
        @Size(max = 40)
        String carrierCode,
        @Schema(description = "运单编号")
        @NotBlank
        @Size(max = 100)
        String trackingNo,
        @Schema(description = "历史无地址快照订单补填；新订单由服务端读取收件人")
        @Size(max = 100)
        String recipientName,
        @Schema(description = "历史订单补填；新订单使用冻结手机号")
        @Size(max = 32)
        String phone,
        @Schema(description = "历史订单补填；新订单使用冻结收货地址")
        @Size(max = 500)
        String address
) implements Serializable {}
