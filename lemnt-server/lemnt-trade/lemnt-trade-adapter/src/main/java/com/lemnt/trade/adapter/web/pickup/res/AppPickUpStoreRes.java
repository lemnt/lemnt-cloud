package com.lemnt.trade.adapter.app.pickup.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 小程序自提门店。 */
@Schema(description = "小程序自提门店")
public record AppPickUpStoreRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "名称") String name,
        @Schema(description = "简介") String introduction,
        @Schema(description = "电话") String phone,
        @Schema(description = "地区编号") Long areaId,
        @Schema(description = "详细地址") String detailAddress,
        @Schema(description = "logo") String logo,
        @Schema(description = "开门时间") String openingTime,
        @Schema(description = "关门时间") String closingTime,
        @Schema(description = "纬度") BigDecimal latitude,
        @Schema(description = "经度") BigDecimal longitude
) implements java.io.Serializable {
}
