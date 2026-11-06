package com.lemnt.order.adapter.web.pickup.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 自提订单核销信息。
 */
@Schema(description = "自提订单核销信息")
public record PickUpOrderRes(
    @Schema(description = "订单编号") String orderNo,
    @Schema(description = "订单状态") String status,
    @Schema(description = "门店编号") Long storeId,
    @Schema(description = "门店名称") String storeName,
    @Schema(description = "门店电话") String storePhone,
    @Schema(description = "门店地址") String storeAddress,
    @Schema(description = "核销码") String verifyCode)
    implements Serializable {
}
