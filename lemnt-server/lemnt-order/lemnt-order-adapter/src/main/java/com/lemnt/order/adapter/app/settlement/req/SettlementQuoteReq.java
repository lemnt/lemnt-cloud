package com.lemnt.order.adapter.app.settlement.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * 结算报价请求。
 */
@Schema(description = "SettlementQuoteReq")
public record SettlementQuoteReq(
    @Schema(description = "该会员购物车中已选中的SKU编号，1至100项") @NotEmpty @Size(max = 100)
    List<@NotNull @Positive Long> productIds,
    @Schema(description = "该会员的优惠券实例编号，可不传") @Positive Long couponId,
    @Schema(description = "是否使用积分抵扣，默认不使用") Boolean pointStatus,
    @Schema(description = "当前会员收货地址编号，快递配送时必填") @Positive Long addressId,
    @Schema(description = "配送方式：1 快递、2 自提，默认快递") Integer deliveryType,
    @Schema(description = "自提门店编号，自提时必填") @Positive Long pickUpStoreId)
    implements Serializable {
    /**
     * 快递必须选择收货地址，自提必须选择门店。
     */
    @Schema(hidden = true)
    @AssertTrue(message = "请选择有效的收货地址或自提门店")
    public boolean isDeliveryChoiceValid() {
        if (Integer.valueOf(2).equals(deliveryType)) {
            return pickUpStoreId != null && pickUpStoreId > 0;
        }
        return addressId != null && addressId > 0;
    }
}
