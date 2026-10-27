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
        @Schema(description = "收件人姓名")
        @NotBlank
        @Size(max = 100)
        String recipientName,
        @Schema(description = "收件联系电话")
        @NotBlank
        @Size(max = 32)
        String phone,
        @Schema(description = "收件详细地址")
        @NotBlank
        @Size(max = 500)
        String address
) implements Serializable {}
