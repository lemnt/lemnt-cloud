package com.lemnt.mall.adapter.app.settlement.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "结算收件及运费快照，金额元")
public record SettlementDeliveryRes(
        @Schema(description = "当前会员收货地址编号") Long addressId,
        @Schema(description = "配送方式，1 快递") Integer deliveryType,
        @Schema(description = "收件人") String receiverName,
        @Schema(description = "收件联系电话") String receiverMobile,
        @Schema(description = "收件地区编号") Long receiverAreaId,
        @Schema(description = "收件详细地址") String receiverDetailAddress,
        @Schema(description = "订单运费，元") BigDecimal deliveryPrice
) implements Serializable {
}
