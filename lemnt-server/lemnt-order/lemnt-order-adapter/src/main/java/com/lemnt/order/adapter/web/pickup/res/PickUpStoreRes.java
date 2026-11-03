package com.lemnt.order.adapter.web.pickup.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 自提门店响应。 */
@Schema(description = "自提门店响应")
public record PickUpStoreRes(
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
        @Schema(description = "经度") BigDecimal longitude,
        @Schema(description = "核销店员") List<Long> verifyUserIds,
        @Schema(description = "状态") Integer status)
        implements Serializable {}
