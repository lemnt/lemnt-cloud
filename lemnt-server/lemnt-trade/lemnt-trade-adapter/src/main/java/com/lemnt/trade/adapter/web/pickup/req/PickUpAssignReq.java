package com.lemnt.trade.adapter.web.pickup.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 设置自提订单请求。 */
@Schema(description = "设置自提订单请求")
public record PickUpAssignReq(
        @Schema(description = "订单编号") @NotBlank @Size(max = 40) String orderNo,
        @Schema(description = "自提门店编号") @NotNull Long storeId)
        implements Serializable {}
